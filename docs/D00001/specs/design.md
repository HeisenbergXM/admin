# 设计文档 — 车辆全生命周期管理系统（VLM）

- **Spec ID**：D00001
- **需求类型**：产品需求
- **状态**：待批准（pending_approval）
- **来源**：`sources/Sample of Master Sheet.xlsx`

---

## 1. 项目概述

将现有 Excel 主台账（`Master Vin List (EV ICE)`）升级为一套支持多部门接力协作的 **车辆全生命周期管理系统**（Vehicle Lifecycle Management，下称 VLM）。系统以 **VIN（车辆识别码）** 为主线，串联生产、物流、销售、财务四大部门的业务动作，覆盖车辆从生产下线到经销商上牌的完整链路。

### 1.1 目标

- 以 VIN 为唯一主键，统一管理一辆车的全生命周期数据
- 多部门按职责分工维护各自阶段字段，操作可追溯
- 状态数据（销售、发票、收款、配送、上牌）通过字典统一维护，口径一致
- 生命周期阶段 **严格按序卡控**，杜绝跳步、漏填
- 主数据（车型、颜色、经销商）集中维护，录入时引用选择，减少手工错误

### 1.2 非目标（本期不含）

- 与上游 SAIC/MES、下游 DMS/上牌系统（Drosstech）的实时接口对接（本期仅做数据录入与状态维护）
- 财务总账/应收应账核算（仅记录开票与收款的业务状态，不做会计分录）
- 移动端 App（本期仅 Web 端）
- Excel 历史数据的自动迁移工具（本期可提供导入模板，迁移作为独立子任务）

---

## 2. 用户与角色

| 角色 | 职责 | 可操作模块 |
|------|------|-----------|
| 系统管理员 | 系统配置、用户/角色/菜单/字典维护 | 系统管理（全部） |
| 生产专员 | 车辆生产数据录入 | 生产管理/车辆录入 |
| 物流专员 | 车厂到仓库、仓库到经销商配送 | 运输管理（全部） |
| 销售专员 | 车辆销售分配、车辆上牌 | 销售管理（全部） |
| 财务专员 | 发票确认、收款确认、形式发票转正 | 财务管理（全部） |
| 主数据管理员 | 车型/颜色等主数据维护 | 主数据管理（全部） |
| 业务主管 | 全局查看、车辆全景视图 | 全部模块（只读）+ 车辆全景视图（可导出） |

> 角色与可操作模块的对应关系通过「角色管理 + 菜单管理」配置，上表为默认建议，最终以 RBAC 配置为准。

---

## 3. 系统架构（建议）

> 架构为建议项，最终技术选型可在 cx-work 阶段按团队习惯调整。

- **前端**：Web 单页应用（Vue 3 + Element Plus，或团队既有前端栈）
- **后端**：分层 REST API（Controller → Service → Repository）
- **数据库**：关系型数据库（MySQL / PostgreSQL）
- **权限**：基于 RBAC（用户-角色-菜单），后端接口级鉴权 + 前端菜单级控制（仅菜单级，无按钮/权限点）

---

## 4. 功能模块总览

```
系统管理
├── 用户管理        用户 CRUD、启用/停用、重置密码、分配角色
├── 角色管理        角色 CRUD、角色-菜单授权（仅授权菜单，无按钮级权限）
├── 菜单管理        菜单树维护（目录/菜单两级，含路由），不含按钮/权限点
└── 字典管理        销售状态、发票状态、收款状态、配送状态、上牌状态等

主数据管理
├── 车型数据        车系/SPEC/物料编码/年款等（源自 DATA 表）
├── 内饰颜色        内饰颜色字典
├── 外饰颜色        外饰颜色字典
└── 经销商维护      经销商编码/名称（源自 Dealer Code 表）

生产管理
└── 车辆录入        创建车辆主记录（VIN、车型、颜色、发动机号、生产信息）

运输管理
├── 车厂到仓库      运输单：表头(入库日期/备注)+多VIN明细(逐车SAIC buy off)，整单确认
└── 仓库到经销商    发车清单→行车路单(轿运车/Trolly)→经销商行(ETD/ETA/签收/状态+VIN)，按经销商行确认

销售管理
├── 车辆销售        分配经销商、分配日期、销售备注
└── 车辆上牌        Drosstech 状态、上传日期、注册日期、客户区域

财务管理
├── 发票确认        发票种类（正式/形式）、发票号、发票日期
├── 收款确认        收款日期、信用全款日期、收款状态
└── 形式发票转正    形式发票 → 正式发票（保留两组记录）
```

---

## 5. 车辆生命周期状态机（严格卡控 + 阶段确认）

系统以车辆主记录的 `lifecycle_stage`（生命周期阶段）作为流程卡控依据。每个阶段对应一张独立的阶段数据表与一次部门操作，**前置阶段未确认时，后续阶段入口不可用**（按钮置灰 + 后端二次校验）。

### 5.0 阶段确认机制（核心规则）

每个生命周期阶段都遵循统一的"录入 → 确认"两步：

1. **录入/暂存**：进入当前阶段后，负责部门可创建并反复修改本阶段数据（阶段表记录处于 `draft` 草稿状态）。
2. **确认（confirm）**：负责部门点击「确认」后，本阶段数据 **锁定不可再改**（记录变为 `confirmed`），同时车辆 `lifecycle_stage` 推进到下一阶段，下一阶段入口随之解锁。
3. **不可逆**：一旦确认，本阶段数据不可再编辑、不可重复确认。若确需修改已确认数据，需走单独的「撤回/退回」流程（本期暂不含，列入开放问题）。
4. **留痕**：每张阶段表都记录 **确认人 / 确认时间 + 创建人 / 创建时间 + 修改人 / 修改时间**。

> 卡控与锁定均以后端为准：后端在写入/确认时校验前置阶段已确认、本阶段未确认。

**阶段命名采用「待办态」视角**——状态名表达的是「该车当前等待什么操作」，方便一线人员按状态筛选待办工作：

```
[0] PENDING_OFFLINE        草稿（新录入）
        │ 生产专员创建车辆主记录（VIN/车型/颜色/发动机号…），可反复修改
        ▼
[1] PENDING_INBOUND        待入库
        │ 生产专员确认车辆录入后进入（生产数据锁定不可改）
        ▼
[2] PENDING_ALLOCATION     待分配
        │ 物流专员确认车厂到仓库（运输单整单确认）后进入
        ▼
[3] PENDING_INVOICE        待开票
        │ 销售专员确认车辆销售（分配经销商、分配日期）后进入
        ▼
[4] PENDING_PAYMENT        待收款
        │ 财务专员确认发票（正式发票 or 形式发票）后进入
        ├──（首张即正式发票 Invoice）──────────────► 可收款
        └──（首张为形式发票 Proforma）──► [4b] 形式发票转正（必做）──► 持有正式发票后可收款
        ▼
[5] PENDING_DELIVERY       待配送  ← 仅持有正式发票的车辆可进入（形式发票必须先转正）
        │ 财务专员确认收款（收款日期、收款状态）后进入
        ▼
[6] PENDING_REGISTRATION   待上牌
        │ 物流专员确认配送停靠签收（按经销商行签收确认）后进入
        ▼
[7] COMPLETED              已完结
          销售专员确认车辆上牌（Drosstech、注册日期、客户区域）后进入
```

### 5.1 卡控规则

| 操作 | 前置条件（车辆须处于的阶段） |
|------|------------------------------|
| 车辆录入确认 | PENDING_OFFLINE（新录入草稿） |
| 车厂到仓库（运输单加入） | VIN 处于 PENDING_INBOUND 且未被其他运输单占用；整单确认时校验 |
| 车辆销售 | PENDING_ALLOCATION（已入库） |
| 发票确认 | PENDING_INVOICE（已分配） |
| 形式发票转正 | PENDING_PAYMENT 且首张发票为「形式发票」 |
| 收款确认 | PENDING_PAYMENT 且持有**正式发票**（首张即正式发票，或形式发票已转正）。仅有形式发票、未转正的车辆不允许收款 |
| 仓库到经销商配送（VIN 加入经销商行） | VIN 处于 PENDING_DELIVERY 且属于该经销商；按经销商行确认签收时校验 |
| 车辆上牌 | PENDING_REGISTRATION（已配送签收） |

> 卡控校验在前端（入口禁用 + 提示）与后端（接口拦截 + 业务异常）双重实现。后端为准。

### 5.2 状态字段（来自字典管理）

| 业务状态 | 字典编码 | 取值（源自 Excel 样本） |
|----------|----------|--------------------------|
| 发票状态 Status1/Status2 | `invoice_status` | Invoiced（正式）、Proforma Invoiced（形式） |
| 收款状态 Payment Status | `payment_status` | Paid、Paid-Sinosure、Unpaid… |
| 配送状态 Delivery Status | `delivery_status` | Delivered、In Transit… |
| 上牌状态 Drosstech Status | `drosstech_status` | Uploaded、Pending… |
| 销售/分配状态 Remark3 | `sales_status` | Delivered、Allocated…（含信用条款备注） |

> 字典具体取值在「字典管理」中维护，上表为样本归纳；实际取值集合由业务在初始化时补全。

---

## 6. 数据模型设计

### 6.1 主数据表

**车型数据 `t_md_model`**（源自 DATA 表 B/C/D/E 列）

| 字段 | 说明 | Excel 来源 |
|------|------|-----------|
| id | 主键 | - |
| material_code | 物料编码 | DATA.物料编码 |
| series | 车系 | DATA.车系（如 MG4 EV） |
| spec | 配置规格 | DATA.SPEC（如 64KWh COM） |
| model_name | 车型名称（展示用） | Master.MODEL |
| model_code | 车型代码 | Master.MODEL CODE |
| year_make | 年款 | Master.Year Make |
| status | 启用/停用 | - |

**外饰颜色 `t_md_exterior_color`** / **内饰颜色 `t_md_interior_color`**

| 字段 | 说明 |
|------|------|
| id | 主键 |
| color_name | 颜色名称（英文，如 NUCLEAR YELLOW / BLACK） |
| color_name_cn | 颜色中文名（可选，如 DATA.内饰「黑色内饰…」） |
| status | 启用/停用 |

**经销商 `t_md_dealer`**（源自 Dealer Code 表）

| 字段 | 说明 | Excel 来源 |
|------|------|-----------|
| id | 主键 | - |
| dealer_code | 经销商编码 | Dealer Code.B（如 290933 / ASMY10024） |
| dealer_name | 经销商名称 | Dealer Code.A |
| status | 启用/停用 | - |

### 6.2 车辆主记录 `t_vehicle`（精简主表）

主表只保留标识与生命周期，**不存放各阶段业务字段**——各阶段数据拆到独立阶段表（见 6.3）。

| 字段 | 说明 |
|------|------|
| id | 主键 |
| vin | VIN，业务唯一键（UNIQUE） |
| lifecycle_stage | 当前生命周期阶段（枚举：PENDING_OFFLINE→PENDING_INBOUND→…→COMPLETED） |
| created_by / created_at | 创建人 / 创建时间 |
| updated_by / updated_at | 修改人 / 修改时间 |

> 主表的创建发生在「车辆录入」首次保存时（与生产阶段表一同落库，初始 `lifecycle_stage = PENDING_OFFLINE`，生产数据可反复修改）；车辆录入确认后推进至 PENDING_INBOUND，此后 `lifecycle_stage` 随每个阶段的确认动作逐级推进。

### 6.3 生命周期阶段表（每阶段一张，含确认与审计字段）

**统一公共字段**：每张阶段表都包含以下列，不再逐表重复列出——

```
id                主键
vehicle_id        FK t_vehicle（与 t_vehicle 一对一/一对零一）
stage_status      阶段记录状态：draft（草稿，可改） / confirmed（已确认，锁定）
confirmed_by      确认人        confirmed_at  确认时间
created_by        创建人        created_at    创建时间
updated_by        修改人        updated_at    修改时间
```

> 阶段表与 t_vehicle 为 1:1（一辆车每阶段至多一条），发票阶段为 1:N（见 6.4）。`stage_status=confirmed` 后该行业务字段不可再改；阶段确认即推进主表 `lifecycle_stage`。

各阶段表的业务字段（公共字段省略）：

**① 生产 `t_veh_production`**（B~N） — 初始 PENDING_OFFLINE，确认后置 PENDING_INBOUND
```
model_id(FK t_md_model), exterior_color_id(FK), interior_color_id(FK),
engine_number, year_make, material, shipment, batch,
offline_epmb_date, epmb_ok_date, remark1
```
> VIN 在 t_vehicle 主表；车型/颜色引用主数据。

**② 销售分配 `t_veh_allocation`**（R~U） — 确认后置 PENDING_INVOICE
```
allocated_date, dealer_id(FK t_md_dealer), sales_status(dict), remark3
```

**③ 发票 `t_veh_invoice`**（V~Y / AD~AG） — 见 6.4（1:N，确认后置 PENDING_PAYMENT）

**④ 收款 `t_veh_payment`**（Z~AC） — 确认后置 PENDING_DELIVERY（前置：持有正式发票）
```
payment_date, credit_full_payment_date, payment_status(dict), remark5
```

**⑤ 上牌 `t_veh_registration`**（AO~AS） — 确认后置 COMPLETED
```
drosstech_status(dict), upload_date, registration_date, customer_region, remark8
```

> 物流相关的车辆阶段数据（车厂到仓库、仓库到经销商配送）由物流单据驱动写入，表设计详见 6.5。

### 6.4 发票阶段表 `t_veh_invoice`（保留两组记录，1:N）

发票阶段是唯一一对多的阶段表，用于支持「形式发票 → 正式发票」留痕，一车至多两条（首次开票 + 转正记录）。除公共字段外：

| 字段 | 说明 | Excel 来源 |
|------|------|-----------|
| invoice_seq | 序号：1=首次开票，2=转正记录 | V~Y / AD~AG |
| invoice_type | 发票种类（正式/形式，字典） | Status1 / Status2 |
| invoice_no | 发票号 | Invoice# |
| invoice_date | 发票日期 | Invoice Date |
| remark | 备注 | remark4 / remark6 |

> 「发票确认」写 seq=1 并确认 → 推进 PENDING_PAYMENT；若 seq=1 为形式发票，「形式发票转正」新增 seq=2（正式发票）并确认，两条均保留。车辆「有效发票」取 seq 最大且为正式发票的记录。收款阶段校验该有效发票存在方可进入。

### 6.5 物流单据模型（批量配送）

物流采用「单据驱动」：一张单据组织多辆车的批量流转，确认动作作用于单据（或单据子项），并联动其下所有 VIN 直接推进车辆主表 `lifecycle_stage`。

> **与 6.3 阶段表的关系**：物流环节（车厂到仓库、仓库到经销商配送）对车辆是「批量维护」而非逐车维护，业务字段（SAIC buy off、到仓日期、ETD/ETA、签收、配送状态等）随单据保存在本节的物流单据表中，**不再单设 `veh_*` 阶段表**。本节单据仅承载物流业务数据并在确认时推进车辆生命周期；车辆在物流阶段的生命周期流转为：
> - 车厂到仓库运输单整单确认：`PENDING_INBOUND → PENDING_ALLOCATION`
> - 仓库到经销商按经销商行确认签收：`PENDING_DELIVERY → PENDING_REGISTRATION`

#### 6.5.1 车厂到仓库 — 运输单（1 单 N 车）

**运输单 `t_transport_order`**（表头）
| 字段 | 说明 | Excel 来源 |
|------|------|-----------|
| id | 主键 | - |
| order_no | 运输单号（系统生成或录入） | - |
| date_to_storage_yard | 到仓库日期（整单共享） | P 列 |
| remark2 | 备注2 | Q 列 |
| order_status | draft（可改）/ confirmed（锁定） | - |
| confirmed_by / confirmed_at | 确认人 / 确认时间 | - |
| created_by/at, updated_by/at | 创建、修改审计字段 | - |

**运输单明细 `t_transport_order_item`**（1 运输单 : N VIN）
| 字段 | 说明 | Excel 来源 |
|------|------|-----------|
| id | 主键 | - |
| transport_order_id | FK t_transport_order | - |
| vehicle_id | FK t_vehicle（仅可选 PENDING_INBOUND 的 VIN） | E 列 VIN |
| saic_buy_off_date | 该 VIN 的 SAIC buy off 日期（逐车填写） | O 列 |

> 运输单承载本阶段全部物流业务数据：`date_to_storage_yard`、`remark2` 在表头整单共享，`saic_buy_off_date` 在明细中逐车填写。
> **整单确认**：校验明细每辆 VIN 仍处 PENDING_INBOUND 且未被其他运输单占用 → 单上所有 VIN 一并推进 PENDING_ALLOCATION。确认后运输单与明细锁定。

#### 6.5.2 仓库到经销商 — 发车清单 / 行车路单 / 经销商行（三层）

**发车清单 `t_dispatch_list`**（表头，批次容器）
| 字段 | 说明 |
|------|------|
| id / dispatch_no | 主键 / 发车清单号 |
| list_status | draft / confirmed（清单可整体作废/关闭，配送推进不在此层） |
| created_by/at, updated_by/at | 审计字段 |

**行车路单 `t_waybill`**（1 发车清单 : N 路单，每张对应一辆轿运车）
| 字段 | 说明 | Excel 来源 |
|------|------|-----------|
| id / waybill_no | 主键 / 路单号 | - |
| dispatch_list_id | FK t_dispatch_list | - |
| trolly_type | 轿运车类型：4 units / 6 units | AJ 列 |
| fully_load | 是否满载 | AK 列 |

**行车路单-经销商行 `t_waybill_dealer`**（1 路单 : N 经销商）
| 字段 | 说明 | Excel 来源 |
|------|------|-----------|
| id | 主键 | - |
| waybill_id | FK t_waybill | - |
| dealer_id | FK t_md_dealer（该路单送达的一家经销商） | - |
| etd_to_dealer | 发车日期 | AH 列 |
| eta_to_dealer | 预计到达 | AI 列 |
| received_date | 经销商签收日期 | AL 列 |
| delivery_status | 配送状态（字典） | AM 列 |
| remark7 | 备注7 | AN 列 |
| row_status | draft / confirmed（按经销商行确认签收） | - |
| confirmed_by / confirmed_at | 确认人 / 确认时间 | - |

**经销商行-VIN `t_waybill_dealer_vin`**（1 经销商行 : N VIN）
| 字段 | 说明 |
|------|------|
| id | 主键 |
| waybill_dealer_id | FK t_waybill_dealer |
| vehicle_id | FK t_vehicle（仅可选该经销商已分配且 PENDING_DELIVERY 的 VIN） |

> 行车路单与经销商行承载本阶段全部物流业务数据：`trolly_type`、`fully_load` 在路单上（一辆轿运车），`etd/eta/received_date/delivery_status/remark7` 在经销商行上，由该行下挂的所有 VIN 共享。
> 一张行车路单（一辆轿运车）可承运多家经销商；每家经销商有各自的 ETD/ETA/签收日期/配送状态，及其要送的 VIN 列表。
> **按经销商行确认签收**：对某个 `t_waybill_dealer` 确认 → 校验其下每辆 VIN 处 PENDING_DELIVERY → 该经销商行下所有 VIN 一并推进 PENDING_REGISTRATION。同一路单上不同经销商行可在不同时间分别确认。

### 6.6 系统管理表

- `t_sys_user`（用户）、`t_sys_role`（角色）、`t_sys_user_role`（用户-角色）
- `t_sys_menu`（菜单：目录/菜单两级，含路由）、`t_sys_role_menu`（角色-菜单授权）
- `t_sys_dict_type`（字典类型）、`t_sys_dict_item`（字典项）

> 不设独立的操作审计日志表与功能；各业务表自带的创建人/创建时间、修改人/修改时间、确认人/确认时间字段已满足记录级留痕需要。


---

## 7. 功能需求明细

### 7.1 系统管理

| 模块 | 功能点 |
|------|--------|
| 用户管理 | 用户列表（分页/查询）、新增/编辑/删除、启用停用、重置密码、分配角色 |
| 角色管理 | 角色 CRUD、为角色授权菜单（仅菜单级授权，不含按钮/权限点） |
| 菜单管理 | 菜单树维护（目录/菜单两级），维护菜单时同时设置其路由；不维护按钮与权限点 |
| 字典管理 | 字典类型 CRUD、字典项 CRUD（销售/发票/收款/配送/上牌状态等） |

### 7.2 主数据管理

| 模块 | 功能点 |
|------|--------|
| 车型数据 | 车型 CRUD（物料编码、车系、SPEC、车型名、车型代码、年款）、启用停用、按车系/物料编码查询 |
| 内饰颜色 | 内饰颜色 CRUD、启用停用 |
| 外饰颜色 | 外饰颜色 CRUD、启用停用 |
| 经销商维护 | 经销商 CRUD（编码、名称）、启用停用、按编码/名称查询；归入主数据管理（源自 Dealer Code 表） |

### 7.3 生产管理 / 车辆录入

- 录入字段：VIN（唯一校验）、车型（选 t_md_model）、外饰颜色、内饰颜色、发动机号、年款、物料、Shipment、Batch、Offline EPMB、EPMB ok、备注
- **草稿 → 确认**：首次保存即创建车辆主记录（`lifecycle_stage = PENDING_OFFLINE`，`t_veh_production.stage_status=draft`），PENDING_OFFLINE 期间生产专员可反复修改；点「确认」后锁定记录，主表 `lifecycle_stage = PENDING_INBOUND`，记录确认人/确认时间
- VIN 重复校验；车型/颜色从主数据下拉选择
- 支持列表查询（按 VIN/车型/车系/阶段/经销商等多条件）

### 7.4 运输管理（单据驱动批量配送）

#### 7.4.1 车厂到仓库（运输单）

- 创建「运输单」：填写表头 `Date to Storage Yard`、`Remark2`
- 选择多辆 VIN 加入运输单明细（候选仅 PENDING_INBOUND 且未被其他运输单占用的车辆），为每辆 VIN 逐车填写 `SAIC buy off`
- 草稿态可增删 VIN、改表头与逐车 SAIC buy off
- **整单确认**：所有 VIN 一并推进 PENDING_ALLOCATION（物流业务数据随运输单保存）；确认后运输单锁定
- 列表查询：运输单查询、明细查看（明细除 VIN 外可显示车型/颜色/车型年等基本信息）

#### 7.4.2 仓库到经销商（发车清单 → 行车路单 → 经销商行）

- 创建「发车清单」作为批次容器
- 在发车清单下创建多张「行车路单」（每张对应一辆轿运车），设置 `Trolly type`（4/6 units）、是否满载
- 在每张行车路单上添加多家经销商行；每家经销商行：选择经销商、设置 `ETD/ETA to Dealer`、`Received date`、`Delivery Status`、`Remark7`，并选择该经销商要运输的 VIN（候选仅该经销商已分配且 PENDING_DELIVERY 的车辆）
- 草稿态可调整路单、经销商行及其 VIN
- **按经销商行确认签收**：该经销商行下所有 VIN 一并推进 PENDING_REGISTRATION（物流业务数据随经销商行保存）；同一路单不同经销商行可分别在不同时间确认
- 列表查询：发车清单 / 行车路单查询、按经销商查看在途与签收（VIN 列表可显示车型/颜色/车型年等基本信息）

### 7.5 销售管理

| 模块 | 字段 | 卡控（前置须已确认 → 本阶段确认后推进） |
|------|------|------|
| 车辆销售 | 分配日期、经销商（选 t_md_dealer，带出编码与名称）、销售状态、备注3 | 需 PENDING_ALLOCATION → 确认后置 PENDING_INVOICE |
| 车辆上牌 | Drosstech 状态、上传日期、注册日期、客户区域、备注8 | 需 PENDING_REGISTRATION → 确认后置 COMPLETED |

### 7.6 财务管理

| 模块 | 字段 | 卡控（前置须已确认 → 本阶段确认后推进） |
|------|------|------|
| 发票确认 | 发票种类（正式/形式）、发票号、发票日期、备注4 | 需 PENDING_INVOICE → 确认后置 PENDING_PAYMENT；写 invoice_seq=1 |
| 形式发票转正 | 正式发票号、发票日期、备注6 | 需 PENDING_PAYMENT 且首张为形式发票；新增并确认 invoice_seq=2，保留两组 |
| 收款确认 | 收款日期、信用全款日期、收款状态、备注5 | 需持有正式发票（形式发票须先转正）→ 确认后置 PENDING_DELIVERY |

> 所有阶段操作统一遵循 5.0 的「草稿可改 → 确认锁定 → 推进下一阶段」机制，确认后本阶段数据不可再改、不可重复确认。
>
> **车辆基本信息显示**：各生命周期阶段在涉及车辆（列表、明细、挂载选择等）时，除 VIN 外可适当显示车型、外饰/内饰颜色、车型年等基本信息，便于识别。这些信息实时取自 `t_vehicle` 主表与所引用的主数据，**不在阶段表或物流单据表中冗余存储**。
>
> **导出**：除车辆全景视图外，本期所有功能不提供导出；所有功能均提供列表查询。

### 7.7 全局功能

- **车辆全景视图**（仅业务主管可查看）：以 VIN 为入口，按车辆单页查看其全生命周期各环节的全部数据与时间线（聚合生产/销售/发票/收款/上牌阶段表 + 车厂到仓库、仓库到经销商物流单据，替代 Excel 横向 45 列），**支持导出**。

---

## 8. 非功能需求

| 维度 | 要求 |
|------|------|
| 权限 | RBAC，后端接口级鉴权；前端按菜单授权渲染（仅菜单级，无按钮/权限点） |
| 安全 | 密码加密存储；登录鉴权（Token/Session）；关键字段记录级留痕（创建/修改/确认人与时间） |
| 数据一致性 | VIN 唯一；生命周期阶段变更使用事务；卡控后端为准 |
| 可用性 | 列表分页与多条件查询；关键字段必填校验与友好提示 |
| 可维护性 | 状态值全部走字典，不硬编码；分层清晰 |
| 兼容性 | 主流浏览器（Chrome/Edge 最新版） |

---

## 9. 开放问题（待确认，不阻塞批准）

1. ~~**收款与形式发票的先后**~~ **【已确认】** 若首张为形式发票，必须先「形式发票转正」、持有正式发票后才能「收款确认」。仅有形式发票、未转正的车辆不允许收款。（注：Excel 第 5 行样本存在"形式发票已收款、之后才转正"的历史填法，本系统按新规则卡控，历史数据迁移时需做合规性校验。）
2. ~~**经销商归属**~~ **【已确认】** 经销商维护归入「主数据管理 / 经销商维护」，独立表 `t_md_dealer`。
3. **已确认数据的撤回/退回**：阶段一旦确认即锁定不可改。若现实中确认后发现填错，是否需要「撤回确认 / 退回上一阶段」流程及其审批与权限？（本期暂不含撤回功能，确认即终态；列为后续增强。）
4. **车型与颜色的关联**：DATA 表中车型与可选颜色存在对应关系，录入车辆时是否需按车型联动过滤可选颜色？（本期暂不联动，颜色为独立全集下拉。）
5. **历史数据迁移**：是否需要在本期提供 Excel 批量导入工具？（暂列为可选子任务。）
6. **多语言**：字段含大量英文（车型、颜色、状态），界面是否需要中英双语？（暂定中文界面 + 英文数据原值。）

---

## 10. 验收标准（概要）

- 七个生命周期阶段均可在系统中维护，且严格卡控生效（跳步操作被前后端双重拦截）
- 形式发票转正后，首张与转正两组发票记录均可查询
- 主数据（车型/内饰/外饰/经销商）可维护，车辆录入时引用选择
- RBAC 生效：不同角色登录后仅见授权菜单（菜单级授权，无按钮级权限）
- 各阶段涉及车辆处除 VIN 外可显示车型/颜色/车型年等基本信息（实时取主表与主数据，不冗余存储）
- 车辆全景视图仅业务主管可访问，按车辆完整呈现其全生命周期数据并可导出

