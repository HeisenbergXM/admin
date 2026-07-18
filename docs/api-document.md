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
6. [车辆生命周期与数据修订](#6-车辆生命周期与数据修订)
7. [数据模型](#7-数据模型)
8. [错误码](#8-错误码)

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

## 6. 车辆生命周期与数据修订

### 6.1 阶段待办列表规则

所有面向业务操作人员的车辆/阶段列表均固定查询各自的**当前生命周期阶段**。调用方传入的 `lifecycleStage`、`stageStatus` 或 `CONFIRMED` 等筛选条件只能在该固定范围内进一步缩小结果，不能把已完成或其他阶段的历史数据重新查询出来。阶段确认成功后，车辆推进到下一阶段，并立即从原阶段的待办列表中消失。

| 列表接口 | 固定生命周期阶段 | 用途 |
|------|------|------|
| `GET /api/vehicles` | `PENDING_OFFLINE` | 生产录入待办 |
| `GET /api/inbounds` | `PENDING_INBOUND` | 入库待办 |
| `GET /api/allocations` | `PENDING_ALLOCATION` | 销售分配待办 |
| `GET /api/invoices` | `PENDING_INVOICE` | 发票待办 |
| `GET /api/payments` | `PENDING_PAYMENT` | 收款待办 |
| `GET /api/deliveries` | `PENDING_DELIVERY` | 配送待办 |
| `GET /api/registrations` | `PENDING_REGISTRATION` | 上牌待办 |

当前阶段尚未创建草稿记录的车辆，以及已创建草稿但尚未确认的车辆，都仍可在对应待办列表中出现；确认后的记录不提供通过 `CONFIRMED` 等参数回查历史的能力。需要查询或修订任意阶段历史数据时，应使用下述仅面向管理员的“车辆数据修订”接口。

### 6.2 车辆数据修订

“车辆数据修订”是确认后发现业务字段录入错误时的补救入口。它保留完整车辆历史，不受当前生命周期阶段限制；默认菜单与权限只授予 `ADMIN`，普通业务专员和业务主管没有访问权限。

#### GET /api/vehicle-corrections

**权限：** `vlm:vehicle-correction:list`

管理员完整车辆分页查询。查询参数与 `GET /api/vehicles` 一致（`pageNum`、`pageSize`、`vin`、`modelId`、`modelName`、`series`、`lifecycleStage`、`dealerId`），但不固定生命周期阶段，可查询任意进行中或已完成车辆。

#### GET /api/vehicle-corrections/{vehicleId}

**权限：** `vlm:vehicle-correction:list`

返回车辆全景聚合数据，包括生产、入库、销售分配、多张发票、收款、配送和上牌的既有数据。没有对应阶段记录时，单条阶段对象返回 `null`，发票返回空列表；不存在或已删除的车辆返回 `VEHICLE_NOT_FOUND`。

#### PUT /api/vehicle-corrections/{vehicleId}

**权限：** `vlm:vehicle-correction:edit`

只更新请求中出现、且已经属于路径中 `vehicleId` 的既有阶段记录；本接口不创建阶段记录。每个传入阶段记录会在同一数据库事务中加锁并校验归属，任一步失败都会回滚本次请求的全部更新。成功后记录字段级的修改前后差异审计日志；失败请求仍由通用操作日志记录异常。

请求体只接受以下七个嵌套对象；未出现的对象不修改。除可选字段外，`id` 必填；`invoices` 可以一次提交多条发票，但每一项的 `id` 必须唯一，且必须属于路径车辆。

| 嵌套对象 | 可写字段 | 必填/校验规则 |
|------|------|------|
| `production` | `id`、`modelId`、`exteriorColorId`、`interiorColorId`、`engineNumber`、`yearMake`、`material`、`shipment`、`batch`、`offlineEpmbDate`、`epmbOkDate`、`remark1` | `id`、车型、内/外饰颜色和发动机号必填；车型及颜色必须为启用数据；`remark1` 最长 500 字符 |
| `inbound` | `id`、`saicBuyOffDate`、`dateToStorageYard`、`remark2` | 已确认记录的两个日期均不可为空；`remark2` 最长 500 字符 |
| `allocation` | `id`、`allocatedDate`、`dealerId`、`salesStatus`、`remark3` | `dealerId` 必填且必须为启用经销商；销售状态须为有效字典值；`remark3` 最长 500 字符 |
| `invoices` | 数组项的 `id`、`invoiceType`、`invoiceNo`、`invoiceDate`、`remark` | 每项的 `id`、发票类型、发票号和发票日期必填；发票类型须为有效字典值；`remark` 最长 500 字符 |
| `payment` | `id`、`paymentDate`、`creditFullPaymentDate`、`paymentStatus`、`remark5` | `paymentDate` 必填；收款状态须为有效字典值；`remark5` 最长 500 字符 |
| `delivery` | `id`、`etdToDealer`、`etaToDealer`、`trollyType`、`fullyLoad`、`receivedDate`、`deliveryStatus`、`remark7` | 已确认记录的签收日期不可为空；预计到达日不得早于发车日；拖运车类型仅限 `4 units` 或 `6 units`；配送状态须为有效字典值；`remark7` 最长 500 字符 |
| `registration` | `id`、`drosstechStatus`、`uploadDate`、`registrationDate`、`customerRegion`、`remark8` | `customerRegion` 最长 100 字符；`remark8` 最长 500 字符；状态须为有效字典值 |

请求示例：

```json
{
  "production": {
    "id": 101,
    "modelId": 10,
    "exteriorColorId": 20,
    "interiorColorId": 30,
    "engineNumber": "ENG-0001",
    "yearMake": "2026",
    "material": "M-01",
    "shipment": "S-01",
    "batch": "B-01",
    "offlineEpmbDate": "2026-07-01",
    "epmbOkDate": "2026-07-02",
    "remark1": "修正生产信息"
  },
  "invoices": [
    {
      "id": 201,
      "invoiceType": "PROFORMA_INVOICED",
      "invoiceNo": "PI-202607-001",
      "invoiceDate": "2026-07-03",
      "remark": "修正发票号"
    }
  ],
  "delivery": {
    "id": 301,
    "etdToDealer": "2026-07-04",
    "etaToDealer": "2026-07-05",
    "trollyType": "4 units",
    "fullyLoad": true,
    "receivedDate": "2026-07-05",
    "deliveryStatus": "DELIVERED",
    "remark7": "修正配送信息"
  }
}
```

VIN、车辆 ID、生命周期阶段、阶段处理状态（`stageStatus`）、发票序号、确认人/确认时间、创建/更新时间及删除标记均不在请求白名单内，无法通过本接口写入；接口也不会触发生命周期推进或阶段确认。

---

## 7. 数据模型

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

## 8. 错误码

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
| 6001 | 车辆不存在 |
| 6018 | 修订记录与车辆不匹配 |

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
