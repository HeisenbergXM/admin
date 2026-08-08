# VIN 物流后端接口变更清单

本次后端改造共新增 10 个接口，并调整 2 个既有车辆全景接口。未生成或修改前端代码。

## 1. 单车入库接口

接口基础路径：`/api/inbounds`

| 方法 | 路径 | 用途 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/inbounds` | 分页查询入库车辆 | `vlm:inbound:list` |
| GET | `/api/inbounds/{vehicleId}` | 查询单车入库详情 | `vlm:inbound:list` |
| POST | `/api/inbounds/{vehicleId}` | 创建入库草稿 | `vlm:inbound:add` |
| PUT | `/api/inbounds/{vehicleId}` | 更新入库草稿 | `vlm:inbound:edit` |
| POST | `/api/inbounds/{vehicleId}/confirm` | 确认入库并推进生命周期 | `vlm:inbound:confirm` |

### 1.1 分页查询入库车辆

```http
GET /api/inbounds
```

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `pageNum` | Integer | 否 | 页码 |
| `pageSize` | Integer | 否 | 每页数量 |
| `vin` | String | 否 | VIN 模糊查询 |
| `modelId` | Long | 否 | 车型 ID |
| `storageStartDate` | LocalDate | 否 | 入库开始日期 |
| `storageEndDate` | LocalDate | 否 | 入库结束日期 |
| `stageStatus` | String | 否 | `PENDING_INBOUND`、`DRAFT` 或 `CONFIRMED` |

返回类型：`Result<PageResult<InboundResponse>>`

状态过滤规则：

- `PENDING_INBOUND`：根据车辆主表生命周期查询待入库车辆，包括尚未创建草稿的车辆。
- `DRAFT`：查询已创建但尚未确认的入库草稿。
- `CONFIRMED`：查询已经确认的入库记录。

### 1.2 查询单车入库详情

```http
GET /api/inbounds/{vehicleId}
```

返回类型：`Result<InboundResponse>`

当车辆处于 `PENDING_INBOUND` 且尚未创建入库草稿时，可以返回车辆信息，此时阶段记录 `id` 为 `null`。

### 1.3 创建入库草稿

```http
POST /api/inbounds/{vehicleId}
Content-Type: application/json
```

请求示例：

```json
{
  "saicBuyOffDate": "2026-07-14",
  "dateToStorageYard": "2026-07-15",
  "remark2": "备注"
}
```

返回类型：`Result<Long>`，返回新增入库记录 ID。

草稿允许日期字段为空，`remark2` 最长 500 个字符。

### 1.4 更新入库草稿

```http
PUT /api/inbounds/{vehicleId}
Content-Type: application/json
```

请求体与创建草稿相同。只能修改尚未确认且车辆生命周期仍为 `PENDING_INBOUND` 的记录，关联的 `vehicleId` 不可变。

返回类型：`Result<Void>`。

### 1.5 确认入库

```http
POST /api/inbounds/{vehicleId}/confirm
```

确认条件：

- `saicBuyOffDate` 必须存在。
- `dateToStorageYard` 必须存在。
- 入库记录必须处于 `DRAFT`。
- 车辆生命周期必须为 `PENDING_INBOUND`。

确认成功后：

- 入库记录更新为 `CONFIRMED`。
- 写入确认人和确认时间。
- 车辆生命周期从 `PENDING_INBOUND` 推进至 `PENDING_ALLOCATION`。
- 使用事务和行锁避免并发更新覆盖已确认记录。

返回类型：`Result<Void>`。

## 2. 单车配送接口

接口基础路径：`/api/deliveries`

| 方法 | 路径 | 用途 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/deliveries` | 分页查询配送车辆 | `vlm:delivery:list` |
| GET | `/api/deliveries/{vehicleId}` | 查询单车配送详情 | `vlm:delivery:list` |
| POST | `/api/deliveries/{vehicleId}` | 创建配送草稿 | `vlm:delivery:add` |
| PUT | `/api/deliveries/{vehicleId}` | 更新配送草稿 | `vlm:delivery:edit` |
| POST | `/api/deliveries/{vehicleId}/confirm` | 确认配送签收并推进生命周期 | `vlm:delivery:confirm` |

### 2.1 分页查询配送车辆

```http
GET /api/deliveries
```

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `pageNum` | Integer | 否 | 页码 |
| `pageSize` | Integer | 否 | 每页数量 |
| `vin` | String | 否 | VIN 模糊查询 |
| `modelId` | Long | 否 | 车型 ID |
| `dealerId` | Long | 否 | 经销商 ID |
| `stageStatus` | String | 否 | `PENDING_DELIVERY`、`DRAFT` 或 `CONFIRMED` |

返回类型：`Result<PageResult<DeliveryResponse>>`

经销商信息不存储在配送阶段表中，只从已确认且未删除的销售分配记录读取。

### 2.2 查询单车配送详情

```http
GET /api/deliveries/{vehicleId}
```

返回类型：`Result<DeliveryResponse>`

当车辆处于 `PENDING_DELIVERY` 且尚未创建配送草稿时，可以返回车辆信息，此时阶段记录 `id` 为 `null`。

### 2.3 创建配送草稿

```http
POST /api/deliveries/{vehicleId}
Content-Type: application/json
```

请求示例：

```json
{
  "etdToDealer": "2026-07-17",
  "etaToDealer": "2026-07-18",
  "trollyType": "4 units",
  "fullyLoad": true,
  "receivedDate": "2026-07-19",
  "deliveryStatus": "DELIVERED",
  "remark7": "备注"
}
```

返回类型：`Result<Long>`，返回新增配送记录 ID。

草稿允许字段不完整，但已填写字段必须满足：

- `etaToDealer` 不能早于 `etdToDealer`。
- `trollyType` 只能为 `4 units` 或 `6 units`。
- `remark7` 最长 500 个字符。

### 2.4 更新配送草稿

```http
PUT /api/deliveries/{vehicleId}
Content-Type: application/json
```

请求体与创建草稿相同。只能修改尚未确认且车辆生命周期仍为 `PENDING_DELIVERY` 的记录，关联的 `vehicleId` 不可变。

返回类型：`Result<Void>`。

### 2.5 确认配送签收

```http
POST /api/deliveries/{vehicleId}/confirm
```

确认条件：

- `receivedDate` 必须存在。
- `etaToDealer` 不能早于 `etdToDealer`。
- `trollyType` 只能为 `4 units` 或 `6 units`。
- 必须存在已确认、未删除且包含经销商 ID 的销售分配记录。
- 配送记录必须处于 `DRAFT`。
- 车辆生命周期必须为 `PENDING_DELIVERY`。

确认成功后：

- 配送记录更新为 `CONFIRMED`。
- 写入确认人和确认时间。
- 车辆生命周期从 `PENDING_DELIVERY` 推进至 `PENDING_REGISTRATION`。
- 使用事务和行锁避免并发更新覆盖已确认记录。

返回类型：`Result<Void>`。

## 3. 车辆全景接口调整

以下接口路径和权限没有变化，但数据来源及响应内容发生调整。

| 方法 | 路径 | 权限 | 调整内容 |
| --- | --- | --- | --- |
| GET | `/api/panorama/{vin}` | `vlm:panorama:view` | 响应新增 `inbound`、`delivery` 分区 |
| GET | `/api/panorama/{vin}/export` | `vlm:panorama:export` | Excel 导出改为使用新的入库和配送阶段数据 |

### 3.1 全景响应变化

- 新增 `inbound`：单车入库阶段信息。
- 新增 `delivery`：单车配送阶段信息。
- 原 `transportOrder`、`dispatch` 字段继续保留，以兼容旧调用方。
- 新流程不再填充 `transportOrder`、`dispatch`。
- 不会从旧运输单或旧发车单数据回退填充新字段。

### 3.2 全景导出变化

新增入库导出分区：

1. `saicBuyOffDate`
2. `dateToStorageYard`
3. `confirmedBy`
4. `confirmedAt`

新增配送导出分区：

1. `etdToDealer`
2. `etaToDealer`
3. `trollyType`
4. `fullyLoad`
5. `receivedDate`
6. `deliveryStatus`
7. `confirmedBy`
8. `confirmedAt`

## 4. 保持不变的旧接口

以下旧接口及其权限行为保持不变：

- `/api/transport-orders/**`
- `/api/dispatch-lists/**`
- Waybill 相关接口

旧运输和发车页面菜单已停用，但旧按钮/API 权限节点继续保持启用，已有接口调用不会因为页面隐藏而失去权限。

## 5. 接口变更汇总

| 类型 | 数量 |
| --- | ---: |
| 新增单车入库接口 | 5 |
| 新增单车配送接口 | 5 |
| 调整既有全景接口 | 2 |
| 删除接口 | 0 |
| 修改旧运输接口行为 | 0 |
