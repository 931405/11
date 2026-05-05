# 用户模块分析与拷贝说明

## 项目结构概览

- `frontend`：Vue 3 + Vite + Pinia + Axios + Element Plus
- `backend`：Spring Boot 3 + Spring Security + JPA + MySQL + JWT
- `user`：我从原项目中整理出的“登录 / 注册 / 数据库相关功能”拷贝

## 登录与注册链路

### 前端

- `frontend/src/views/auth/LoginPage.vue`
  - 登录表单页面
  - 调用 `login(form)`
  - 登录成功后把 token 和用户信息写入 Pinia / localStorage

- `frontend/src/views/auth/RegisterPage.vue`
  - 注册表单页面
  - 支持 `STUDENT` 和 `ENTERPRISE` 两种角色
  - 提交到 `/api/auth/register`

- `frontend/src/api/auth.js`
  - 封装 `/api/auth/login`
  - 封装 `/api/auth/register`

- `frontend/src/utils/request.js`
  - Axios 请求封装
  - 自动在请求头挂载 `Authorization: Bearer <token>`
  - 401/403 时会清空登录态并跳回 `/login`

- `frontend/src/stores/user.js`
  - 保存 token
  - 保存 `userId / username / realName / role / avatar`
  - 提供学生、企业、管理员角色判断

### 后端

- `backend/src/main/java/com/yb/partjob/controller/AuthController.java`
  - 暴露 `/api/auth/login`
  - 暴露 `/api/auth/register`
  - 还包含 `/api/auth/change-password`

- `backend/src/main/java/com/yb/partjob/service/impl/AuthServiceImpl.java`
  - 登录时按用户名查 `sys_user`
  - 校验密码和状态
  - 生成 JWT 并返回 `LoginVO`
  - 注册时先写 `sys_user`
  - 如果角色是学生，再写 `student_profile`
  - 如果角色是企业，再写 `enterprise_info`

- `backend/src/main/java/com/yb/partjob/config/JwtUtil.java`
  - 负责 JWT 生成和解析

- `backend/src/main/java/com/yb/partjob/config/JwtAuthFilter.java`
  - 从请求头提取 Bearer Token
  - 将 `userId` 和 `role` 放入 Spring Security 上下文

- `backend/src/main/java/com/yb/partjob/config/SecurityConfig.java`
  - 放行 `/api/auth/**`
  - 其他接口按角色控制访问

### 数据库

- `sys_user`
  - 用户主表
  - 保存账号、密码、角色、手机号、邮箱、状态

- `student_profile`
  - 学生扩展资料
  - 通过 `user_id` 关联 `sys_user.id`

- `enterprise_info`
  - 企业扩展资料
  - 通过 `user_id` 关联 `sys_user.id`

- `sys_log`
  - 登录接口上的 `@LogOperation` 最终会写入该表

- 
