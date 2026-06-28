package com.yupi.yucodemotherbackend.ratelimter.aop;

import com.yupi.yucodemotherbackend.exception.BusinessException;
import com.yupi.yucodemotherbackend.exception.ErrorCode;
import com.yupi.yucodemotherbackend.model.entity.User;
import com.yupi.yucodemotherbackend.ratelimter.annotation.RateLimit;
import com.yupi.yucodemotherbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 实现限流切面
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserService userService;

    // 核心逻辑：限流不需要环绕通知了，只需要在 方法前 判断
    @Before("@annotation(rateLimit)")
    public void doBefore(JoinPoint point, RateLimit rateLimit) {
        // 针对限流场景，调用 生成唯一Key 的方法
        String key = generateRateLimitKey(point, rateLimit);

        // 使用 Redisson 的分布式限流器
            // 1.使用Redisson的客户端得到一个限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
            // 2.调用限流器，并设置合理过期时间 - 1h
        rateLimiter.expire(Duration.ofHours(1));
            // 3.设置限流参数：每个时间窗口允许的请求数和时间窗口
        rateLimiter.trySetRate(RateType.OVERALL, rateLimit.rate(), rateLimit.rateInterval(), RateIntervalUnit.SECONDS);
            // 4.尝试获取一个令牌，如果获取失败则限流
        if (!rateLimiter.tryAcquire(1)) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, rateLimit.message());
        }
    }


    private String generateRateLimitKey(JoinPoint point, RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("rate_limit:");

        // 添加自定义前缀
        if (!rateLimit.key().isEmpty()) {
            keyBuilder.append(rateLimit.key()).append(":");
        }
        // 根据限流类型生成不同的key
        switch (rateLimit.limitType()) {
            case API:
                // 接口级别：方法名 -> 先从切入点调用接口名称Method -> 然后把method当成key拼接
                MethodSignature signature = (MethodSignature) point.getSignature();
                Method method = signature.getMethod();
                keyBuilder.append("api:").append(method.getDeclaringClass().getSimpleName())
                        .append(".").append(method.getName());
                break;

            case USER:
                // 用户级别：用户Id
	            try {
		            // 1.先拿到Attributes -> 然后获取request
                    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		            if (attributes != null) {
		                // 获取 request -> 拿到当前登录用户信息
                        HttpServletRequest request = attributes.getRequest();
                        User loginUser = userService.getLoginUser(request);
                        // 把用户Id当成key拼接限流
                        keyBuilder.append("user:").append(loginUser.getId());
                    } else {
                        // 无法获取请求上下文，使用IP限流
                        keyBuilder.append("ip:").append(getClientIP());
                    }
	            } catch (BusinessException e) {
		            // 未登录用户使用IP限流
                    keyBuilder.append("ip:").append(getClientIP());
	            }
                break;
            case IP:
                // IP级别：客户端IP
                keyBuilder.append("ip：").append(getClientIP());
                break;
            default:
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的限流类型");
        }
        return keyBuilder.toString();
    }


    /**
     * 获取IP地址
     *
     * @return
     */
    private String getClientIP() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多级代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
     }

}
