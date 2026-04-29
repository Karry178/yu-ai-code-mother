# 🎉 YuCodeMother v0.2 版本发布说明

## 📋 版本概述

本次 v0.2 版本是项目的重要里程碑，完成了完整的用户管理系统的后端和前端基础架构搭建，为后续 AI 代码生成功能奠定了坚实基础。

---

## ✨ 新增功能

### 🔐 用户认证与授权系统

#### 1. 用户注册与登录
- ✅ 用户注册功能（账号密码校验、重复密码验证）
- ✅ 用户登录功能（Session 会话管理）
- ✅ 用户注销功能
- ✅ 获取当前登录用户信息
- ✅ 密码加密存储（盐值加密，Salt = "Karry"）

#### 2. 权限控制系统
- ✅ 基于 AOP 的权限拦截器（`@AuthCheck` 注解）
- ✅ 角色权限管理（普通用户 / 管理员）
- ✅ 接口级别的权限校验
- ✅ 自动识别并拦截需要权限的接口

### 👥 用户管理功能（管理员）

- ✅ 创建用户（默认密码：12345678）
- ✅ 删除用户
- ✅ 更新用户信息
- ✅ 分页查询用户列表
- ✅ 根据 ID 获取用户详情
- ✅ 用户信息脱敏（VO 封装）

### 🏗️ 后端技术架构

#### 核心技术栈
- **Spring Boot 3.5.13** - 最新稳定版本
- **Java 21** - 使用最新 LTS 版本
- **MyBatis-Flex 1.11.6** - 轻量级 MyBatis 增强框架
- **MySQL 8.x** - 数据持久化
- **Lombok 1.18.44** - 简化代码编写

#### 项目结构
```
YuCodeMother-Backend/
├── annotation/          # 自定义注解（权限校验）
├── aop/                # AOP 切面（权限拦截器）
├── common/             # 通用类（统一响应、分页请求）
├── config/             # 配置类（CORS、JSON 序列化）
├── constant/           # 常量定义
├── controller/         # 控制器层
├── exception/          # 异常处理（全局异常处理器）
├── mapper/             # 数据访问层
├── model/              # 数据模型
│   ├── dto/           # 数据传输对象
│   ├── entity/        # 实体类
│   ├── enums/         # 枚举类
│   └── vo/            # 视图对象（脱敏）
├── service/            # 业务逻辑层
└── generator/          # MyBatis 代码生成器
```

#### 核心特性
- ✅ **统一响应格式**：`BaseResponse<T>` 封装所有接口返回
- ✅ **全局异常处理**：`GlobalExceptionHandler` 统一处理异常
- ✅ **业务异常封装**：`BusinessException` + `ErrorCode` 枚举
- ✅ **数据脱敏**：使用 VO 模式保护敏感信息
- ✅ **CORS 跨域配置**：支持前后端分离开发
- ✅ **Knife4j 接口文档**：自动生成 API 文档（OpenAPI 3.0）
- ✅ **HikariCP 连接池**：高性能数据库连接管理

### 🎨 前端技术架构

#### 核心技术栈
- **Vue 3.5.17** - 最新 Composition API
- **TypeScript 5.8** - 类型安全
- **Vite 7.0** - 极速构建工具
- **Ant Design Vue 4.2.6** - 企业级 UI 组件库
- **Vue Router 4.5.1** - 路由管理
- **Pinia 3.0.3** - 状态管理
- **Axios 1.15.2** - HTTP 请求库

#### 项目结构
```
YuCodeMother-Frontend/
├── api/                # API 接口定义（OpenAPI 自动生成）
├── assets/             # 静态资源
├── components/         # 公共组件
├── layouts/            # 布局组件
├── router/             # 路由配置
├── stores/             # Pinia 状态管理
├── views/              # 页面视图
├── App.vue             # 根组件
├── main.ts             # 入口文件
└── request.ts          # Axios 请求封装
```

#### 核心特性
- ✅ **TypeScript 全栈支持**：类型安全的开发体验
- ✅ **OpenAPI 自动生成**：后端接口自动生成前端 API 代码
- ✅ **组件化开发**：可复用的 Vue 3 组件
- ✅ **响应式布局**：适配多种设备
- ✅ **代码规范**：ESLint + Prettier 自动格式化

---

## 🔧 技术亮点

### 1. AOP 权限拦截器
使用 Spring AOP 实现优雅的权限控制：
```java
@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
    // 只有管理员才能访问
}
```

### 2. MyBatis-Flex 代码生成
- 自动生成 Mapper、Service、Controller
- 支持 Lombok 注解
- 雪花 ID 生成策略

### 3. 统一响应封装
```java
public class BaseResponse<T> {
    private int code;
    private T data;
    private String message;
}
```

### 4. 数据脱敏机制
- `LoginUserVO`：登录用户视图（隐藏密码）
- `UserVO`：用户信息视图（隐藏敏感字段）

---

## 📊 数据库设计

### User 表结构
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 雪花 ID 主键 |
| userAccount | VARCHAR | 用户账号 |
| userPassword | VARCHAR | 加密密码 |
| userName | VARCHAR | 用户昵称 |
| userAvatar | VARCHAR | 用户头像 |
| userProfile | VARCHAR | 用户简介 |
| userRole | VARCHAR | 用户角色（user/admin） |
| createTime | DATETIME | 创建时间 |
| updateTime | DATETIME | 更新时间 |
| isDelete | TINYINT | 逻辑删除 |

---

## 🚀 快速开始

### 后端启动
```bash
cd YuCodeMother-Backend
mvn clean install
mvn spring-boot:run
```

### 前端启动
```bash
cd YuCodeMother-Frontend
npm install
npm run dev
```

### 访问地址
- 前端：http://localhost:5173
- 后端：http://localhost:8080
- API 文档：http://localhost:8080/doc.html

---

## 📝 API 接口列表

### 公开接口
- `POST /user/register` - 用户注册
- `POST /user/login` - 用户登录
- `POST /user/logout` - 用户注销
- `POST /user/get/login` - 获取当前登录用户

### 管理员接口（需要 admin 权限）
- `POST /user/add` - 创建用户
- `POST /user/delete` - 删除用户
- `POST /user/update` - 更新用户
- `POST /user/list/page/vo` - 分页查询用户列表

### 通用接口
- `GET /user/get/vo` - 根据 ID 获取用户信息
- `GET /health` - 健康检查

---

## 🐛 Bug 修复

- ✅ 修复 Lombok 1.18.36 与 Java 21 兼容性问题（升级到 1.18.44）
- ✅ 修复权限拦截器逻辑错误（管理员权限判断）
- ✅ 修复 `joinPoint.proceed()` 未声明异常问题

---

## 📦 依赖版本

### 后端核心依赖
- Spring Boot: 3.5.13
- MyBatis-Flex: 1.11.6
- Lombok: 1.18.44
- Hutool: 5.8.44
- Knife4j: 4.4.0
- HikariCP: 6.3.3（Spring Boot 管理）

### 前端核心依赖
- Vue: 3.5.17
- TypeScript: 5.8.0
- Ant Design Vue: 4.2.6
- Vite: 7.0.0
- Pinia: 3.0.3

---

## 👨‍💻 贡献者

- [@Karry178](https://github.com/Karry178) - 项目负责人

---

## 📄 许可证

本项目采用 MIT 许可证

---

**发布日期**: 2026-04-24  
**版本号**: v0.2  
**分支**: 0.2
