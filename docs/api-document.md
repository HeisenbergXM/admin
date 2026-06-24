# 后台管理系统 API 接口文档

> **版本：** 1.0.0  
> **Base URL：** `http://localhost:8080`  
> **认证方式：** JWT Bearer Token（请求头 `Authorization: Bearer <token>`）

---

## 目录

1. [认证模块](#1-认证模块)
2. [用户管理](#2-用户管理)
3. [角色管理](#3-角色管理)
4. [菜单管理](#4-菜单管理)
5. [日志管理](#5-日志管理)
6. [数据模型](#6-数据模型)
7. [错误码](#7-错误码)

---

## 1. 认证模块

### 1.1 登录

**POST** `/api/auth/login`

> 公开接口，无需认证

**请求体：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**请求示例：**

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1
  }
}
```

---

### 1.2 获取当前用户信息

**GET** `/api/auth/info`

> 需要认证

**请求头：**

```
Authorization: Bearer <token>
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "admin",
    "nickname": "超级管理员",
    "avatar": null,
    "roles": ["1"],
    "permissions": [
      "sys:user:list",
      "sys:user:add",
      "sys:user:edit",
      "sys:user:delete",
      "sys:user:role",
      "sys:user:resetPwd",
      "sys:role:list",
      "sys:role:add",
      "sys:role:edit",
      "sys:role:delete",
      "sys:role:menu",
      "sys:menu:list",
      "sys:menu:add",
      "sys:menu:edit",
      "sys:menu:delete",
      "sys:log:list"
    ]
  }
}
```

---

### 1.3 登出

**POST** `/api/auth/logout`

> 需要认证，登出后 Token 失效

**响应示例：**

```json
{
  "code": 200,
  "message": "登出成功"
}
```

---

## 2. 用户管理

> 所有接口需要认证，部分需要 `sys:user:*` 权限

### 2.1 分页查询用户列表

**GET** `/api/user/list`

**权限：** `sys:user:list`

**Query 参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| username | string | 否 | - | 用户名（模糊搜索） |
| nickname | string | 否 | - | 昵称（模糊搜索） |
| status | int | 否 | - | 状态：0禁用 1正常 |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "id": 1,
        "username": "admin",
        "nickname": "超级管理员",
        "email": null,
        "phone": null,
        "status": 1,
        "avatar": null,
        "createTime": "2026-06-23 00:00:00",
        "updateTime": "2026-06-23 00:00:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

### 2.2 查询单个用户

**GET** `/api/user/{id}`

**权限：** `sys:user:list`

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | long | 用户 ID |

---

### 2.3 新增用户

**POST** `/api/user`

**权限：** `sys:user:add`

**请求体：**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| username | string | 是 | 3-50字符 | 用户名（唯一） |
| nickname | string | 是 | 最长50字符 | 昵称 |
| email | string | 否 | - | 邮箱 |
| phone | string | 否 | - | 手机号 |
| status | int | 否 | - | 状态：0禁用 1正常 |

> 默认密码为 `123456`（BCrypt 加密存储）

**请求示例：**

```json
{
  "username": "zhangsan",
  "nickname": "张三",
  "email": "zhangsan@company.com",
  "phone": "13800138000",
  "status": 1
}
```

---

### 2.4 修改用户

**PUT** `/api/user`

**权限：** `sys:user:edit`

**请求体：**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| id | long | 是 | - | 用户 ID |
| nickname | string | 是 | 最长50字符 | 昵称 |
| email | string | 否 | - | 邮箱 |
| phone | string | 否 | - | 手机号 |
| status | int | 否 | - | 状态 |

---

### 2.5 删除用户

**DELETE** `/api/user/{id}`

**权限：** `sys:user:delete`

---

### 2.6 获取用户角色 ID 列表

**GET** `/api/user/{id}/roles`

**权限：** `sys:user:list`

**响应示例：**

```json
{
  "code": 200,
  "data": [1, 2]
}
```

---

### 2.7 分配用户角色

**PUT** `/api/user/{id}/roles`

**权限：** `sys:user:role`

**请求体：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户 ID |
| roleIds | long[] | 否 | 角色 ID 列表 |

**请求示例：**

```json
{
  "userId": 2,
  "roleIds": [1, 2]
}
```

---

### 2.8 重置用户密码

**PUT** `/api/user/{id}/reset-password`

**权限：** `sys:user:resetPwd`

> 重置后密码为 `123456`

---

### 2.9 修改个人信息

**PUT** `/api/user/profile`

> 需要认证，操作当前登录用户

**请求体：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | string | 是 | 昵称 |
| email | string | 否 | 邮箱 |
| phone | string | 否 | 手机号 |

---

### 2.10 修改密码

**PUT** `/api/user/password`

> 需要认证，操作当前登录用户

**请求体：**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| oldPassword | string | 是 | - | 原密码 |
| newPassword | string | 是 | 6-20位 | 新密码 |

**请求示例：**

```json
{
  "oldPassword": "123456",
  "newPassword": "newPassword123"
}
```

---

## 3. 角色管理

> 所有接口需要认证，部分需要 `sys:role:*` 权限

### 3.1 查询角色列表

**GET** `/api/role/list`

**权限：** `sys:role:list`

**响应示例：**

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "roleName": "超级管理员",
      "roleCode": "ADMIN",
      "description": "拥有所有权限",
      "status": 1,
      "createTime": "2026-06-23 00:00:00"
    }
  ]
}
```

---

### 3.2 查询单个角色

**GET** `/api/role/{id}`

**权限：** `sys:role:list`

---

### 3.3 新增角色

**POST** `/api/role`

**权限：** `sys:role:add`

**请求体：**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| roleName | string | 是 | 最长50字符 | 角色名称 |
| roleCode | string | 是 | 最长50字符 | 角色编码（唯一） |
| description | string | 否 | 最长200字符 | 描述 |
| status | int | 否 | - | 状态：0禁用 1正常 |

---

### 3.4 修改角色

**PUT** `/api/role/{id}`

**权限：** `sys:role:edit`

**请求体：** 同新增角色

---

### 3.5 删除角色

**DELETE** `/api/role/{id}`

**权限：** `sys:role:delete`

> 同时删除该角色下的用户关联和菜单关联

---

### 3.6 获取角色已分配菜单 ID 列表

**GET** `/api/role/{id}/menus`

**权限：** `sys:role:list`

**响应示例：**

```json
{
  "code": 200,
  "data": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]
}
```

---

### 3.7 分配角色菜单

**PUT** `/api/role/{id}/menus`

**权限：** `sys:role:menu`

**请求体：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleId | long | 是 | 角色 ID |
| menuIds | long[] | 否 | 菜单 ID 列表 |

**请求示例：**

```json
{
  "roleId": 2,
  "menuIds": [1, 2, 3, 4, 5]
}
```

---

## 4. 菜单管理

> 所有接口需要认证，部分需要 `sys:menu:*` 权限

### 4.1 查询菜单列表（平铺）

**GET** `/api/menu/list`

**权限：** `sys:menu:list`

**响应示例：**

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "parentId": 0,
      "menuName": "系统管理",
      "menuType": 1,
      "path": "/system",
      "permission": null,
      "icon": "system",
      "sortOrder": 1,
      "status": 1
    }
  ]
}
```

> `menuType`：1=目录，2=菜单，3=按钮

---

### 4.2 查询菜单树形结构

**GET** `/api/menu/tree`

**权限：** `sys:menu:list`

**响应示例：**

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "parentId": 0,
      "menuName": "系统管理",
      "menuType": 1,
      "children": [
        {
          "id": 2,
          "parentId": 1,
          "menuName": "用户管理",
          "menuType": 2,
          "children": [
            {
              "id": 3,
              "parentId": 2,
              "menuName": "新增用户",
              "menuType": 3,
              "permission": "sys:user:add",
              "children": []
            }
          ]
        }
      ]
    }
  ]
}
```

---

### 4.3 查询单个菜单

**GET** `/api/menu/{id}`

**权限：** `sys:menu:list`

---

### 4.4 新增菜单

**POST** `/api/menu`

**权限：** `sys:menu:add`

**请求体：**

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| parentId | long | 否 | - | 父菜单 ID，默认 0（顶级） |
| menuName | string | 是 | 最长50字符 | 菜单名称（唯一） |
| menuType | int | 是 | - | 类型：1目录 2菜单 3按钮 |
| path | string | 否 | - | 路由路径 |
| permission | string | 否 | - | 权限标识（如 `sys:user:add`） |
| icon | string | 否 | - | 图标 |
| sortOrder | int | 否 | - | 排序号 |
| status | int | 否 | - | 状态：0禁用 1正常 |

---

### 4.5 修改菜单

**PUT** `/api/menu/{id}`

**权限：** `sys:menu:edit`

**请求体：** 同新增菜单

---

### 4.6 删除菜单

**DELETE** `/api/menu/{id}`

**权限：** `sys:menu:delete`

> 存在子菜单时无法删除，需先删除子菜单  
> 同时删除角色-菜单关联

---

## 5. 日志管理

> 所有接口需要认证，权限：`sys:log:list`

### 5.1 查询操作日志

**GET** `/api/log/operation/list`

**权限：** `sys:log:list`

**Query 参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |

**响应示例：**

```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "username": "admin",
        "operation": "新增用户",
        "method": "UserController.create",
        "params": "[{\"username\":\"zhangsan\",...}]",
        "result": null,
        "ip": "127.0.0.1",
        "createTime": "2026-06-23 22:30:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

### 5.2 查询登录日志

**GET** `/api/log/login/list`

**权限：** `sys:log:list`

**Query 参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |

**响应示例：**

```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "username": "admin",
        "loginType": 1,
        "ip": "127.0.0.1",
        "location": null,
        "status": 1,
        "message": "登录成功",
        "createTime": "2026-06-23 22:00:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

> `loginType`：1=登录，2=登出  
> `status`：0=失败，1=成功

---

## 6. 数据模型

### User 用户

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| username | string | 用户名 |
| password | string | 密码（BCrypt） |
| nickname | string | 昵称 |
| email | string | 邮箱 |
| phone | string | 手机号 |
| status | int | 状态：0禁用 1正常 |
| avatar | string | 头像 URL |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |

### Role 角色

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| roleName | string | 角色名称 |
| roleCode | string | 角色编码 |
| description | string | 描述 |
| status | int | 状态：0禁用 1正常 |
| createTime | datetime | 创建时间 |

### Menu 菜单

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| parentId | long | 父菜单 ID（0=顶级） |
| menuName | string | 菜单名称 |
| menuType | int | 类型：1目录 2菜单 3按钮 |
| path | string | 路由路径 |
| permission | string | 权限标识 |
| icon | string | 图标 |
| sortOrder | int | 排序号 |
| status | int | 状态 |

### UserInfoResponse 用户信息响应

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | long | 用户 ID |
| username | string | 用户名 |
| nickname | string | 昵称 |
| avatar | string | 头像 URL |
| roles | string[] | 角色 ID 列表 |
| permissions | string[] | 权限标识集合 |

### LoginResponse 登录响应

| 字段 | 类型 | 说明 |
|------|------|------|
| token | string | JWT Token |
| userId | long | 用户 ID |

### OperationLog 操作日志

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| username | string | 操作用户 |
| operation | string | 操作描述 |
| method | string | 请求方法（类名.方法名） |
| params | string | 请求参数（JSON） |
| result | string | 返回结果（JSON） |
| ip | string | 操作 IP |
| createTime | datetime | 操作时间 |

### LoginLog 登录日志

| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 主键 |
| username | string | 登录用户名 |
| loginType | int | 类型：1登录 2登出 |
| ip | string | 登录 IP |
| location | string | 登录地点 |
| status | int | 状态：0失败 1成功 |
| message | string | 提示消息 |
| createTime | datetime | 登录时间 |

---

## 7. 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未登录 / Token 无效 |
| 403 | 无权限 |
| 1001 | 用户名或密码错误 |
| 1002 | 用户已禁用 |
| 1003 | 用户名已存在 |
| 1004 | 原密码错误 |
| 2001 | 角色编码已存在 |
| 3001 | 菜单名称已存在 |
| 3002 | 存在子菜单，无法删除 |
| 4001 | Token 已过期 |
| 4002 | Token 无效 |
| 5000 | 系统内部错误 |

---

## 统一响应格式

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

**分页响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [ ... ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

**错误响应：**

```json
{
  "code": 1001,
  "message": "用户名或密码错误",
  "data": null
}
```
