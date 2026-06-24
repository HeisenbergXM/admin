# 后台管理系统设计文档

## 项目概述

| 项目 | 选择 |
|------|------|
| 系统类型 | 企业内部管理系统 |
| 用户规模 | 小型（< 100 人，简单组织结构） |
| 认证方式 | 用户名 + 密码 |
| 权限模型 | RBAC（用户 → 角色 → 权限） |
| 前端 | 纯后端 RESTful API |
| API 文档 | Swagger/Knife4j |
| 操作日志 | 记录所有操作（增删改查） |
| 缓存 | 无 Redis，数据库查询 |
| 项目结构 | 单体应用 |

## 技术栈

- SpringBoot 2.7+
- SpringSecurity + JWT
- MyBatis + MySQL
- Knife4j（Swagger 增强）
- BCryptPasswordEncoder（密码加密）

## 架构方案

经典分层架构：Controller → Service → Mapper 三层分离。

## 数据库设计

### 表关系图

```
┌─────────┐     ┌──────────────┐     ┌─────────┐
│  sys_user│────→│sys_user_role │←────│ sys_role │
└─────────┘     └──────────────┘     └─────────┘
                                          │
                                          │
                                 ┌──────────────┐
                                 │sys_role_menu  │
                                 └──────────────┘
                                          │
                                          ↓
                                    ┌─────────┐
                                    │sys_menu │
                                    └─────────┘

┌──────────────────┐    ┌──────────────────┐
│sys_operation_log  │    │ sys_login_log    │
└──────────────────┘    └──────────────────┘
```

### sys_user（用户表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| username | varchar(50) | 用户名，唯一 |
| password | varchar(100) | 密码（BCrypt加密） |
| nickname | varchar(50) | 昵称 |
| email | varchar(100) | 邮箱 |
| phone | varchar(20) | 手机号 |
| status | tinyint | 状态：0禁用 1正常 |
| avatar | varchar(200) | 头像URL |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| deleted | tinyint | 逻辑删除：0未删除 1已删除 |

### sys_role（角色表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| role_name | varchar(50) | 角色名称 |
| role_code | varchar(50) | 角色编码（如 ADMIN、USER），唯一 |
| description | varchar(200) | 描述 |
| status | tinyint | 状态：0禁用 1正常 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| deleted | tinyint | 逻辑删除 |

### sys_menu（菜单/权限表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| parent_id | bigint | 父菜单ID（0为顶级） |
| menu_name | varchar(50) | 菜单名称 |
| menu_type | tinyint | 类型：1目录 2菜单 3按钮 |
| path | varchar(200) | 路由路径 |
| permission | varchar(100) | 权限标识（如 sys:user:list） |
| icon | varchar(50) | 图标 |
| sort_order | int | 排序号 |
| status | tinyint | 状态：0禁用 1正常 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |
| deleted | tinyint | 逻辑删除 |

### sys_user_role（用户-角色关联表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| user_id | bigint | 用户ID |
| role_id | bigint | 角色ID |

### sys_role_menu（角色-菜单关联表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| role_id | bigint | 角色ID |
| menu_id | bigint | 菜单ID |

### sys_operation_log（操作日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| user_id | bigint | 操作用户ID |
| username | varchar(50) | 操作用户名 |
| operation | varchar(50) | 操作描述 |
| method | varchar(200) | 请求方法 |
| params | text | 请求参数 |
| result | text | 返回结果 |
| ip | varchar(50) | 操作IP |
| create_time | datetime | 操作时间 |

### sys_login_log（登录日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| username | varchar(50) | 登录用户名 |
| login_type | tinyint | 类型：1登录 2登出 |
| ip | varchar(50) | 登录IP |
| location | varchar(50) | 登录地点 |
| status | tinyint | 状态：0失败 1成功 |
| message | varchar(200) | 提示消息 |
| create_time | datetime | 登录时间 |

## API 接口设计

### 统一响应格式

```json
{
  "code": 200,
  "data": {},
  "message": "操作成功"
}
```

分页响应：

```json
{
  "code": 200,
  "data": {
    "list": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 认证模块 `/api/auth`

| 接口 | 方法 | 说明 | 是否需登录 |
|------|------|------|-----------|
| `/api/auth/login` | POST | 用户登录 | 否 |
| `/api/auth/logout` | POST | 用户登出 | 是 |
| `/api/auth/info` | GET | 获取当前登录用户信息 + 权限菜单 | 是 |

登录请求/响应：

```json
// POST /api/auth/login
请求: { "username": "admin", "password": "123456" }
响应: { "code": 200, "data": { "token": "xxx", "userId": 1 }, "message": "登录成功" }

// GET /api/auth/info
响应: { "code": 200, "data": {
  "userId": 1, "username": "admin", "nickname": "管理员",
  "roles": ["ADMIN"], "permissions": ["sys:user:list", "sys:user:add", ...]
}}
```

### 用户管理 `/api/user`

| 接口 | 方法 | 说明 | 权限标识 |
|------|------|------|---------|
| `/api/user/list` | GET | 用户列表（分页） | sys:user:list |
| `/api/user/{id}` | GET | 用户详情 | sys:user:list |
| `/api/user` | POST | 新增用户 | sys:user:add |
| `/api/user` | PUT | 修改用户 | sys:user:edit |
| `/api/user/{id}` | DELETE | 删除用户 | sys:user:delete |
| `/api/user/{id}/roles` | GET | 查询用户角色 | sys:user:list |
| `/api/user/{id}/roles` | PUT | 分配用户角色 | sys:user:role |
| `/api/user/{id}/reset-password` | PUT | 重置密码 | sys:user:resetPwd |
| `/api/user/profile` | PUT | 修改个人信息 | 无（自己改自己） |
| `/api/user/password` | PUT | 修改个人密码 | 无（自己改自己） |

### 角色管理 `/api/role`

| 接口 | 方法 | 说明 | 权限标识 |
|------|------|------|---------|
| `/api/role/list` | GET | 角色列表 | sys:role:list |
| `/api/role/{id}` | GET | 角色详情 | sys:role:list |
| `/api/role` | POST | 新增角色 | sys:role:add |
| `/api/role` | PUT | 修改角色 | sys:role:edit |
| `/api/role/{id}` | DELETE | 删除角色 | sys:role:delete |
| `/api/role/{id}/menus` | GET | 查询角色菜单 | sys:role:list |
| `/api/role/{id}/menus` | PUT | 分配角色菜单 | sys:role:menu |

### 菜单管理 `/api/menu`

| 接口 | 方法 | 说明 | 权限标识 |
|------|------|------|---------|
| `/api/menu/list` | GET | 菜单列表（树形） | sys:menu:list |
| `/api/menu/{id}` | GET | 菜单详情 | sys:menu:list |
| `/api/menu` | POST | 新增菜单 | sys:menu:add |
| `/api/menu` | PUT | 修改菜单 | sys:menu:edit |
| `/api/menu/{id}` | DELETE | 删除菜单 | sys:menu:delete |
| `/api/menu/tree` | GET | 菜单树（角色分配用） | sys:menu:list |

### 操作日志 `/api/log/operation`

| 接口 | 方法 | 说明 | 权限标识 |
|------|------|------|---------|
| `/api/log/operation/list` | GET | 操作日志列表（分页） | sys:log:list |

### 登录日志 `/api/log/login`

| 接口 | 方法 | 说明 | 权限标识 |
|------|------|------|---------|
| `/api/log/login/list` | GET | 登录日志列表（分页） | sys:log:list |

## SpringSecurity 认证与权限控制

### 认证流程

```
登录请求 → UsernamePasswordAuthenticationFilter
    → AuthenticationManager → UserDetailsService（从DB加载用户+角色+权限）
    → 认证成功 → JwtTokenUtil 生成 Token → 返回 Token

后续请求携带 Token（Header: Authorization: Bearer xxx）
    → JwtAuthenticationFilter（解析Token → 验证有效性 → 加载权限 → 设置到 SecurityContext）
    → 权限校验（@PreAuthorize）
    → Controller
```

### Security 核心组件

| 组件 | 说明 |
|------|------|
| JwtTokenUtil | Token 生成、解析、验证，过期时间设 24 小时 |
| JwtAuthenticationFilter | 自定义过滤器，拦截请求解析 Token |
| UserDetailsService | 从数据库加载用户信息 + 角色 + 权限标识 |
| SecurityConfig | 配置白名单路径、过滤器链、异常处理 |
| AccessDeniedHandler | 自定义 403 无权限响应（返回 JSON） |
| AuthenticationEntryPoint | 自定义 401 未登录响应（返回 JSON） |
| LogoutHandler | 登出时记录登录日志 |

### 白名单路径

```
/api/auth/login        # 登录
/swagger-ui/**         # Swagger UI
/v3/api-docs/**        # Swagger API 文档
/knife4j/**            # Knife4j 文档
```

### 权限校验方式

`@PreAuthorize` 注解 + SpEL 表达式：

```java
@PreAuthorize("hasAuthority('sys:user:add')")       // 单个权限
@PreAuthorize("hasAnyAuthority('sys:user:add', 'sys:user:edit')")  // 多个权限任一
@PreAuthorize("hasRole('ADMIN')")                    // 管理员角色
```

### 密码策略

- BCryptPasswordEncoder 加密存储
- 新增用户默认密码：123456
- 重置密码恢复为默认密码 123456

## 操作日志实现

### 方案：AOP + 自定义注解

**自定义注解 `@OperationLog`：**

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    String value();          // 操作描述，如 "新增用户"
    LogType type();          // 日志类型：OTHER、INSERT、UPDATE、DELETE、SELECT
    boolean saveParams();    // 是否保存请求参数，默认 true
    boolean saveResult();    // 是否保存返回结果，默认 false（SELECT 类型默认 true）
}
```

**使用方式：**

```java
@OperationLog(value = "新增用户", type = LogType.INSERT)
@PreAuthorize("hasAuthority('sys:user:add')")
@PostMapping
public Result addUser(@RequestBody UserDTO dto) { ... }
```

**AOP 切面逻辑：**

```
方法执行前 → 读取注解信息、获取请求参数、获取当前用户、获取IP
方法执行后 → 获取返回结果、组装日志对象、异步写入数据库
方法异常 → 记录异常信息到日志
```

日志异步写入使用 `@Async`，避免影响接口响应速度。

## 项目包结构

```
com.company.admin
│
├── controller/
│   ├── AuthController
│   ├── UserController
│   ├── RoleController
│   ├── MenuController
│   └── LogController
│
├── service/
│   ├── AuthService
│   ├── UserService
│   ├── RoleService
│   ├── MenuService
│   ├── OperationLogService
│   └── LoginLogService
│
├── mapper/
│   ├── UserMapper
│   ├── RoleMapper
│   ├── MenuMapper
│   ├── UserRoleMapper
│   ├── RoleMenuMapper
│   ├── OperationLogMapper
│   └── LoginLogMapper
│
├── entity/
│   ├── User
│   ├── Role
│   ├── Menu
│   ├── UserRole
│   ├── RoleMenu
│   ├── OperationLog
│   └── LoginLog
│
├── dto/
│   ├── request/
│   │   ├── LoginRequest
│   │   ├── UserQueryRequest
│   │   ├── UserCreateRequest
│   │   ├── UserUpdateRequest
│   │   ├── RoleCreateRequest
│   │   ├── MenuCreateRequest
│   │   ├── RoleMenuRequest
│   │   └── UserRoleRequest
│   ├── response/
│   │   ├── LoginResponse
│   │   ├── UserInfoResponse
│   │   ├── MenuTreeResponse
│   │   └── PageResponse<T>
│
├── config/
│   ├── SecurityConfig
│   ├── SwaggerConfig
│   ├── MyBatisConfig
│   └── AsyncConfig
│
├── security/
│   ├── JwtTokenUtil
│   ├── JwtAuthenticationFilter
│   ├── CustomUserDetailsService
│   ├── CustomAccessDeniedHandler
│   ├── CustomAuthEntryPoint
│   └── CustomLogoutHandler
│
├── annotation/
│   └── OperationLog
│
├── aspect/
│   └── OperationLogAspect
│
├── common/
│   ├── Result
│   ├── PageResult
│   ├── BusinessException
│   └── ErrorCode
│
└── AdminApplication
```

## 错误处理

### 自定义异常

```
BusinessException（业务异常）
    ├── code: 错误码
    ├── message: 错误消息
    └── 用户触发的可预期异常，返回 400

RuntimeException → 全局异常处理器兜底 → 返回 500
```

### 全局异常处理器

| 异常类型 | HTTP 状态码 | 处理 |
|----------|------------|------|
| BusinessException | 400 | 返回业务错误码 + 消息 |
| MethodArgumentNotValidException | 400 | 参数校验失败，返回具体字段错误 |
| AccessDeniedException | 403 | 无权限，返回 JSON |
| AuthenticationException | 401 | 未登录，返回 JSON |
| 其他 Exception | 500 | 记录日志，返回通用错误消息 |

### 错误码枚举

| 码值 | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 |
| 403 | 无权限 |
| 1001 | 用户名或密码错误 |
| 1002 | 用户已禁用 |
| 1003 | 用户名已存在 |
| 2001 | 角色编码已存在 |
| 3001 | 菜单名称已存在 |
| 3002 | 存在子菜单，无法删除 |
| 4001 | Token 已过期 |
| 4002 | Token 无效 |
| 5000 | 系统内部错误 |
