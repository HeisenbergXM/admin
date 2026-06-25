# 执行计划 — VLM 车辆全生命周期管理系统后端 API（优化版）

- **文档路径**：`docs/VLM-execution-plan.md`
- **基于**：D00001 系统设计 + D00002 前端页面设计
- **范围**：仅后端 Spring Boot REST API
- **执行方式**：分阶段实现，每阶段完成后人工检查确认，再执行下一阶段

---

## 0. 关键决策

| 决策项 | 结论 |
|--------|------|
| 实现范围 | 仅后端 Spring Boot REST API |
| 表名前缀 | 新表用 `t_` 前缀，现有 `sys_*` 表不变 |
| 现有日志功能 | 保留并增强（新业务表自带审计字段） |
| Lombok | 新代码统一使用 |
| 注入方式 | 构造器注入（与现有代码一致） |
| 现有 RBAC | 复用用户/角色/菜单体系，新增字典管理 |

## 1. 解耦设计原则

### 1.1 阶段间零直接依赖

各阶段 Service **不互相注入**，只通过以下两种松耦合方式协作：

```
                    ┌──────────────────────┐
                    │   LifecycleService   │  ← 唯一的阶段间协调者
                    │  (状态机 + 卡控引擎)  │
                    └──────────┬───────────┘
                               │
        ┌──────┬───────┬───────┼───────┬───────┬───────┐
        │      │       │       │       │       │       │
   Production Allocation Invoice Payment Delivery Registration
   Service   Service   Service Service Service  Service
        │      │       │       │       │       │       │
        └──────┴───────┴───────┴───────┴───────┴───────┘
              各自只依赖 LifecycleService + 自己的 Mapper
```

- **纵向**：每个阶段 Service 只注入 `LifecycleService` + 自己的 Mapper，不注入其他阶段 Service
- **横向**：阶段间不直接调用。A 阶段确认后由 `LifecycleService.advanceStage()` 推进主表 `lifecycle_stage`，B 阶段通过查询主表判断是否可操作
- **数据隔离**：每个阶段表独立，阶段间通过 `vehicle_id` 关联，不直接 JOIN 其他阶段表

### 1.2 LifecycleService 接口设计

```java
public interface LifecycleService {
    // 查询车辆当前阶段（供各阶段 Service 判断卡控）
    LifecycleStage getCurrentStage(Long vehicleId);

    // 校验车辆是否处于指定阶段（不匹配抛 BusinessException）
    void assertStage(Long vehicleId, LifecycleStage expected);

    // 校验车辆阶段在允许集合内
    void assertStageIn(Long vehicleId, Set<LifecycleStage> allowed);

    // 推进阶段（事务内调用，由调用方在同一事务中提交）
    void advanceStage(Long vehicleId, LifecycleStage from, LifecycleStage to);

    // 校验阶段记录未确认
    void assertNotConfirmed(String stageStatus);

    // 确认阶段记录（锁定 + 推进主表）
    void confirmAndAdvance(Long vehicleId, Object stageRecord,
                           LifecycleStage from, LifecycleStage to,
                           Runnable lockStageRecord);
}
```

### 1.3 每阶段统一文件结构

每个业务模块的文件结构完全自包含，删除任一模块不影响其他模块编译：

```
entity/          ← 该模块自己的实体
mapper/          ← 该模块自己的 Mapper
dto/request/     ← 该模块的请求 DTO
dto/response/    ← 该模块的响应 DTO
service/         ← 该模块的 Service 接口 + Impl
controller/      ← 该模块的 Controller
```

### 1.4 共享依赖（仅这些是跨阶段复用的）

| 共享组件 | 说明 | 所在阶段 |
|----------|------|---------|
| `LifecycleService` | 状态机 + 卡控 | 阶段 0 |
| `LifecycleStage` / `StageStatus` 枚举 | 阶段常量 | 阶段 0 |
| `SecurityUtils` | 获取当前登录用户 | 阶段 0 |
| `Vehicle` 实体 + `VehicleMapper` | 车辆主表 | 阶段 0 |
| `VehicleBasicInfo` 响应 DTO | 车辆基本信息（VIN/车型/颜色/年款） | 阶段 0 |
| `ErrorCode` 扩展 | VLM 业务错误码 | 阶段 0 |

---

## 2. 阶段定义

---

### 阶段 0：基础设施与生命周期引擎

**目标**：搭建 VLM 数据库 schema、枚举、卡控引擎、公共工具。此阶段是所有后续阶段的公共基础。

#### 新增文件清单

| # | 文件 | 说明 |
|---|------|------|
| 1 | `src/main/resources/sql/vlm_init.sql` | 全部 18 张新表的 DDL |
| 2 | `src/main/resources/sql/vlm_data.sql` | 菜单 + 字典 + 角色初始化数据 |
| 3 | `enums/LifecycleStage.java` | 8 个生命周期阶段枚举 |
| 4 | `enums/StageStatus.java` | DRAFT / CONFIRMED |
| 5 | `enums/OrderStatus.java` | 物流单据状态（复用 draft/confirmed） |
| 6 | `entity/Vehicle.java` | 车辆主表实体 |
| 7 | `mapper/VehicleMapper.java` | 主表 Mapper |
| 8 | `resources/mapper/vlm/VehicleMapper.xml` | 主表自定义 SQL |
| 9 | `dto/response/VehicleBasicInfo.java` | 车辆基本信息 DTO（VIN/车型/颜色/年款） |
| 10 | `service/VehicleBasicService.java` | 主表基础查询接口 |
| 11 | `service/impl/VehicleBasicServiceImpl.java` | 主表基础查询实现 |
| 12 | `service/LifecycleService.java` | 卡控引擎接口 |
| 13 | `service/impl/LifecycleServiceImpl.java` | 卡控引擎实现 |
| 14 | `util/SecurityUtils.java` | 获取当前登录用户 ID/用户名 |
| 15 | `common/ErrorCode.java`（修改） | 新增 VLM 业务错误码 |

#### DDL 内容（vlm_init.sql）

```sql
-- ========== 字典管理 ==========
CREATE TABLE t_sys_dict_type (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1正常',
    remark VARCHAR(200),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB COMMENT='字典类型';

CREATE TABLE t_sys_dict_item (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dict_type_id BIGINT NOT NULL,
    item_value VARCHAR(100) NOT NULL COMMENT '字典值',
    item_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    sort_order INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(200),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_dict_type_id (dict_type_id)
) ENGINE=InnoDB COMMENT='字典项';

-- ========== 主数据 ==========
CREATE TABLE t_md_model (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    material_code VARCHAR(50) COMMENT '物料编码',
    series VARCHAR(100) COMMENT '车系',
    spec VARCHAR(100) COMMENT '配置规格',
    model_name VARCHAR(100) COMMENT '车型名称',
    model_code VARCHAR(50) COMMENT '车型代码',
    year_make VARCHAR(20) COMMENT '年款',
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB COMMENT='车型数据';

CREATE TABLE t_md_exterior_color (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    color_name VARCHAR(100) NOT NULL,
    color_name_cn VARCHAR(100),
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB COMMENT='外饰颜色';

CREATE TABLE t_md_interior_color (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    color_name VARCHAR(100) NOT NULL,
    color_name_cn VARCHAR(100),
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB COMMENT='内饰颜色';

CREATE TABLE t_md_dealer (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dealer_code VARCHAR(50) NOT NULL,
    dealer_name VARCHAR(200) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_dealer_code (dealer_code)
) ENGINE=InnoDB COMMENT='经销商';

-- ========== 车辆主表 ==========
CREATE TABLE t_vehicle (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vin VARCHAR(17) NOT NULL COMMENT 'VIN',
    lifecycle_stage VARCHAR(30) NOT NULL COMMENT '当前生命周期阶段',
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_vin (vin)
) ENGINE=InnoDB COMMENT='车辆主表';

-- ========== 生命周期阶段表（公共字段：id, vehicle_id, stage_status, confirmed_by, confirmed_at, created_by, created_at, updated_by, updated_at）==========

CREATE TABLE t_veh_production (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    stage_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    model_id BIGINT, exterior_color_id BIGINT, interior_color_id BIGINT,
    engine_number VARCHAR(50), year_make VARCHAR(20), material VARCHAR(100),
    shipment VARCHAR(100), batch VARCHAR(100),
    offline_epmb_date DATE, epmb_ok_date DATE, remark1 VARCHAR(500),
    confirmed_by VARCHAR(50), confirmed_at DATETIME,
    created_by VARCHAR(50), created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50), updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='生产阶段';

CREATE TABLE t_veh_allocation (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    stage_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    allocated_date DATE, dealer_id BIGINT,
    sales_status VARCHAR(50), remark3 VARCHAR(500),
    confirmed_by VARCHAR(50), confirmed_at DATETIME,
    created_by VARCHAR(50), created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50), updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='销售分配阶段';

CREATE TABLE t_veh_invoice (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    stage_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    invoice_seq INT NOT NULL COMMENT '1=首次开票 2=转正记录',
    invoice_type VARCHAR(50) COMMENT '正式/形式',
    invoice_no VARCHAR(100), invoice_date DATE, remark VARCHAR(500),
    confirmed_by VARCHAR(50), confirmed_at DATETIME,
    created_by VARCHAR(50), created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50), updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    KEY idx_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='发票阶段(1:N)';

CREATE TABLE t_veh_payment (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    stage_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    payment_date DATE, credit_full_payment_date DATE,
    payment_status VARCHAR(50), remark5 VARCHAR(500),
    confirmed_by VARCHAR(50), confirmed_at DATETIME,
    created_by VARCHAR(50), created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50), updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='收款阶段';

CREATE TABLE t_veh_registration (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    stage_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    drosstech_status VARCHAR(50), upload_date DATE,
    registration_date DATE, customer_region VARCHAR(100), remark8 VARCHAR(500),
    confirmed_by VARCHAR(50), confirmed_at DATETIME,
    created_by VARCHAR(50), created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50), updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='上牌阶段';

-- ========== 物流单据 ==========
CREATE TABLE t_transport_order (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50),
    date_to_storage_yard DATE,
    remark2 VARCHAR(500),
    order_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    confirmed_by VARCHAR(50), confirmed_at DATETIME,
    created_by VARCHAR(50), created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50), updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_order_no (order_no)
) ENGINE=InnoDB COMMENT='运输单(车厂到仓库)';

CREATE TABLE t_transport_order_item (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    transport_order_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    saic_buy_off_date DATE,
    KEY idx_transport_order_id (transport_order_id),
    KEY idx_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='运输单明细';

CREATE TABLE t_dispatch_list (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dispatch_no VARCHAR(50),
    list_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by VARCHAR(50), created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50), updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_dispatch_no (dispatch_no)
) ENGINE=InnoDB COMMENT='发车清单';

CREATE TABLE t_waybill (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    waybill_no VARCHAR(50),
    dispatch_list_id BIGINT NOT NULL,
    trolly_type VARCHAR(20) COMMENT '4 units / 6 units',
    fully_load TINYINT DEFAULT 0,
    KEY idx_dispatch_list_id (dispatch_list_id)
) ENGINE=InnoDB COMMENT='行车路单';

CREATE TABLE t_waybill_dealer (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    waybill_id BIGINT NOT NULL,
    dealer_id BIGINT NOT NULL,
    etd_to_dealer DATE, eta_to_dealer DATE,
    received_date DATE, delivery_status VARCHAR(50), remark7 VARCHAR(500),
    row_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    confirmed_by VARCHAR(50), confirmed_at DATETIME,
    KEY idx_waybill_id (waybill_id)
) ENGINE=InnoDB COMMENT='行车路单-经销商行';

CREATE TABLE t_waybill_dealer_vin (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    waybill_dealer_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    KEY idx_waybill_dealer_id (waybill_dealer_id),
    KEY idx_vehicle_id (vehicle_id)
) ENGINE=InnoDB COMMENT='经销商行-VIN';
```

#### 验证步骤

| # | 检查项 | 方法 |
|---|--------|------|
| V0.1 | SQL 语法正确 | 在 MySQL 中执行 `vlm_init.sql`，无报错 |
| V0.2 | 初始化数据正确 | 执行 `vlm_data.sql`，查 `sys_menu` 确认新菜单、查 `t_sys_dict_type` 确认字典 |
| V0.3 | 项目编译通过 | `mvn compile` 无错误 |
| V0.4 | 应用启动正常 | `mvn spring-boot:run` 启动成功，访问 Knife4j `http://localhost:8899/doc.html` 正常 |
| V0.5 | 枚举正确 | 单元测试或代码检查：LifecycleStage 包含 8 个值，StageStatus 包含 2 个值 |
| V0.6 | LifecycleService 卡控逻辑 | 启动后通过 Knife4j 调用内部测试接口，或编写单元测试验证：正确推进、跳步拒绝、重复确认拒绝 |
| V0.7 | SecurityUtils | 登录后调用任意已认证接口，日志确认能正确取到当前用户 |

---

### 阶段 1：字典管理

**目标**：实现字典类型 + 字典项 CRUD，为后续所有字典下拉提供数据源。
**依赖**：仅依赖阶段 0 的 DDL（`t_sys_dict_type` / `t_sys_dict_item`）。
**与其他阶段耦合度**：零。其他阶段通过 `/api/dict/code/{dictCode}` 接口获取字典项，不直接注入 DictService。

#### 新增文件清单

| # | 文件 | 说明 |
|---|------|------|
| 1 | `entity/DictType.java` | @Data, @TableName("t_sys_dict_type") |
| 2 | `entity/DictItem.java` | @Data, @TableName("t_sys_dict_item") |
| 3 | `mapper/DictTypeMapper.java` | extends BaseMapper<DictType> |
| 4 | `mapper/DictItemMapper.java` | extends BaseMapper<DictItem> |
| 5 | `dto/request/DictTypeCreateRequest.java` | @NotBlank dictCode, dictName |
| 6 | `dto/request/DictTypeUpdateRequest.java` | |
| 7 | `dto/request/DictItemCreateRequest.java` | |
| 8 | `dto/request/DictItemUpdateRequest.java` | |
| 9 | `dto/request/DictTypeQueryRequest.java` | extends PageRequest |
| 10 | `service/DictService.java` | 接口 |
| 11 | `service/impl/DictServiceImpl.java` | 实现 |
| 12 | `controller/DictController.java` | REST API |

#### API 设计

```
GET    /api/dict/types                  — 字典类型分页列表（支持按编码/名称查询）
POST   /api/dict/types                  — 新增字典类型
PUT    /api/dict/types/{id}             — 编辑
DELETE /api/dict/types/{id}             — 删除
PUT    /api/dict/types/{id}/status      — 启停（body: {status: 0|1}）

GET    /api/dict/types/{typeId}/items   — 字典项列表
POST   /api/dict/items                  — 新增字典项
PUT    /api/dict/items/{id}             — 编辑
DELETE /api/dict/items/{id}             — 删除
PUT    /api/dict/items/{id}/status      — 启停

GET    /api/dict/code/{dictCode}        — 按编码获取字典项列表（前端下拉用）
```

#### 验证步骤

| # | 检查项 | 方法 |
|---|--------|------|
| V1.1 | 项目编译通过 | `mvn compile` |
| V1.2 | Knife4j 可见新接口 | 访问 `/doc.html`，确认 DictController 下 12 个接口出现 |
| V1.3 | 字典类型 CRUD | Knife4j 依次测试：新增→列表→编辑→启停→删除 |
| V1.4 | 字典项 CRUD | Knife4j 依次测试：在类型下新增项→列表→编辑→启停→删除 |
| V1.5 | 按编码查字典 | 调用 `GET /api/dict/code/invoice_status`，返回正确字典项 |
| V1.6 | 重复编码拒绝 | 新增相同 dict_code 的字典类型，返回错误 |
| V1.7 | 删除有子项的类型 | 删除包含字典项的类型，应拒绝或级联处理 |

---

### 阶段 2：主数据管理

**目标**：实现车型、外饰颜色、内饰颜色、经销商四个主数据模块的 CRUD。
**依赖**：仅依赖阶段 0 的 DDL（`t_md_model` 等 4 张表）。
**与其他阶段耦合度**：零。其他阶段通过 `/api/master-data/*/all` 接口获取下拉选项，不直接注入主数据 Service。

#### 新增文件清单（4 个模块 × 每模块 ~5 文件 = ~20 文件）

**车型数据**
| # | 文件 |
|---|------|
| 1 | `entity/VehicleModel.java` |
| 2 | `mapper/VehicleModelMapper.java` |
| 3 | `dto/request/ModelCreateRequest.java` + `ModelUpdateRequest.java` + `ModelQueryRequest.java` |
| 4 | `service/VehicleModelService.java` + `impl/VehicleModelServiceImpl.java` |
| 5 | `controller/VehicleModelController.java` |

**外饰颜色**（同上模式：Entity + Mapper + DTO×2 + Service + Controller）
**内饰颜色**（同上模式）
**经销商**（同上模式 + DealerQueryRequest 支持按编码/名称查询）

#### API 设计（统一模式）

```
# 车型（其余三个模块路径格式相同）
GET    /api/master-data/models              — 分页列表
GET    /api/master-data/models/all          — 全部启用项（下拉用）
POST   /api/master-data/models              — 新增
PUT    /api/master-data/models/{id}         — 编辑
DELETE /api/master-data/models/{id}         — 删除
PUT    /api/master-data/models/{id}/status  — 启停
GET    /api/master-data/models/series       — 车系去重列表（下拉用）

# 外饰颜色: /api/master-data/exterior-colors/...
# 内饰颜色: /api/master-data/interior-colors/...
# 经销商:   /api/master-data/dealers/...
```

#### 验证步骤

| # | 检查项 | 方法 |
|---|--------|------|
| V2.1 | 项目编译通过 | `mvn compile` |
| V2.2 | Knife4j 可见 4 个 Controller | 确认 ~24 个接口全部出现 |
| V2.3 | 车型 CRUD 全流程 | Knife4j：新增→列表查询（按车系/物料编码筛选）→编辑→启停→下拉接口→删除 |
| V2.4 | 颜色 CRUD | Knife4j：内饰/外饰各走一遍 CRUD + 启停 |
| V2.5 | 经销商 CRUD | Knife4j：新增→列表查询（按编码/名称搜索）→编辑→启停→删除 |
| V2.6 | 重复校验 | 经销商相同 dealer_code 新增，返回错误 |
| V2.7 | 下拉接口 | 调用 `/all` 接口，仅返回 status=1 的记录 |

---

### 阶段 3：车辆主记录 + 生产管理

**目标**：实现车辆主表基础查询 + 车辆录入（生产阶段）的完整 CRUD + 首次确认推进生命周期。
**依赖**：阶段 0（Vehicle 实体 + LifecycleService）+ 阶段 2（主数据下拉：车型/颜色）。
**与其他阶段耦合度**：仅依赖 LifecycleService 公共接口，不依赖其他阶段 Service。

#### 新增文件清单

| # | 文件 | 说明 |
|---|------|------|
| 1 | `dto/request/VehicleQueryRequest.java` | 多条件查询（VIN/车型/车系/阶段/经销商） |
| 2 | `dto/request/ProductionSaveRequest.java` | 生产数据保存 |
| 3 | `dto/request/VehicleCandidateRequest.java` | VIN 候选查询 |
| 4 | `dto/response/VehicleListResponse.java` | 车辆列表响应 |
| 5 | `dto/response/VehicleDetailResponse.java` | 车辆详情响应 |
| 6 | `dto/response/ProductionResponse.java` | 生产数据响应 |
| 7 | `mapper/VehicleMapper.xml`（修改） | 添加联查 SQL |
| 8 | `service/VehicleService.java` | 车辆查询 Service 接口 |
| 9 | `service/impl/VehicleServiceImpl.java` | 车辆查询实现 |
| 10 | `entity/VehProduction.java` | 生产阶段实体 |
| 11 | `mapper/VehProductionMapper.java` | 生产阶段 Mapper |
| 12 | `service/VehProductionService.java` | 生产 Service 接口 |
| 13 | `service/impl/VehProductionServiceImpl.java` | 生产 Service 实现 |
| 14 | `controller/VehicleController.java` | 车辆通用查询 API |
| 15 | `controller/VehProductionController.java` | 生产录入 API |

#### API 设计

```
# 车辆通用查询（跨阶段复用）
GET    /api/vehicles                          — 多条件分页查询
GET    /api/vehicles/{id}                     — 车辆详情
GET    /api/vehicles/{id}/basic-info          — 基本信息（VIN/车型/颜色/年款）
GET    /api/vehicles/candidates               — VIN 候选（按阶段过滤）
GET    /api/vehicles/check-vin?vin=xxx        — VIN 唯一性校验

# 生产管理
POST   /api/production                        — 创建车辆 + 生产数据（草稿）
PUT    /api/production/{vehicleId}            — 更新草稿
POST   /api/production/{vehicleId}/confirm    — 确认（→ PENDING_INBOUND）
GET    /api/production/{vehicleId}            — 获取生产数据
```

#### 验证步骤

| # | 检查项 | 方法 |
|---|--------|------|
| V3.1 | 项目编译通过 | `mvn compile` |
| V3.2 | 创建车辆草稿 | POST /api/production，检查 t_vehicle 和 t_veh_production 都有数据，lifecycle_stage=PENDING_OFFLINE, stage_status=DRAFT |
| V3.3 | VIN 唯一性 | 用相同 VIN 再次创建，返回 4002 错误 |
| V3.4 | VIN 异步查重 | GET /api/vehicles/check-vin?vin=已存在的，返回 false |
| V3.5 | 更新草稿 | PUT 修改生产数据，再次 GET 确认更新成功 |
| V3.6 | 确认录入 | POST /api/production/{id}/confirm，检查：production.stage_status=CONFIRMED，production.confirmed_by/at 有值，vehicle.lifecycle_stage=PENDING_INBOUND |
| V3.7 | 重复确认拒绝 | 再次 POST confirm，返回 4004 错误 |
| V3.8 | 确认后不可改 | PUT 修改已确认记录，返回 4004 错误 |
| V3.9 | 车辆列表查询 | GET /api/vehicles，验证按 VIN/车型/阶段筛选正常 |
| V3.10 | VIN 候选查询 | GET /api/vehicles/candidates?stage=PENDING_INBOUND，仅返回待入库车辆 |

---

### 阶段 4：运输管理 — 车厂到仓库（运输单）

**目标**：实现运输单（表头+VIN 明细）的创建、编辑、整单确认，确认后批量推进车辆生命周期。
**依赖**：阶段 0（LifecycleService）+ 阶段 3（Vehicle 主表）。不依赖阶段 1/2 的 Service。
**与其他阶段耦合度**：仅通过 `LifecycleService.assertStage(PENDING_INBOUND)` 校验车辆状态。

#### 新增文件清单

| # | 文件 |
|---|------|
| 1 | `entity/TransportOrder.java` |
| 2 | `entity/TransportOrderItem.java` |
| 3 | `mapper/TransportOrderMapper.java` + XML |
| 4 | `mapper/TransportOrderItemMapper.java` |
| 5 | `dto/request/TransportOrderSaveRequest.java` |
| 6 | `dto/request/TransportOrderQueryRequest.java` |
| 7 | `dto/request/TransportOrderItemRequest.java` |
| 8 | `dto/response/TransportOrderListResponse.java` |
| 9 | `dto/response/TransportOrderDetailResponse.java` |
| 10 | `service/TransportOrderService.java` + `impl` |
| 11 | `controller/TransportOrderController.java` |

#### API 设计

```
GET    /api/transport-orders                          — 列表（分页，支持单号/日期/状态查询）
POST   /api/transport-orders                          — 创建（草稿）
PUT    /api/transport-orders/{id}                     — 编辑表头
POST   /api/transport-orders/{id}/items               — 添加 VIN
DELETE /api/transport-orders/{id}/items/{itemId}      — 移除 VIN
PUT    /api/transport-orders/{id}/items/{itemId}      — 更新 SAIC buy off
POST   /api/transport-orders/{id}/confirm             — 整单确认
GET    /api/transport-orders/{id}                     — 详情
```

#### 验证步骤

| # | 检查项 | 方法 |
|---|--------|------|
| V4.1 | 项目编译通过 | `mvn compile` |
| V4.2 | 创建运输单 | POST 创建，检查 t_transport_order 数据，order_status=DRAFT |
| V4.3 | 添加 VIN 到明细 | POST items，检查 t_transport_order_item，验证仅 PENDING_INBOUND 车辆可选 |
| V4.4 | 添加非法 VIN | 添加非 PENDING_INBOUND 的 VIN，返回错误 |
| V4.5 | 更新 SAIC buy off | PUT items，检查逐车日期更新 |
| V4.6 | 移除 VIN | DELETE items，确认明细被删除 |
| V4.7 | 整单确认 | POST confirm（含 2+ VIN），检查：所有 VIN 的 lifecycle_stage → PENDING_ALLOCATION，运输单 order_status → confirmed，confirmed_by/at 有值 |
| V4.8 | 确认后锁定 | PUT 编辑已确认运输单，返回错误 |
| V4.9 | VIN 占用检测 | 将已被其他运输单占用的 VIN 添加到新运输单，返回 4005 |
| V4.10 | 列表查询 | GET 列表，确认单号/日期/状态筛选正常 |

---

### 阶段 5：销售管理 — 车辆销售

**目标**：实现车辆销售分配（选经销商、分配日期）的草稿/确认流程。
**依赖**：阶段 0（LifecycleService）+ 阶段 3（Vehicle 主表）。
**与其他阶段耦合度**：仅通过 `LifecycleService.assertStage(PENDING_ALLOCATION)` 校验。经销商数据通过 `t_md_dealer` 表直接查询（或通过 VehicleBasicService 取基本信息时 JOIN）。

#### 新增文件清单

| # | 文件 |
|---|------|
| 1 | `entity/VehAllocation.java` |
| 2 | `mapper/VehAllocationMapper.java` |
| 3 | `dto/request/AllocationSaveRequest.java` |
| 4 | `dto/request/AllocationQueryRequest.java` |
| 5 | `dto/response/AllocationResponse.java` |
| 6 | `service/VehAllocationService.java` + `impl` |
| 7 | `controller/VehAllocationController.java` |

#### API 设计

```
GET    /api/allocations                               — 列表
POST   /api/allocations/{vehicleId}                   — 创建草稿
PUT    /api/allocations/{vehicleId}                   — 更新草稿
POST   /api/allocations/{vehicleId}/confirm           — 确认（→ PENDING_INVOICE）
GET    /api/allocations/{vehicleId}                   — 获取数据
```

#### 验证步骤

| # | 检查项 | 方法 |
|---|--------|------|
| V5.1 | 项目编译通过 | `mvn compile` |
| V5.2 | 创建销售草稿 | POST /api/allocations/{vehicleId}（选一辆 PENDING_ALLOCATION 的车），检查 t_veh_allocation 数据 |
| V5.3 | 非 PENDING_ALLOCATION 拒绝 | 对非待分配车辆创建，返回 4003 |
| V5.4 | 更新草稿 | PUT 修改经销商/日期，GET 确认更新 |
| V5.5 | 确认 | POST confirm，检查：allocation.stage_status=CONFIRMED，vehicle.lifecycle_stage → PENDING_INVOICE |
| V5.6 | 重复确认拒绝 | 再次 confirm，返回 4004 |
| V5.7 | 列表查询 | GET 列表，验证按 VIN/经销商/阶段筛选 |

---

### 阶段 6：财务管理 — 发票确认 + 形式发票转正

**目标**：实现发票确认（写 seq=1）、形式发票转正（写 seq=2）的完整流程。
**依赖**：阶段 0（LifecycleService）。
**与其他阶段耦合度**：仅通过 `LifecycleService` 校验 PENDING_INVOICE / PENDING_PAYMENT。

#### 新增文件清单

| # | 文件 |
|---|------|
| 1 | `entity/VehInvoice.java` |
| 2 | `mapper/VehInvoiceMapper.java` + XML |
| 3 | `dto/request/InvoiceCreateRequest.java` |
| 4 | `dto/request/InvoiceConvertRequest.java` |
| 5 | `dto/request/InvoiceQueryRequest.java` |
| 6 | `dto/response/InvoiceResponse.java` |
| 7 | `dto/response/InvoiceListResponse.java` |
| 8 | `service/VehInvoiceService.java` + `impl` |
| 9 | `controller/VehInvoiceController.java` |

#### API 设计

```
GET    /api/invoices                                  — 列表（含有效发票判断）
POST   /api/invoices/{vehicleId}                      — 发票确认（seq=1，→ PENDING_PAYMENT）
PUT    /api/invoices/{id}                             — 更新草稿
POST   /api/invoices/{vehicleId}/convert              — 形式发票转正（seq=2）
GET    /api/invoices/{vehicleId}                      — 获取发票记录（1~2 条）
```

#### 验证步骤

| # | 检查项 | 方法 |
|---|--------|------|
| V6.1 | 项目编译通过 | `mvn compile` |
| V6.2 | 正式发票确认 | POST /api/invoices/{vehicleId}（正式发票，PENDING_INVOICE 的车），检查：invoice_seq=1，stage_status=CONFIRMED，vehicle → PENDING_PAYMENT |
| V6.3 | 形式发票确认 | POST 另一辆车（形式发票），检查 seq=1 为形式发票，vehicle → PENDING_PAYMENT |
| V6.4 | 非 PENDING_INVOICE 拒绝 | 对非待开票车辆开票，返回 4003 |
| V6.5 | 形式发票转正 | POST /api/invoices/{vehicleId}/convert（对 V6.3 的车），检查：新增 seq=2（正式发票），阶段仍 PENDING_PAYMENT |
| V6.6 | 转正前置校验 | 对首张为正式发票的车调转正，返回错误 |
| V6.7 | 非 PENDING_PAYMENT 转正拒绝 | 对非待收款车辆转正，返回错误 |
| V6.8 | 列表有效发票判断 | GET 列表，确认"有效发票"列正确显示（是/否/无） |
| V6.9 | 发票历史查询 | GET /api/invoices/{vehicleId}，确认转正车辆返回 2 条记录 |

---

### 阶段 7：财务管理 — 收款确认

**目标**：实现收款确认流程，核心是「持有正式发票才能收款」的校验。
**依赖**：阶段 0（LifecycleService）+ 阶段 6 的 VehInvoice 表数据（通过 Mapper 查询，不注入 InvoiceService）。
**与其他阶段耦合度**：通过 `LifecycleService.assertStage(PENDING_PAYMENT)` + 直接查询 `t_veh_invoice` 表判断是否持有正式发票。

> **解耦说明**：收款 Service 直接注入 `VehInvoiceMapper`（只读查询发票表），不注入 `VehInvoiceService`。这样删除发票模块只影响查询逻辑，不影响收款模块编译。或者，在 LifecycleService 中增加 `boolean hasValidFormalInvoice(Long vehicleId)` 方法，将发票校验逻辑集中在 LifecycleService 中。推荐后者。

#### 新增文件清单

| # | 文件 |
|---|------|
| 1 | `entity/VehPayment.java` |
| 2 | `mapper/VehPaymentMapper.java` |
| 3 | `dto/request/PaymentSaveRequest.java` |
| 4 | `dto/request/PaymentQueryRequest.java` |
| 5 | `dto/response/PaymentResponse.java` |
| 6 | `service/VehPaymentService.java` + `impl` |
| 7 | `controller/VehPaymentController.java` |

#### API 设计

```
GET    /api/payments                                  — 列表
POST   /api/payments/{vehicleId}                      — 创建草稿
PUT    /api/payments/{vehicleId}                      — 更新草稿
POST   /api/payments/{vehicleId}/confirm              — 确认（→ PENDING_DELIVERY）
GET    /api/payments/{vehicleId}                      — 获取数据
```

#### 验证步骤

| # | 检查项 | 方法 |
|---|--------|------|
| V7.1 | 项目编译通过 | `mvn compile` |
| V7.2 | 正式发票车辆收款 | POST 创建草稿 + confirm（对阶段 6 正式发票的车），检查：vehicle → PENDING_DELIVERY |
| V7.3 | 形式发票未转正拒绝收款 | POST confirm（对仅有形式发票、未转正的车），返回 4006 |
| V7.4 | 形式发票转正后可收款 | 先转正（阶段 6），再收款确认，成功 |
| V7.5 | 非 PENDING_PAYMENT 拒绝 | 对非待收款车辆操作，返回 4003 |
| V7.6 | 确认后锁定 | PUT 已确认记录，返回 4004 |
| V7.7 | 列表查询 | GET 列表，确认有效发票/收款状态筛选正常 |

---

### 阶段 8：运输管理 — 仓库到经销商（发车清单三层结构）

**目标**：实现发车清单 → 行车路单 → 经销商行 → VIN 的三层单据结构，以及按经销商行签收确认。
**依赖**：阶段 0（LifecycleService）。
**与其他阶段耦合度**：仅通过 `LifecycleService.assertStage(PENDING_DELIVERY)` 校验 VIN 状态。经销商信息通过 `DealerMapper` 只读查询。

#### 新增文件清单

| # | 文件 |
|---|------|
| 1 | `entity/DispatchList.java` |
| 2 | `entity/Waybill.java` |
| 3 | `entity/WaybillDealer.java` |
| 4 | `entity/WaybillDealerVin.java` |
| 5 | `mapper/DispatchListMapper.java` + XML |
| 6 | `mapper/WaybillMapper.java` |
| 7 | `mapper/WaybillDealerMapper.java` |
| 8 | `mapper/WaybillDealerVinMapper.java` |
| 9 | `dto/request/DispatchListSaveRequest.java` |
| 10 | `dto/request/WaybillSaveRequest.java` |
| 11 | `dto/request/WaybillDealerSaveRequest.java` |
| 12 | `dto/request/WaybillDealerConfirmRequest.java` |
| 13 | `dto/response/DispatchListResponse.java` |
| 14 | `dto/response/WaybillResponse.java` |
| 15 | `dto/response/WaybillDealerResponse.java` |
| 16 | `service/DispatchListService.java` + `impl` |
| 17 | `controller/DispatchListController.java` |

#### API 设计

```
# 发车清单
GET    /api/dispatch-lists                             — 列表（含签收进度）
POST   /api/dispatch-lists                             — 创建
GET    /api/dispatch-lists/{id}                        — 详情（三层嵌套）

# 行车路单
POST   /api/dispatch-lists/{dispatchId}/waybills       — 添加路单
PUT    /api/waybills/{id}                              — 编辑
DELETE /api/waybills/{id}                              — 删除

# 经销商行
POST   /api/waybills/{waybillId}/dealers               — 添加
PUT    /api/waybill-dealers/{id}                       — 编辑
DELETE /api/waybill-dealers/{id}                       — 删除
POST   /api/waybill-dealers/{id}/vins                  — 挂载 VIN
DELETE /api/waybill-dealers/{id}/vins/{vinId}          — 移除 VIN

# 签收确认
POST   /api/waybill-dealers/{id}/confirm               — 按行确认（VIN → PENDING_REGISTRATION）
```

#### 验证步骤

| # | 检查项 | 方法 |
|---|--------|------|
| V8.1 | 项目编译通过 | `mvn compile` |
| V8.2 | 创建发车清单 | POST 创建，检查 t_dispatch_list |
| V8.3 | 添加行车路单 | POST waybills，检查 t_waybill（trolly_type/fully_load） |
| V8.4 | 添加经销商行 | POST dealers，检查 t_waybill_dealer |
| V8.5 | 挂载 VIN | POST vins，验证仅 PENDING_DELIVERY 且属于该经销商的 VIN 可选 |
| V8.6 | 非法 VIN 拒绝 | 挂载非 PENDING_DELIVERY 或非该经销商的 VIN，返回错误 |
| V8.7 | 按经销商行签收确认 | POST confirm，检查：该行所有 VIN → PENDING_REGISTRATION，row_status=CONFIRMED |
| V8.8 | 不同经销商行分别确认 | 同一路单 2 个经销商行，分别在不同时间确认，各自 VIN 独立推进 |
| V8.9 | 确认后锁定 | 编辑已签收的经销商行，返回错误 |
| V8.10 | 详情三层嵌套 | GET /api/dispatch-lists/{id}，返回清单→路单→经销商行→VIN 完整嵌套结构 |
| V8.11 | 签收进度 | GET 列表，确认"未签收/已签收"计数正确 |

---

### 阶段 9：销售管理 — 车辆上牌

**目标**：实现车辆上牌登记（Drosstech/注册日期/客户区域）并确认完结生命周期。
**依赖**：阶段 0（LifecycleService）。
**与其他阶段耦合度**：仅通过 `LifecycleService.assertStage(PENDING_REGISTRATION)` 校验。

#### 新增文件清单

| # | 文件 |
|---|------|
| 1 | `entity/VehRegistration.java` |
| 2 | `mapper/VehRegistrationMapper.java` |
| 3 | `dto/request/RegistrationSaveRequest.java` |
| 4 | `dto/request/RegistrationQueryRequest.java` |
| 5 | `dto/response/RegistrationResponse.java` |
| 6 | `service/VehRegistrationService.java` + `impl` |
| 7 | `controller/VehRegistrationController.java` |

#### API 设计

```
GET    /api/registrations                              — 列表
POST   /api/registrations/{vehicleId}                  — 创建草稿
PUT    /api/registrations/{vehicleId}                  — 更新草稿
POST   /api/registrations/{vehicleId}/confirm          — 确认（→ COMPLETED）
GET    /api/registrations/{vehicleId}                  — 获取数据
```

#### 验证步骤

| # | 检查项 | 方法 |
|---|--------|------|
| V9.1 | 项目编译通过 | `mvn compile` |
| V9.2 | 创建上牌草稿 | POST（PENDING_REGISTRATION 的车），检查 t_veh_registration |
| V9.3 | 非 PENDING_REGISTRATION 拒绝 | 对非待上牌车辆操作，返回 4003 |
| V9.4 | 确认上牌 | POST confirm，检查：stage_status=CONFIRMED，vehicle.lifecycle_stage → COMPLETED |
| V9.5 | 重复确认拒绝 | 再次 confirm，返回 4004 |
| V9.6 | 列表查询 | GET 列表，验证按 VIN/经销商/阶段筛选 |

---

### 阶段 10：车辆全景视图 + 导出

**目标**：实现按 VIN 聚合全生命周期数据的只读视图 + Excel 导出。
**依赖**：所有阶段表已就绪（阶段 0~9）。
**与其他阶段耦合度**：仅通过各阶段 Mapper 只读查询，不注入任何阶段 Service。

#### 新增文件清单

| # | 文件 | 说明 |
|---|------|------|
| 1 | `dto/response/VehiclePanoramaResponse.java` | 聚合全景 DTO |
| 2 | `service/VehiclePanoramaService.java` | 全景 Service 接口 |
| 3 | `service/impl/VehiclePanoramaServiceImpl.java` | 聚合各 Mapper 查询 |
| 4 | `controller/VehiclePanoramaController.java` | 全景 API |
| 5 | `pom.xml`（修改） | 添加 EasyExcel 依赖 |

#### API 设计

```
GET    /api/panorama/{vin}                             — 车辆全景视图
GET    /api/panorama/{vin}/export                      — 导出 Excel
```

#### 全景数据结构

```json
{
  "vehicle": { "vin": "...", "lifecycleStage": "COMPLETED", "modelName": "...", ... },
  "timeline": [
    { "stage": "PENDING_OFFLINE", "confirmedBy": "...", "confirmedAt": "..." },
    { "stage": "PENDING_INBOUND", ... },
    ...
  ],
  "production": { ... },
  "transportOrder": { "orderNo": "...", "saicBuyOffDate": "...", ... },
  "allocation": { ... },
  "invoices": [ { "seq": 1, ... }, { "seq": 2, ... } ],
  "payment": { ... },
  "dispatch": { "waybills": [ { "dealers": [ { "vin": [...] } ] } ] },
  "registration": { ... }
}
```

#### 验证步骤

| # | 检查项 | 方法 |
|---|--------|------|
| V10.1 | 项目编译通过 | `mvn compile` |
| V10.2 | 全景视图查询 | GET /api/panorama/{vin}（用一辆 COMPLETED 的车），检查所有阶段数据完整返回 |
| V10.3 | 时间线完整 | 确认 timeline 包含 7 个已确认节点（录入→入库→分配→开票→收款→配送→上牌） |
| V10.4 | 发票两组记录 | 确认 invoices 数组包含 seq=1 和 seq=2（转正场景） |
| V10.5 | 未到达阶段 | 对只到 PENDING_INVOICE 的车查询，后续阶段字段为 null |
| V10.6 | VIN 不存在 | 查询不存在的 VIN，返回 404 / 4001 |
| V10.7 | Excel 导出 | GET /api/panorama/{vin}/export，浏览器下载 Excel，内容完整 |

---

### 阶段 11：权限配置 + 全链路收尾

**目标**：配置菜单权限、更新 SecurityConfig、全链路冒烟测试。

#### 修改文件清单

| # | 文件 | 说明 |
|---|------|------|
| 1 | `config/SecurityConfig.java`（检查） | 确认新 API 路径被认证规则覆盖（通常已覆盖，无需改动） |
| 2 | `resources/sql/vlm_data.sql`（检查） | 确认菜单/角色/字典初始化数据完整 |

#### 验证步骤

| # | 检查项 | 方法 |
|---|--------|------|
| V11.1 | 全链路冒烟 | 按顺序跑通一辆车的完整生命周期（见下方流程） |
| V11.2 | 跳步拦截 | 跳过某阶段直接操作后续阶段，返回 4003 |
| V11.3 | 多车并行 | 同时操作多辆不同阶段的车辆，互不影响 |
| V11.4 | 权限隔离 | 用不同角色登录，验证菜单级权限控制 |

**全链路冒烟流程**：
```
1. 创建车辆草稿 → 确认录入            [PENDING_OFFLINE → PENDING_INBOUND]
2. 创建运输单 → 加入该VIN → 整单确认   [PENDING_INBOUND → PENDING_ALLOCATION]
3. 销售分配经销商 → 确认               [PENDING_ALLOCATION → PENDING_INVOICE]
4a. 发票确认(正式) → 确认              [PENDING_INVOICE → PENDING_PAYMENT]
4b. (或) 发票确认(形式) → 转正         [PENDING_INVOICE → PENDING_PAYMENT(转正后)]
5. 收款确认 → 确认                     [PENDING_PAYMENT → PENDING_DELIVERY]
6. 发车清单 → 路单 → 经销商行 → 签收   [PENDING_DELIVERY → PENDING_REGISTRATION]
7. 上牌确认 → 确认                     [PENDING_REGISTRATION → COMPLETED]
8. 全景视图查询该VIN                   [全量数据 + 时间线]
```

---

## 3. 执行顺序总览

```
阶段 0（基础设施）  ─────────────────────────────────────►
                     │
         ┌───────────┼───────────┐
         ▼           ▼           ▼
   阶段 1（字典）  阶段 2（主数据）  ← 可并行，互不依赖
         │           │
         └─────┬─────┘
               ▼
         阶段 3（车辆 + 生产录入）
               │
               ▼
         阶段 4（运输单 · 车厂到仓库）
               │
               ▼
         阶段 5（销售分配）
               │
               ▼
         阶段 6（发票确认 + 形式转正）
               │
               ▼
         阶段 7（收款确认）
               │
               ▼
         阶段 8（发车清单 · 仓库到经销商）
               │
               ▼
         阶段 9（车辆上牌）
               │
               ▼
         阶段 10（全景视图 + 导出）
               │
               ▼
         阶段 11（权限 + 全链路收尾）
```

> 阶段 1 和阶段 2 可并行实现（互不依赖）。阶段 3 之后严格按车辆生命周期流转顺序实现。

## 4. 文件数量汇总

| 阶段 | 新增/修改文件 | 新增表 |
|------|-------------|-------|
| 0 - 基础设施 | ~15 | 18 |
| 1 - 字典管理 | ~12 | — |
| 2 - 主数据管理 | ~20 | — |
| 3 - 车辆 + 生产 | ~15 | — |
| 4 - 运输单 | ~11 | — |
| 5 - 销售分配 | ~7 | — |
| 6 - 发票 | ~9 | — |
| 7 - 收款 | ~7 | — |
| 8 - 发车清单 | ~17 | — |
| 9 - 上牌 | ~7 | — |
| 10 - 全景视图 | ~5 + pom.xml | — |
| 11 - 收尾 | ~2（检查/修改） | — |
| **合计** | **~127** | **18** |

## 5. 统一编码约定

- **包路径**：`com.company.admin.{entity/mapper/dto/service/controller/enums/util}`
- **表名前缀**：系统 `sys_`（现有），主数据 `t_md_`，车辆 `t_veh_`，物流 `t_transport_/t_dispatch_/t_waybill`，字典 `t_sys_dict_`
- **Lombok**：新实体和 DTO 统一 `@Data`；Service Impl 用 `@RequiredArgsConstructor`
- **审计字段填充**：扩展现有 `MyBatisConfig` 的 MetaObjectHandler，新增 `created_by`/`updated_by` 自动从 SecurityUtils 填充
- **事务**：阶段确认方法加 `@Transactional`，确保主表 + 阶段表原子更新
- **异常**：统一抛 `BusinessException(ErrorCode.XXX)`，由 `GlobalExceptionHandler` 处理
