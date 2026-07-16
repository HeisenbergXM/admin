# D00003 VIN 单车物流后端设计

**日期：** 2026-07-16

**上游需求：** `docs/D00003/specs/design.md`、`docs/D00004/specs/design.md`

**范围：** 仅当前 Spring Boot 后端仓库；D00004 仅作为接口字段、筛选条件和状态行为的参考，不包含前端实现。

## 1. 目标与边界

本次改造新增一套按 VIN 单车维护的入库和配送流程，并将其作为运输管理的可见入口。旧运输单、发车清单、行车路单和经销商行的数据库表、Java 代码、API、权限标识和自动化测试全部保留。

硬性边界：

- 不删除、不改表结构：`t_transport_order`、`t_transport_order_item`、`t_dispatch_list`、`t_waybill`、`t_waybill_dealer`、`t_waybill_dealer_vin`。
- 不修改旧 `TransportOrder`、`DispatchList`、`Waybill` 相关 Controller、Service、Mapper、Entity 和 DTO 的接口或行为。
- 不增加或重命名 `LifecycleStage` 枚举值。
- 不修改 `LifecycleService` 现有方法签名；新模块直接复用 `assertStage`、`assertNotConfirmed` 和 `confirmAndAdvance`。
- 不在配送阶段冗余保存经销商；经销商始终读取已确认的销售分配记录。
- 本期不支持批量确认、确认撤回和旧物流存量数据在全景视图中的混合展示。

## 2. 方案选择

采用“新增独立阶段模块”的方案：入库和配送分别新增完整的 Entity、DTO、Mapper、Service、Controller，与现有 `VehAllocation`、`VehPayment`、`VehRegistration` 模块保持同构。

不采用以下方案：

- 不将旧运输单接口改造成单车接口，因为这会破坏旧 API 和旧数据模型。
- 不先重构通用阶段框架，因为这会扩大共享代码变更面，增加其他生命周期模块的回归风险。

## 3. 数据模型

### 3.1 `t_veh_inbound`

一辆车最多一条入库阶段记录，`vehicle_id` 建唯一索引。

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| id | bigint | 自增主键 |
| vehicle_id | bigint | 非空，唯一；逻辑关联 `t_vehicle.id` |
| stage_status | varchar(20) | 非空，默认 `DRAFT` |
| saic_buy_off_date | date | 草稿可空，确认时必填 |
| date_to_storage_yard | date | 草稿可空，确认时必填 |
| remark2 | varchar(500) | 可空 |
| confirmed_by / confirmed_at | varchar(50) / datetime | 确认时写入 |
| created_by / created_at | varchar(50) / datetime | 审计字段 |
| updated_by / updated_at | varchar(50) / datetime | 审计字段 |
| deleted | tinyint | 逻辑删除，默认 0 |

增加 `uk_vehicle_id(vehicle_id)`、`idx_stage_status(stage_status)` 和 `idx_storage_date(date_to_storage_yard)`。

### 3.2 `t_veh_delivery`

一辆车最多一条配送阶段记录，`vehicle_id` 建唯一索引；不包含 `dealer_id`。

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| id | bigint | 自增主键 |
| vehicle_id | bigint | 非空，唯一；逻辑关联 `t_vehicle.id` |
| stage_status | varchar(20) | 非空，默认 `DRAFT` |
| etd_to_dealer | date | 可空 |
| eta_to_dealer | date | 可空；ETD、ETA 同时存在时 ETA 不得早于 ETD |
| trolly_type | varchar(20) | 可空；仅允许 `4 units`、`6 units` |
| fully_load | tinyint | 可空；Boolean 映射 |
| received_date | date | 草稿可空，确认签收时必填 |
| delivery_status | varchar(50) | 可空；复用 `delivery_status` 字典 |
| remark7 | varchar(500) | 可空 |
| confirmed_by / confirmed_at | varchar(50) / datetime | 确认时写入 |
| created_by / created_at | varchar(50) / datetime | 审计字段 |
| updated_by / updated_at | varchar(50) / datetime | 审计字段 |
| deleted | tinyint | 逻辑删除，默认 0 |

增加 `uk_vehicle_id(vehicle_id)`、`idx_stage_status(stage_status)`、`idx_eta_to_dealer(eta_to_dealer)` 和 `idx_received_date(received_date)`。

### 3.3 数据库交付形式

- 更新 `src/main/resources/sql/admin_system.sql`，保证全新初始化数据库包含两张新表和新菜单数据。
- 新增 `src/main/resources/sql/d00003_vin_logistics_migration.sql`，使用只新增/更新菜单状态的增量 SQL 升级已有数据库；不得 DROP 或 ALTER 旧物流表。

## 4. 后端模块与接口

### 4.1 入库模块

新增 `VehInbound` 模块，接口前缀为 `/api/inbounds`，权限前缀为 `vlm:inbound`。

| 方法 | 路径 | 权限 | 行为 |
|---|---|---|---|
| GET | `/api/inbounds` | `vlm:inbound:list` | 分页查询待处理、草稿或已确认记录 |
| GET | `/api/inbounds/{vehicleId}` | `vlm:inbound:list` | 获取单车入库详情；待处理车辆返回空业务字段和车辆基本信息 |
| POST | `/api/inbounds/{vehicleId}` | `vlm:inbound:add` | 创建 `DRAFT` 草稿 |
| PUT | `/api/inbounds/{vehicleId}` | `vlm:inbound:edit` | 修改未确认草稿 |
| POST | `/api/inbounds/{vehicleId}/confirm` | `vlm:inbound:confirm` | 校验必填字段，锁定记录并推进生命周期 |

分页查询参数：`pageNum`、`pageSize`、`vin`、`modelId`、`storageStartDate`、`storageEndDate`、`stageStatus`。

`stageStatus` 只接受：

- `PENDING_INBOUND`：按 `t_vehicle.lifecycle_stage` 查询，包含未建记录和已有草稿；响应分别返回 `PENDING_INBOUND` 或 `DRAFT`。
- `DRAFT`：按 `t_veh_inbound.stage_status` 查询。
- `CONFIRMED`：按 `t_veh_inbound.stage_status` 查询，不受车辆当前生命周期阶段限制。

未传 `stageStatus` 时默认按 `PENDING_INBOUND` 查询，符合待办列表语义。

### 4.2 配送模块

新增 `VehDelivery` 模块，接口前缀为 `/api/deliveries`，权限前缀为 `vlm:delivery`。

| 方法 | 路径 | 权限 | 行为 |
|---|---|---|---|
| GET | `/api/deliveries` | `vlm:delivery:list` | 分页查询待处理、草稿或已确认记录 |
| GET | `/api/deliveries/{vehicleId}` | `vlm:delivery:list` | 获取单车配送详情及只读经销商信息 |
| POST | `/api/deliveries/{vehicleId}` | `vlm:delivery:add` | 创建 `DRAFT` 草稿 |
| PUT | `/api/deliveries/{vehicleId}` | `vlm:delivery:edit` | 修改未确认草稿 |
| POST | `/api/deliveries/{vehicleId}/confirm` | `vlm:delivery:confirm` | 校验签收和经销商前置条件，锁定并推进生命周期 |

分页查询参数：`pageNum`、`pageSize`、`vin`、`modelId`、`dealerId`、`stageStatus`。

`stageStatus` 只接受 `PENDING_DELIVERY`、`DRAFT`、`CONFIRMED`；未传时默认 `PENDING_DELIVERY`。经销商筛选通过 `t_veh_allocation.dealer_id` 完成。

### 4.3 响应模型

两个列表/详情响应均返回：

- 记录 ID、车辆 ID、VIN。
- 当前 `lifecycleStage` 及中文标签。
- 阶段记录 `stageStatus` 及中文标签。
- 车型、车系、规格、车型代码、年款、内外饰颜色等只读车辆信息。
- 对应阶段业务字段、确认人和确认时间。

配送响应额外返回只读的 `dealerId`、`dealerCode`、`dealerName` 和 `deliveryStatusLabel`。所有标签继续通过 `BusinessStatusLabelService` 生成，不在 SQL 或 Controller 中硬编码。

## 5. 业务规则与事务

### 5.1 草稿

- 创建草稿前分别校验车辆处于 `PENDING_INBOUND` 或 `PENDING_DELIVERY`。
- 同一 `vehicle_id` 已有未删除记录时禁止重复创建。
- 更新时必须存在阶段记录，且 `stage_status` 不是 `CONFIRMED`。
- 草稿允许缺少确认必填字段，保证用户可以分步保存。

### 5.2 入库确认

确认事务内依次执行：

1. 查询入库记录，不存在则返回 `STAGE_DATA_NOT_FOUND`。
2. 校验 `saic_buy_off_date`、`date_to_storage_yard` 非空。
3. 调用 `LifecycleService.confirmAndAdvance(vehicleId, stageStatus, PENDING_INBOUND, PENDING_ALLOCATION, lockAction)`。
4. `lockAction` 将记录改为 `CONFIRMED`，写入当前用户名和当前时间。

任何校验或阶段推进失败时，阶段记录锁定和主表推进必须整体回滚。

### 5.3 配送确认

确认事务内依次执行：

1. 查询配送记录并校验未确认。
2. 校验 `received_date` 非空。
3. 查询该车未删除且 `stage_status=CONFIRMED` 的 `t_veh_allocation`，要求 `dealer_id` 非空。
4. 调用 `LifecycleService.confirmAndAdvance(vehicleId, stageStatus, PENDING_DELIVERY, PENDING_REGISTRATION, lockAction)`。
5. 锁定配送记录并写入确认审计字段。

草稿保存和确认均校验：ETD、ETA 同时存在时 `eta_to_dealer >= etd_to_dealer`；`trolly_type` 非空时只能是 `4 units` 或 `6 units`。

## 6. 全景视图适配

`VehiclePanoramaResponse` 新增：

- `InboundResponse inbound`
- `DeliveryResponse delivery`

为降低已有调用方的结构性风险，现有 `transportOrder` 和 `dispatch` Java 属性暂不删除，但标记为兼容字段；新的 `getPanorama` 不再从旧物流表填充这两个字段。本期不读取旧物流存量数据作为回退。

`VehiclePanoramaServiceImpl` 使用新 Mapper 填充：

- 入库分区和 `PENDING_ALLOCATION` 时间线节点来自 `t_veh_inbound`。
- 配送分区和 `PENDING_REGISTRATION` 时间线节点来自 `t_veh_delivery`。
- 只有 `CONFIRMED` 记录进入时间线。
- 全景 Excel 导出增加入库日期、SAIC buy off、配送 ETD/ETA、轿运车类型、满载标志、签收日期、配送状态和两段确认信息。

旧运输单和发车清单的独立 API 不受上述全景数据源切换影响。

## 7. 菜单、路由元数据与权限

保留旧菜单和旧权限记录，通过状态实现“隐藏入口但 API 仍可调用”：

- 将旧页面菜单 131、132 设置为 `status=0`，使其不进入用户导航树。
- 旧按钮权限 1030–1033、1040–1043 保持 `status=1` 且保留原角色关联，因此已授权用户仍持有旧 API 的 `vlm:transport:*`、`vlm:dispatch:*` 权限，可直接调用旧 API。
- 不删除旧菜单、按钮权限或 `sys_role_menu` 记录；恢复旧页面只需重新启用 131、132。

新增菜单和权限：

| ID | 名称 | 类型 | 路径/权限 |
|---|---|---|---|
| 133 | 中转运输 | 页面 | `/transport/vin-inbound`，`vlm:inbound:list` |
| 134 | 发车清单 | 页面 | `/transport/vin-delivery`，`vlm:delivery:list` |
| 1100–1103 | 入库查询/新增/编辑/确认 | 按钮 | `vlm:inbound:list/add/edit/confirm` |
| 1110–1113 | 配送查询/新增/编辑/确认 | 按钮 | `vlm:delivery:list/add/edit/confirm` |

角色授权：

- `ADMIN`：沿用“自动拥有全部启用权限”的现有行为，并补齐初始化角色菜单数据。
- `LOGISTICS_SPECIALIST`：获得两个页面及全部新增按钮权限。
- `BUSINESS_MANAGER`：获得两个页面的只读查询权限，不授予新增、编辑、确认权限。

## 8. 错误处理

继续复用现有错误码：

- 车辆不存在：`VEHICLE_NOT_FOUND`
- 生命周期不匹配：`LIFECYCLE_STAGE_MISMATCH`
- 已确认后修改或重复确认：`STAGE_ALREADY_CONFIRMED`
- 阶段记录不存在：`STAGE_DATA_NOT_FOUND`
- 重复创建、缺少确认必填项、日期顺序非法、轿运车类型非法、未分配经销商：使用 `BAD_REQUEST` 并返回明确中文业务消息。

不为本次两个简单阶段额外扩展共享错误码枚举，避免扩大公共契约。

## 9. 测试策略

### 9.1 新模块单元测试

入库和配送 Service 各覆盖：

- 创建草稿时校验正确生命周期并写入 `DRAFT`。
- 重复创建被拒绝。
- 已确认记录不能修改或重复确认。
- 入库确认缺少两个日期时失败。
- 配送确认缺少签收日期、缺少已确认经销商分配时失败。
- 配送日期顺序和 `trolly_type` 值校验。
- 确认调用正确的起止阶段，写入确认人/时间。
- 详情返回车辆、经销商和中文状态标签。

### 9.2 Mapper 集成测试

扩展 H2 schema 并验证：

- 未建草稿的待入库/待配送车辆仍出现在默认列表。
- 已建草稿显示 `DRAFT`，且仍在待办范围。
- 已确认记录只能通过 `CONFIRMED` 筛选查询。
- VIN、车型、到仓日期范围和经销商筛选正确。
- 配送列表经销商来自已确认销售分配，不来自配送表。

### 9.3 全景与菜单测试

- 全景响应和导出使用新入库、配送数据，并生成两段确认时间线。
- 旧物流表存在数据而新阶段表无数据时，本期全景不回退旧数据。
- 新菜单启用、旧页面菜单停用、旧按钮权限仍启用。
- 物流专员拥有新模块全部权限；业务主管仅有查询权限。
- 旧运输 API 所需权限仍能由已授权用户取得。

### 9.4 回归门禁

- 不修改或删除现有旧运输测试断言。
- 执行 `mvn test`，要求全部新旧测试通过。
- 当前环境未安装 `mvn`，实施阶段需通过项目构建环境、IDE Maven 或 CI 执行该命令并保留结果。

## 10. 验收标准

- 单车入库和配送均支持待办查询、草稿保存、草稿修改、详情查看和单车确认。
- 两次确认分别正确推进到 `PENDING_ALLOCATION` 和 `PENDING_REGISTRATION`，确认后锁定且事务一致。
- 配送经销商只读取自已确认销售分配，配送表无 `dealer_id`。
- 全景视图和导出只使用新物流阶段表。
- 用户导航仅展示“中转运输”和新的“发车清单”；旧页面入口隐藏但旧 API 仍可鉴权调用。
- 旧六张物流表、旧 Java 代码、旧 API、旧权限和旧测试全部保留。
- 生产、销售、财务、上牌、主数据、系统管理等其他模块行为不变。
