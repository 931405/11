# Feature Exports

这次新增的是“原项目功能源码导出”，不是把它们直接并入当前 `user/frontend` 和 `user/backend` 的最小可运行版本。

原因：

- 这两块功能依赖的职位、投递、匹配、推荐、后台统计链路比较大。
- 直接并入当前最小工程会引入大量额外依赖，容易把现有可运行状态打坏。
- 先按模块导出原始源码，后续如果需要，我再继续把其中某一块整合进当前 `user` 示例工程。

## 目录

- `user/feature_exports/user-info-management`
  - 对应图片 1 “用户信息管理”
- `user/feature_exports/job-match-management`
  - 对应图片 2 “岗位匹配管理”

## 图片 1 对应关系

### 用户信息管理

- 管理员：用户管理
  - 前端页：`frontend/src/views/admin/UserManagement.vue`
  - 主要接口：`frontend/src/api/admin.js`
  - 后端控制器：`backend/src/main/java/com/yb/partjob/controller/AdminController.java`
  - 后端服务：`backend/src/main/java/com/yb/partjob/service/impl/AdminServiceImpl.java`

- 企业：查投递信息
  - 前端页：`frontend/src/views/enterprise/CandidateList.vue`
  - 主要接口：`frontend/src/api/enterprise.js`
  - 后端控制器：`backend/src/main/java/com/yb/partjob/controller/EnterpriseController.java`
  - 后端服务：`backend/src/main/java/com/yb/partjob/service/impl/EnterpriseServiceImpl.java`

- 学生：查看个人资料
  - 前端页：`frontend/src/views/student/ProfileEdit.vue`
  - 主要接口：`frontend/src/api/student.js`
  - 后端控制器：`backend/src/main/java/com/yb/partjob/controller/StudentController.java`
  - 后端服务：`backend/src/main/java/com/yb/partjob/service/impl/StudentServiceImpl.java`
  - 用户基础资料接口：`backend/src/main/java/com/yb/partjob/controller/UserController.java`

## 图片 2 对应关系

### 岗位匹配管理

- 管理员：查看数据
  - 前端页：`frontend/src/views/admin/AdminDashboard.vue`
  - 匹配配置页：`frontend/src/views/admin/MatchSettings.vue`
  - 主要接口：`frontend/src/api/admin.js`
  - 后端控制器：`backend/src/main/java/com/yb/partjob/controller/AdminController.java`
  - 后端服务：`backend/src/main/java/com/yb/partjob/service/impl/AdminServiceImpl.java`

- 企业：看推荐列表筛选人选
  - 前端页：`frontend/src/views/enterprise/TalentPool.vue`
  - 主要接口：`frontend/src/api/enterprise.js`
  - 后端控制器：`backend/src/main/java/com/yb/partjob/controller/EnterpriseController.java`
  - 后端服务：`backend/src/main/java/com/yb/partjob/service/impl/EnterpriseServiceImpl.java`

- 学生：发起匹配查看推荐岗位
  - 前端页：`frontend/src/views/student/StudentDashboard.vue`
  - 推荐列表页：`frontend/src/views/student/JobList.vue`
  - 职位详情页：`frontend/src/views/student/JobDetail.vue`
  - 主要接口：`frontend/src/api/student.js`、`frontend/src/api/job.js`
  - 后端控制器：`backend/src/main/java/com/yb/partjob/controller/StudentController.java`、`backend/src/main/java/com/yb/partjob/controller/JobController.java`
  - 后端服务：`backend/src/main/java/com/yb/partjob/service/impl/StudentServiceImpl.java`、`backend/src/main/java/com/yb/partjob/service/impl/JobServiceImpl.java`、`backend/src/main/java/com/yb/partjob/service/impl/MatchServiceImpl.java`

## 数据库

两个模块需要的数据库结构都已经一并复制到了各自目录下的：

- `backend/src/main/resources/schema.sql`

如果你后面要做“真正可运行整合”，优先从以下几类对象继续往当前 `user/backend` 并：

- `JobPosition`
- `JobApplication`
- `MatchScore`
- `JobFavorite`
- `InterviewInvitation`
- 相关 Repository / DTO / VO / Service / Controller

## 当前状态

- 当前 `user/frontend` 和 `user/backend` 仍然保持之前的最小认证与资料管理版本。
- 新增的两个模块源码在 `user/feature_exports` 下，便于你单独查看和后续继续拆解。
