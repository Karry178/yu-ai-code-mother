# 前端无法获取后端数据排查指南

## 问题分析

你的 `/admin/userManage` 页面无法获取数据，可能的原因：

### 1. 后端服务未启动 ⚠️
**检查方法：**
```bash
# 在浏览器访问
http://localhost:8123/api/health/

# 或在命令行测试
curl http://localhost:8123/api/health/
```

**预期结果：**
```json
{
  "code": 0,
  "data": "ok",
  "message": "success"
}
```

**如果失败：** 启动后端服务
```bash
cd YuCodeMother-Backend
mvn spring-boot:run
```

---

### 2. 用户未登录或没有管理员权限 🔐
**问题：** `/user/list/page/vo` 接口需要 **管理员权限**

**检查方法：**
1. 打开浏览器开发者工具（F12）
2. 切换到 Network（网络）标签
3. 刷新页面
4. 查看 `/user/list/page/vo` 请求的响应

**可能的错误响应：**

#### 情况 A：未登录（40100）
```json
{
  "code": 40100,
  "data": null,
  "message": "未登录"
}
```
**解决方法：** 先登录
- 访问 `http://localhost:5173/user/login`
- 使用管理员账号登录

#### 情况 B：无权限（40101）
```json
{
  "code": 40101,
  "data": null,
  "message": "无权限"
}
```
**解决方法：** 使用管理员账号登录
- 确保登录的用户 `userRole` 字段为 `admin`

---

### 3. 数据库中没有数据 📊
**检查方法：**
```sql
-- 连接数据库
mysql -h 192.168.0.103 -u root -p

-- 切换数据库
USE yu_ai_code_mother;

-- 查询用户数据
SELECT * FROM user;
```

**如果没有数据：** 插入测试数据
```sql
-- 插入管理员账号（密码：12345678，已加密）
INSERT INTO user (userAccount, userPassword, userName, userRole) 
VALUES ('admin', 'b0dd3697a192885d7c055db46155b26a', '管理员', 'admin');

-- 插入普通用户
INSERT INTO user (userAccount, userPassword, userName, userRole) 
VALUES ('user001', 'b0dd3697a192885d7c055db46155b26a', '测试用户', 'user');
```

---

### 4. CORS 跨域问题 🌐
**检查方法：**
- 打开浏览器控制台（F12）
- 查看是否有 CORS 相关错误

**错误示例：**
```
Access to XMLHttpRequest at 'http://localhost:8123/api/user/list/page/vo' 
from origin 'http://localhost:5173' has been blocked by CORS policy
```

**解决方法：** 检查后端 CORS 配置
```java
// CorsConfig.java 应该包含：
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("http://localhost:5173")
            .allowCredentials(true)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .maxAge(3600);
}
```

---

### 5. 前端请求参数错误 📝
**检查方法：**
查看 UserManagePage.vue 中的请求参数：

```typescript
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,    // ✅ 正确
  pageSize: 10,  // ✅ 正确
})
```

**后端期望的参数：**
```java
public class UserQueryRequest {
    private long pageNum;   // 页码
    private int pageSize;   // 每页大小
}
```

---

## 快速诊断步骤

### Step 1: 检查后端是否运行
```bash
# 浏览器访问
http://localhost:8123/api/health/
```

### Step 2: 检查登录状态
```bash
# 浏览器访问（需要先登录）
http://localhost:8123/api/user/get/login
```

### Step 3: 检查数据库
```sql
SELECT * FROM user WHERE userRole = 'admin';
```

### Step 4: 查看浏览器控制台
- 打开 F12 开发者工具
- 查看 Console 标签的错误信息
- 查看 Network 标签的请求响应

---

## 最可能的原因

根据你的代码，**最可能的原因是：**

### ⭐ 用户未以管理员身份登录

因为接口有这个注解：
```java
@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
```

**解决方案：**

1. **创建管理员账号**
```sql
-- 密码是 12345678 加密后的值
INSERT INTO user (userAccount, userPassword, userName, userRole) 
VALUES ('admin', 'b0dd3697a192885d7c055db46155b26a', '管理员', 'admin');
```

2. **使用管理员账号登录**
- 访问：`http://localhost:5173/user/login`
- 账号：`admin`
- 密码：`12345678`

3. **登录后访问用户管理页面**
- 访问：`http://localhost:5173/admin/userManage`

---

## 调试代码

在 `UserManagePage.vue` 中添加调试信息：

```typescript
// 获取数据
const fetchData = async () => {
  console.log('开始请求数据，参数：', searchParams)
  
  try {
    const res = await listUserVoByPage({
      ...searchParams,
    })
    
    console.log('请求响应：', res)
    
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
      console.log('获取到的数据：', data.value)
    } else {
      console.error('获取数据失败：', res.data)
      message.error('获取数据失败,' + res.data.message)
    }
  } catch (error) {
    console.error('请求异常：', error)
    message.error('请求失败，请检查网络或后端服务')
  }
}
```

---

## 检查清单

- [ ] 后端服务已启动（端口 8123）
- [ ] 数据库连接正常
- [ ] 数据库中有用户数据
- [ ] 已使用管理员账号登录
- [ ] 浏览器控制台无 CORS 错误
- [ ] Network 标签显示请求成功（状态码 200）
- [ ] 响应数据 code 为 0

---

## 需要帮助？

如果以上步骤都检查过了还是不行，请提供：
1. 浏览器控制台的错误信息（Console 标签）
2. Network 标签中 `/user/list/page/vo` 请求的完整响应
3. 后端控制台的日志输出
