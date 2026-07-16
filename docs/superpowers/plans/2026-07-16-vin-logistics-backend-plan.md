# D00003 VIN 单车物流后端 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在保留旧单据式运输管理全部表、代码、API 和权限的前提下，新增按 VIN 单车维护的入库与配送后端流程，并将全景视图和菜单入口切换到新流程。

**Architecture:** 新增 `VehInbound`、`VehDelivery` 两套与现有阶段模块同构的独立纵向切片，每套包含 Entity、DTO、Mapper/XML、Service、Controller；共享层仅调用现有 `LifecycleService` 和 `BusinessStatusLabelService`。数据库采用两张新阶段表，配送经销商实时关联已确认的 `t_veh_allocation`；全景响应以新增字段承载新数据，同时保留旧响应字段以降低兼容风险。

**Tech Stack:** Java 17、Spring Boot 2.7.18、Spring Security、MyBatis-Plus 3.5.5、MySQL/H2、JUnit 5、Mockito、EasyExcel 3.3.4、Maven。

## 实施后安全修订（优先于下方早期伪代码）

- 入库、配送的更新与确认写路径必须在公开的 `@Transactional` 方法中，先通过 Mapper 执行 `SELECT ... FOR UPDATE`（按唯一 `vehicle_id` 且 `deleted=0`）再检查 `stage_status` 并写入。下方使用普通 `selectOne`/`get*Entity` 的早期测试和伪代码仅保留为实施过程记录，不得作为最终实现依据。
- 并发创建没有可锁定的阶段行，由 `vehicle_id` 唯一键兜底；Service 捕获 `DuplicateKeyException` 并转换为明确的 `BAD_REQUEST`。
- 配送确认除 `receivedDate` 和已确认、未删除的 allocation 外，还必须重新校验持久化实体的 `ETA >= ETD`，且 `trollyType` 只能为 `null`、`4 units` 或 `6 units`。
- 全量 schema 的 `t_vehicle` 必须包含 `(lifecycle_stage, deleted, id)` 复合索引；增量迁移通过 `information_schema.statistics` 与动态 DDL 仅在索引不存在时创建，保证顺序重复执行安全。
- 全量 seed 可继续固定使用 `sys_role_menu.id=267..290`；增量迁移的 24 条角色菜单关联必须省略 `id`，只插入 `role_id/menu_id` 并用 `WHERE NOT EXISTS` 幂等保护，避免生产库主键占用冲突。
- 并发验收必须包含真实 H2 双事务证据：第二事务在首事务持有阶段行锁时等待，首事务提交确认后第二事务读取到 `CONFIRMED` 并拒绝陈旧更新。
- 本机 Maven 实际可执行路径为 `C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd`。

## Global Constraints

- 不删除、不修改旧六张物流表及其 `TransportOrder`、`DispatchList`、`Waybill` 相关 Java/API 行为。
- 不新增、不重命名 `LifecycleStage` 值；只使用既有 `PENDING_INBOUND → PENDING_ALLOCATION` 与 `PENDING_DELIVERY → PENDING_REGISTRATION`。
- 不修改 `LifecycleService` 现有方法签名或既有分支；新模块只调用它。
- `t_veh_delivery` 不得包含 `dealer_id`；经销商必须从已确认且未删除的 `t_veh_allocation` 读取。
- 草稿允许不完整；入库确认要求两个日期，配送确认要求签收日期和已确认的经销商分配。
- 不实现批量确认、撤回、旧数据迁移或全景旧数据回退。
- 旧页面菜单隐藏不能导致旧 API 权限消失：旧页面节点停用，旧按钮权限节点继续启用。
- 现有用户改动和未跟踪文件不得被覆盖；每次提交只包含当前任务明确列出的文件。
- 当前机器没有可执行的 `mvn`；计划中的 Maven 命令必须在 IDE Maven、CI 或已配置 Maven 的实施环境运行。

---

## File Structure

### 新增文件

- `src/main/java/com/company/admin/entity/VehInbound.java`：入库阶段实体。
- `src/main/java/com/company/admin/entity/VehDelivery.java`：配送阶段实体。
- `src/main/java/com/company/admin/dto/request/InboundSaveRequest.java`
- `src/main/java/com/company/admin/dto/request/InboundQueryRequest.java`
- `src/main/java/com/company/admin/dto/request/DeliverySaveRequest.java`
- `src/main/java/com/company/admin/dto/request/DeliveryQueryRequest.java`
- `src/main/java/com/company/admin/dto/response/InboundResponse.java`
- `src/main/java/com/company/admin/dto/response/DeliveryResponse.java`
- `src/main/java/com/company/admin/mapper/VehInboundMapper.java`
- `src/main/java/com/company/admin/mapper/VehDeliveryMapper.java`
- `src/main/resources/mapper/vlm/VehInboundMapper.xml`
- `src/main/resources/mapper/vlm/VehDeliveryMapper.xml`
- `src/main/java/com/company/admin/service/VehInboundService.java`
- `src/main/java/com/company/admin/service/VehDeliveryService.java`
- `src/main/java/com/company/admin/service/impl/VehInboundServiceImpl.java`
- `src/main/java/com/company/admin/service/impl/VehDeliveryServiceImpl.java`
- `src/main/java/com/company/admin/controller/VehInboundController.java`
- `src/main/java/com/company/admin/controller/VehDeliveryController.java`
- `src/main/resources/sql/d00003_vin_logistics_migration.sql`
- `src/test/java/com/company/admin/service/impl/VehInboundServiceImplTest.java`
- `src/test/java/com/company/admin/service/impl/VehDeliveryServiceImplTest.java`
- `src/test/java/com/company/admin/sql/VinLogisticsSchemaSqlTest.java`

### 修改文件

- `src/main/resources/sql/admin_system.sql`：新增两张阶段表、新菜单与权限；停用旧页面节点但保留旧按钮权限。
- `src/test/resources/sql/h2-mapper-smoke-schema.sql`：增加 H2 测试表。
- `src/test/java/com/company/admin/mapper/MapperSqlSmokeTest.java`：增加新 Mapper 的列表/筛选测试。
- `src/main/java/com/company/admin/dto/response/VehiclePanoramaResponse.java`：新增 `inbound`、`delivery`。
- `src/main/java/com/company/admin/service/impl/VehiclePanoramaServiceImpl.java`：全景聚合、时间线和导出切换到新表。
- `src/test/java/com/company/admin/service/impl/VehiclePanoramaServiceImplTest.java`：验证新数据源和不回退旧表。
- `src/test/java/com/company/admin/sql/VlmMenuSeedDataTest.java`：验证新旧菜单状态、权限可用性和角色授权。

---

### Task 1: 数据库阶段表与增量迁移骨架

**Files:**
- Create: `src/test/java/com/company/admin/sql/VinLogisticsSchemaSqlTest.java`
- Create: `src/main/resources/sql/d00003_vin_logistics_migration.sql`
- Modify: `src/main/resources/sql/admin_system.sql`

**Interfaces:**
- Produces: `t_veh_inbound(vehicle_id UNIQUE)`、`t_veh_delivery(vehicle_id UNIQUE)`。
- Preserves: 旧六张物流表的定义与数据段逐字保留。

- [ ] **Step 1: 写失败的 SQL 契约测试**

创建测试，读取全量脚本与增量脚本，固定新增表字段并禁止对旧表执行破坏性操作：

```java
package com.company.admin.sql;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VinLogisticsSchemaSqlTest {

    @Test
    void fullAndMigrationSqlDefineVinLevelLogisticsTables() throws IOException {
        String full = resource("/sql/admin_system.sql");
        String migration = resource("/sql/d00003_vin_logistics_migration.sql");

        for (String sql : new String[]{full, migration}) {
            assertTrue(sql.contains("t_veh_inbound"));
            assertTrue(sql.contains("saic_buy_off_date"));
            assertTrue(sql.contains("date_to_storage_yard"));
            assertTrue(sql.contains("t_veh_delivery"));
            assertTrue(sql.contains("etd_to_dealer"));
            assertTrue(sql.contains("eta_to_dealer"));
            assertTrue(sql.contains("trolly_type"));
            assertTrue(sql.contains("fully_load"));
            assertTrue(sql.contains("received_date"));
            assertTrue(sql.contains("delivery_status"));
        }
    }

    @Test
    void migrationDoesNotDropOrAlterLegacyLogisticsTables() throws IOException {
        String migration = resource("/sql/d00003_vin_logistics_migration.sql").toUpperCase();
        String[] legacyTables = {
                "T_TRANSPORT_ORDER", "T_TRANSPORT_ORDER_ITEM", "T_DISPATCH_LIST",
                "T_WAYBILL", "T_WAYBILL_DEALER", "T_WAYBILL_DEALER_VIN"
        };
        for (String table : legacyTables) {
            assertFalse(migration.contains("DROP TABLE " + table));
            assertFalse(migration.contains("ALTER TABLE " + table));
        }
    }

    private String resource(String path) throws IOException {
        var stream = getClass().getResourceAsStream(path);
        assertNotNull(stream, "Missing SQL resource: " + path);
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run: `mvn -Dtest=VinLogisticsSchemaSqlTest test`

Expected: FAIL，原因是 `d00003_vin_logistics_migration.sql` 不存在，且全量脚本不包含新表。

- [ ] **Step 3: 在全量脚本和增量脚本中加入精确 DDL**

两个脚本使用相同字段定义；增量脚本使用 `CREATE TABLE IF NOT EXISTS`，全量脚本沿用现有 dump 风格的 `DROP TABLE IF EXISTS` + `CREATE TABLE`：

```sql
CREATE TABLE IF NOT EXISTS `t_veh_inbound` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint NOT NULL COMMENT 'FK t_vehicle',
  `stage_status` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `saic_buy_off_date` date NULL DEFAULT NULL,
  `date_to_storage_yard` date NULL DEFAULT NULL,
  `remark2` varchar(500) NULL DEFAULT NULL,
  `confirmed_by` varchar(50) NULL DEFAULT NULL,
  `confirmed_at` datetime NULL DEFAULT NULL,
  `created_by` varchar(50) NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vehicle_id` (`vehicle_id`),
  KEY `idx_stage_status` (`stage_status`),
  KEY `idx_storage_date` (`date_to_storage_yard`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='按 VIN 入库阶段';

CREATE TABLE IF NOT EXISTS `t_veh_delivery` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint NOT NULL COMMENT 'FK t_vehicle',
  `stage_status` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `etd_to_dealer` date NULL DEFAULT NULL,
  `eta_to_dealer` date NULL DEFAULT NULL,
  `trolly_type` varchar(20) NULL DEFAULT NULL,
  `fully_load` tinyint NULL DEFAULT NULL,
  `received_date` date NULL DEFAULT NULL,
  `delivery_status` varchar(50) NULL DEFAULT NULL,
  `remark7` varchar(500) NULL DEFAULT NULL,
  `confirmed_by` varchar(50) NULL DEFAULT NULL,
  `confirmed_at` datetime NULL DEFAULT NULL,
  `created_by` varchar(50) NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(50) NULL DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vehicle_id` (`vehicle_id`),
  KEY `idx_stage_status` (`stage_status`),
  KEY `idx_eta_to_dealer` (`eta_to_dealer`),
  KEY `idx_received_date` (`received_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='按 VIN 配送阶段';
```

全量脚本中去掉 `IF NOT EXISTS`，并将新表放在其他 `t_veh_*` 阶段表附近。不要向配送表添加 `dealer_id`。

- [ ] **Step 4: 运行 SQL 契约测试**

Run: `mvn -Dtest=VinLogisticsSchemaSqlTest test`

Expected: PASS。

- [ ] **Step 5: 提交数据库骨架**

```bash
git add src/main/resources/sql/admin_system.sql src/main/resources/sql/d00003_vin_logistics_migration.sql src/test/java/com/company/admin/sql/VinLogisticsSchemaSqlTest.java
git commit -m "feat: add VIN logistics stage tables"
```

---

### Task 2: 入库数据契约与 Mapper

**Files:**
- Create: `src/main/java/com/company/admin/entity/VehInbound.java`
- Create: `src/main/java/com/company/admin/dto/request/InboundSaveRequest.java`
- Create: `src/main/java/com/company/admin/dto/request/InboundQueryRequest.java`
- Create: `src/main/java/com/company/admin/dto/response/InboundResponse.java`
- Create: `src/main/java/com/company/admin/mapper/VehInboundMapper.java`
- Create: `src/main/resources/mapper/vlm/VehInboundMapper.xml`
- Modify: `src/test/resources/sql/h2-mapper-smoke-schema.sql`
- Modify: `src/test/java/com/company/admin/mapper/MapperSqlSmokeTest.java`

**Interfaces:**
- Produces: `Page<InboundResponse> selectInboundPage(Page<InboundResponse>, InboundQueryRequest)`。
- Produces: `InboundResponse selectInboundByVehicleId(Long vehicleId)`。
- Response convention: 未建记录且车辆待入库时 `id=null`、`stageStatus=PENDING_INBOUND`。

- [ ] **Step 1: 先扩展 H2 schema 并写 Mapper 失败测试**

在 H2 脚本顶部增加 `DROP TABLE IF EXISTS t_veh_inbound;`，并加入：

```sql
CREATE TABLE t_veh_inbound (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL UNIQUE,
    stage_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    saic_buy_off_date DATE,
    date_to_storage_yard DATE,
    remark2 VARCHAR(500),
    confirmed_by VARCHAR(50),
    confirmed_at TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);
```

在 `MapperSqlSmokeTest` 注入 `VehInboundMapper`，增加三个测试：

```java
@Autowired
private VehInboundMapper vehInboundMapper;

@Test
void inboundDefaultPageIncludesPendingVehicleWithoutDraft() {
    jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (20, 'VIN00000000000020', 'PENDING_INBOUND', 0)");
    InboundQueryRequest request = new InboundQueryRequest();

    Page<InboundResponse> page = vehInboundMapper.selectInboundPage(new Page<>(1, 10), request);

    assertEquals(1, page.getRecords().size());
    assertEquals("PENDING_INBOUND", page.getRecords().get(0).getStageStatus());
}

@Test
void inboundPendingPageExposesSavedDraftAndVehicleMasterData() {
    jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (21, 'VIN00000000000021', 'PENDING_INBOUND', 0)");
    jdbcTemplate.update("INSERT INTO t_md_model (id, model_name, deleted) VALUES (210, 'MG4 EV', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_production (vehicle_id, stage_status, model_id, year_make, deleted) VALUES (21, 'CONFIRMED', 210, '2026', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_inbound (id, vehicle_id, stage_status, date_to_storage_yard, deleted) VALUES (211, 21, 'DRAFT', '2026-07-15', 0)");

    Page<InboundResponse> page = vehInboundMapper.selectInboundPage(new Page<>(1, 10), new InboundQueryRequest());

    InboundResponse row = page.getRecords().get(0);
    assertEquals("DRAFT", row.getStageStatus());
    assertEquals("MG4 EV", row.getModelName());
    assertEquals("2026", row.getYearMake());
}

@Test
void inboundConfirmedFilterFindsAdvancedVehicleByStorageDate() {
    jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (22, 'VIN00000000000022', 'PENDING_ALLOCATION', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_inbound (vehicle_id, stage_status, date_to_storage_yard, deleted) VALUES (22, 'CONFIRMED', '2026-07-14', 0)");
    InboundQueryRequest request = new InboundQueryRequest();
    request.setStageStatus("CONFIRMED");
    request.setStorageStartDate(LocalDate.of(2026, 7, 14));
    request.setStorageEndDate(LocalDate.of(2026, 7, 14));

    Page<InboundResponse> page = vehInboundMapper.selectInboundPage(new Page<>(1, 10), request);

    assertEquals(List.of(22L), page.getRecords().stream().map(InboundResponse::getVehicleId).toList());
}
```

补充 `InboundQueryRequest`、`InboundResponse` 和 `LocalDate` imports 后运行。

- [ ] **Step 2: 运行测试并确认 Mapper 类型不存在**

Run: `mvn -Dtest=MapperSqlSmokeTest test`

Expected: FAIL at compilation，缺少入库 DTO/Mapper。

- [ ] **Step 3: 创建入库 Entity 与 DTO**

`VehInbound` 使用 `@Data`、`@TableName("t_veh_inbound")`、`@TableId(type=AUTO)`、审计字段 `FieldFill` 和 `@TableLogic`，字段必须精确为：

```java
private Long id;
private Long vehicleId;
private String stageStatus;
private LocalDate saicBuyOffDate;
private LocalDate dateToStorageYard;
private String remark2;
private String confirmedBy;
private LocalDateTime confirmedAt;
private String createdBy;
private LocalDateTime createdAt;
private String updatedBy;
private LocalDateTime updatedAt;
private Integer deleted;
```

`InboundSaveRequest`：

```java
@Data
@Schema(description = "单车入库草稿请求")
public class InboundSaveRequest {
    private LocalDate saicBuyOffDate;
    private LocalDate dateToStorageYard;
    @Size(max = 500, message = "备注2长度不能超过500")
    private String remark2;
}
```

`InboundQueryRequest extends PageRequest`：

```java
private String vin;
private Long modelId;
private LocalDate storageStartDate;
private LocalDate storageEndDate;
private String stageStatus;
```

`InboundResponse` 返回以下字段及 Swagger 描述：

```java
private Long id;
private Long vehicleId;
private String vin;
private String lifecycleStage;
private String lifecycleStageLabel;
private String stageStatus;
private String stageStatusLabel;
private Long modelId;
private String modelName;
private String series;
private String spec;
private String modelCode;
private String yearMake;
private Long exteriorColorId;
private String exteriorColorName;
private Long interiorColorId;
private String interiorColorName;
private LocalDate saicBuyOffDate;
private LocalDate dateToStorageYard;
private String remark2;
private String confirmedBy;
private LocalDateTime confirmedAt;
```

- [ ] **Step 4: 创建 Mapper 接口和 XML**

Mapper 接口：

```java
@Mapper
public interface VehInboundMapper extends BaseMapper<VehInbound> {
    Page<InboundResponse> selectInboundPage(Page<InboundResponse> page,
                                             @Param("query") InboundQueryRequest query);
    InboundResponse selectInboundByVehicleId(@Param("vehicleId") Long vehicleId);
}
```

XML 使用公共列片段，连接 `t_vehicle`、`t_veh_inbound`、`t_veh_production` 和三张主数据表。关键状态分支必须是：

```xml
<choose>
    <when test="query.stageStatus == null or query.stageStatus == '' or query.stageStatus == 'PENDING_INBOUND'">
        AND v.lifecycle_stage = 'PENDING_INBOUND'
    </when>
    <when test="query.stageStatus == 'DRAFT' or query.stageStatus == 'CONFIRMED'">
        AND i.stage_status = #{query.stageStatus}
    </when>
    <otherwise>
        AND 1 = 0
    </otherwise>
</choose>
```

SELECT 至少包含：

```sql
i.id, v.id AS vehicle_id, v.vin,
v.lifecycle_stage,
COALESCE(i.stage_status, v.lifecycle_stage) AS stage_status,
p.model_id, m.model_name, m.series, m.spec, m.model_code, p.year_make,
p.exterior_color_id, ec.color_name AS exterior_color_name,
p.interior_color_id, ic.color_name AS interior_color_name,
i.saic_buy_off_date, i.date_to_storage_yard, i.remark2,
i.confirmed_by, i.confirmed_at
```

日期过滤使用闭区间：`i.date_to_storage_yard >= storageStartDate`、`<= storageEndDate`。详情查询复用同一列和 JOIN，条件为 `v.id=#{vehicleId} AND v.deleted=0`。

- [ ] **Step 5: 运行 Mapper 测试**

Run: `mvn -Dtest=MapperSqlSmokeTest test`

Expected: PASS，包含新增三个入库查询测试和所有既有 Mapper 测试。

- [ ] **Step 6: 提交入库数据访问层**

```bash
git add src/main/java/com/company/admin/entity/VehInbound.java src/main/java/com/company/admin/dto/request/InboundSaveRequest.java src/main/java/com/company/admin/dto/request/InboundQueryRequest.java src/main/java/com/company/admin/dto/response/InboundResponse.java src/main/java/com/company/admin/mapper/VehInboundMapper.java src/main/resources/mapper/vlm/VehInboundMapper.xml src/test/resources/sql/h2-mapper-smoke-schema.sql src/test/java/com/company/admin/mapper/MapperSqlSmokeTest.java
git commit -m "feat: add VIN inbound data access"
```

---

### Task 3: 入库 Service 与 REST API

**Files:**
- Create: `src/main/java/com/company/admin/service/VehInboundService.java`
- Create: `src/main/java/com/company/admin/service/impl/VehInboundServiceImpl.java`
- Create: `src/main/java/com/company/admin/controller/VehInboundController.java`
- Create: `src/test/java/com/company/admin/service/impl/VehInboundServiceImplTest.java`

**Interfaces:**
- `PageResult<InboundResponse> pageInbounds(InboundQueryRequest request)`
- `Long createInbound(Long vehicleId, InboundSaveRequest request)`
- `void updateInbound(Long vehicleId, InboundSaveRequest request)`
- `void confirmInbound(Long vehicleId)`
- `InboundResponse getInbound(Long vehicleId)`

- [ ] **Step 1: 写 Service 失败测试**

测试至少包含创建、确认和缺失必填项：

```java
@ExtendWith(MockitoExtension.class)
class VehInboundServiceImplTest {
    @Mock private VehInboundMapper vehInboundMapper;
    @Mock private LifecycleService lifecycleService;
    @Mock private BusinessStatusLabelService statusLabelService;
    @InjectMocks private VehInboundServiceImpl service;

    @Test
    void createInboundChecksStageAndStoresDraft() {
        doAnswer(invocation -> {
            VehInbound value = invocation.getArgument(0);
            value.setId(31L);
            return 1;
        }).when(vehInboundMapper).insert(any(VehInbound.class));

        Long id = service.createInbound(30L, completeRequest());

        assertEquals(31L, id);
        verify(lifecycleService).assertStage(30L, LifecycleStage.PENDING_INBOUND);
        ArgumentCaptor<VehInbound> captor = ArgumentCaptor.forClass(VehInbound.class);
        verify(vehInboundMapper).insert(captor.capture());
        assertEquals(StageStatus.DRAFT.name(), captor.getValue().getStageStatus());
    }

    @Test
    void confirmInboundRejectsMissingRequiredDates() {
        VehInbound inbound = new VehInbound();
        inbound.setVehicleId(30L);
        inbound.setStageStatus(StageStatus.DRAFT.name());
        when(vehInboundMapper.selectOne(any())).thenReturn(inbound);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.confirmInbound(30L));

        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        verify(lifecycleService, never()).confirmAndAdvance(any(), any(), any(), any(), any());
    }

    @Test
    void confirmInboundLocksAndAdvancesToAllocation() {
        VehInbound inbound = new VehInbound();
        inbound.setVehicleId(30L);
        inbound.setStageStatus(StageStatus.DRAFT.name());
        inbound.setSaicBuyOffDate(LocalDate.of(2026, 7, 14));
        inbound.setDateToStorageYard(LocalDate.of(2026, 7, 15));
        when(vehInboundMapper.selectOne(any())).thenReturn(inbound);
        doAnswer(invocation -> { invocation.<Runnable>getArgument(4).run(); return null; })
                .when(lifecycleService).confirmAndAdvance(
                        eq(30L), eq("DRAFT"), eq(LifecycleStage.PENDING_INBOUND),
                        eq(LifecycleStage.PENDING_ALLOCATION), any(Runnable.class));

        service.confirmInbound(30L);

        assertEquals("CONFIRMED", inbound.getStageStatus());
        assertNotNull(inbound.getConfirmedAt());
        verify(vehInboundMapper).updateById(inbound);
    }

    private InboundSaveRequest completeRequest() {
        InboundSaveRequest request = new InboundSaveRequest();
        request.setSaicBuyOffDate(LocalDate.of(2026, 7, 14));
        request.setDateToStorageYard(LocalDate.of(2026, 7, 15));
        return request;
    }
}
```

同一测试类再增加：重复创建返回 `BAD_REQUEST`；更新时调用 `assertStage(PENDING_INBOUND)` 和 `assertNotConfirmed`；详情为待处理车辆时正确应用生命周期/阶段中文标签。

- [ ] **Step 2: 运行测试并确认缺少 Service**

Run: `mvn -Dtest=VehInboundServiceImplTest test`

Expected: FAIL at compilation。

- [ ] **Step 3: 实现 Service 接口与核心事务**

实现类依赖仅为 `VehInboundMapper`、`LifecycleService`、`BusinessStatusLabelService`。核心逻辑：

```java
@Override
@Transactional
public Long createInbound(Long vehicleId, InboundSaveRequest request) {
    lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_INBOUND);
    if (findInbound(vehicleId) != null) {
        throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "车辆已存在入库记录");
    }
    VehInbound inbound = new VehInbound();
    copyFields(request, inbound);
    inbound.setVehicleId(vehicleId);
    inbound.setStageStatus(StageStatus.DRAFT.name());
    vehInboundMapper.insert(inbound);
    return inbound.getId();
}

@Override
@Transactional
public void updateInbound(Long vehicleId, InboundSaveRequest request) {
    VehInbound inbound = getInboundEntity(vehicleId);
    lifecycleService.assertNotConfirmed(inbound.getStageStatus());
    lifecycleService.assertStage(vehicleId, LifecycleStage.PENDING_INBOUND);
    copyFields(request, inbound);
    vehInboundMapper.updateById(inbound);
}

@Override
@Transactional
public void confirmInbound(Long vehicleId) {
    VehInbound inbound = getInboundEntity(vehicleId);
    lifecycleService.assertNotConfirmed(inbound.getStageStatus());
    if (inbound.getSaicBuyOffDate() == null || inbound.getDateToStorageYard() == null) {
        throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(),
                "SAIC buy off 日期和到中转仓库日期不能为空");
    }
    lifecycleService.confirmAndAdvance(vehicleId, inbound.getStageStatus(),
            LifecycleStage.PENDING_INBOUND, LifecycleStage.PENDING_ALLOCATION, () -> {
                inbound.setStageStatus(StageStatus.CONFIRMED.name());
                inbound.setConfirmedBy(SecurityUtils.getCurrentUsername());
                inbound.setConfirmedAt(LocalDateTime.now());
                vehInboundMapper.updateById(inbound);
            });
}
```

`pageInbounds` 调用 Mapper、逐条设置 `lifecycleStageLabel` 和 `stageStatusLabel`；`getInbound` 调用 `selectInboundByVehicleId`，若返回 null，或 `id=null` 且生命周期不是 `PENDING_INBOUND`，抛 `STAGE_DATA_NOT_FOUND`。

- [ ] **Step 4: 创建 Controller**

按 `VehPaymentController` 模式创建五个端点，分别使用：

```java
@RequestMapping("/api/inbounds")
@PreAuthorize("hasAuthority('vlm:inbound:list')")
@PreAuthorize("hasAuthority('vlm:inbound:add')")
@PreAuthorize("hasAuthority('vlm:inbound:edit')")
@PreAuthorize("hasAuthority('vlm:inbound:confirm')")
```

写操作增加 `@OpLog`：创建“创建单车入库草稿”、更新“更新单车入库草稿”、确认“确认单车入库”。请求体使用 `@Valid`。

- [ ] **Step 5: 运行入库单元测试和编译**

Run: `mvn -Dtest=VehInboundServiceImplTest test`

Expected: PASS。

Run: `mvn -DskipTests compile`

Expected: BUILD SUCCESS。

- [ ] **Step 6: 提交入库业务/API**

```bash
git add src/main/java/com/company/admin/service/VehInboundService.java src/main/java/com/company/admin/service/impl/VehInboundServiceImpl.java src/main/java/com/company/admin/controller/VehInboundController.java src/test/java/com/company/admin/service/impl/VehInboundServiceImplTest.java
git commit -m "feat: add VIN inbound workflow API"
```

---

### Task 4: 配送数据契约与 Mapper

**Files:**
- Create: `src/main/java/com/company/admin/entity/VehDelivery.java`
- Create: `src/main/java/com/company/admin/dto/request/DeliverySaveRequest.java`
- Create: `src/main/java/com/company/admin/dto/request/DeliveryQueryRequest.java`
- Create: `src/main/java/com/company/admin/dto/response/DeliveryResponse.java`
- Create: `src/main/java/com/company/admin/mapper/VehDeliveryMapper.java`
- Create: `src/main/resources/mapper/vlm/VehDeliveryMapper.xml`
- Modify: `src/test/resources/sql/h2-mapper-smoke-schema.sql`
- Modify: `src/test/java/com/company/admin/mapper/MapperSqlSmokeTest.java`

**Interfaces:**
- Produces: `Page<DeliveryResponse> selectDeliveryPage(Page<DeliveryResponse>, DeliveryQueryRequest)`。
- Produces: `DeliveryResponse selectDeliveryByVehicleId(Long vehicleId)`。
- Dealer source: `t_veh_allocation` must be `deleted=0 AND stage_status='CONFIRMED'`。

- [ ] **Step 1: 增加 H2 表和失败测试**

H2 表：

```sql
CREATE TABLE t_veh_delivery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL UNIQUE,
    stage_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    etd_to_dealer DATE,
    eta_to_dealer DATE,
    trolly_type VARCHAR(20),
    fully_load TINYINT,
    received_date DATE,
    delivery_status VARCHAR(50),
    remark7 VARCHAR(500),
    confirmed_by VARCHAR(50),
    confirmed_at TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);
```

同时为 H2 `t_md_dealer` 增加 `dealer_code VARCHAR(50)`，因为响应需要编码。增加测试：默认待配送包含未建草稿车辆；草稿字段回填；`dealerId` 只匹配已确认销售分配；`CONFIRMED` 可查询已推进车辆。

核心经销商断言：

```java
@Test
void deliveryPageReadsDealerFromConfirmedAllocation() {
    jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (40, 'VIN00000000000040', 'PENDING_DELIVERY', 0)");
    jdbcTemplate.update("INSERT INTO t_md_dealer (id, dealer_code, dealer_name, deleted) VALUES (400, 'D400', 'Phoenix Dealer', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_allocation (vehicle_id, stage_status, dealer_id, deleted) VALUES (40, 'CONFIRMED', 400, 0)");
    DeliveryQueryRequest request = new DeliveryQueryRequest();
    request.setDealerId(400L);

    Page<DeliveryResponse> page = vehDeliveryMapper.selectDeliveryPage(new Page<>(1, 10), request);

    assertEquals(1, page.getRecords().size());
    assertEquals("D400", page.getRecords().get(0).getDealerCode());
    assertEquals("Phoenix Dealer", page.getRecords().get(0).getDealerName());
}
```

- [ ] **Step 2: 运行并确认缺少配送类型**

Run: `mvn -Dtest=MapperSqlSmokeTest test`

Expected: FAIL at compilation。

- [ ] **Step 3: 创建配送 Entity 与 DTO**

`VehDelivery` 精确字段：

```java
private Long id;
private Long vehicleId;
private String stageStatus;
private LocalDate etdToDealer;
private LocalDate etaToDealer;
private String trollyType;
private Boolean fullyLoad;
private LocalDate receivedDate;
private String deliveryStatus;
private String remark7;
private String confirmedBy;
private LocalDateTime confirmedAt;
private String createdBy;
private LocalDateTime createdAt;
private String updatedBy;
private LocalDateTime updatedAt;
private Integer deleted;
```

`DeliverySaveRequest` 包含七个业务字段，`remark7` 使用 `@Size(max=500)`；日期和签收字段不加 `@NotNull`，保证草稿可不完整。

`DeliveryQueryRequest extends PageRequest`：`vin`、`modelId`、`dealerId`、`stageStatus`。

`DeliveryResponse` 包含与 `InboundResponse` 相同的车辆/状态字段，加上 `dealerId`、`dealerCode`、`dealerName`、七个配送字段、`deliveryStatusLabel`、确认人/时间。

- [ ] **Step 4: 创建配送 Mapper/XML**

Mapper 签名与入库对应。XML 的销售分配 JOIN 必须写成：

```sql
LEFT JOIN t_veh_allocation a
  ON a.vehicle_id = v.id
 AND a.deleted = 0
 AND a.stage_status = 'CONFIRMED'
LEFT JOIN t_md_dealer d ON d.id = a.dealer_id AND d.deleted = 0
```

不得 JOIN 或 SELECT `t_waybill*`。默认状态分支使用 `v.lifecycle_stage='PENDING_DELIVERY'`；`DRAFT/CONFIRMED` 使用 `dv.stage_status`。经销商筛选为 `a.dealer_id=#{query.dealerId}`。

- [ ] **Step 5: 运行全部 Mapper 测试**

Run: `mvn -Dtest=MapperSqlSmokeTest test`

Expected: PASS。

- [ ] **Step 6: 提交配送数据访问层**

```bash
git add src/main/java/com/company/admin/entity/VehDelivery.java src/main/java/com/company/admin/dto/request/DeliverySaveRequest.java src/main/java/com/company/admin/dto/request/DeliveryQueryRequest.java src/main/java/com/company/admin/dto/response/DeliveryResponse.java src/main/java/com/company/admin/mapper/VehDeliveryMapper.java src/main/resources/mapper/vlm/VehDeliveryMapper.xml src/test/resources/sql/h2-mapper-smoke-schema.sql src/test/java/com/company/admin/mapper/MapperSqlSmokeTest.java
git commit -m "feat: add VIN delivery data access"
```

---

### Task 5: 配送 Service 与 REST API

**Files:**
- Create: `src/main/java/com/company/admin/service/VehDeliveryService.java`
- Create: `src/main/java/com/company/admin/service/impl/VehDeliveryServiceImpl.java`
- Create: `src/main/java/com/company/admin/controller/VehDeliveryController.java`
- Create: `src/test/java/com/company/admin/service/impl/VehDeliveryServiceImplTest.java`

**Interfaces:**
- `PageResult<DeliveryResponse> pageDeliveries(DeliveryQueryRequest request)`
- `Long createDelivery(Long vehicleId, DeliverySaveRequest request)`
- `void updateDelivery(Long vehicleId, DeliverySaveRequest request)`
- `void confirmDelivery(Long vehicleId)`
- `DeliveryResponse getDelivery(Long vehicleId)`

- [ ] **Step 1: 写配送 Service 失败测试**

覆盖日期、车型值、经销商和阶段推进：

```java
@Test
void saveRejectsEtaBeforeEtd() {
    DeliverySaveRequest request = new DeliverySaveRequest();
    request.setEtdToDealer(LocalDate.of(2026, 7, 16));
    request.setEtaToDealer(LocalDate.of(2026, 7, 15));

    BusinessException ex = assertThrows(BusinessException.class,
            () -> service.createDelivery(50L, request));

    assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
}

@Test
void confirmRejectsVehicleWithoutConfirmedDealerAllocation() {
    when(vehDeliveryMapper.selectOne(any())).thenReturn(draftDelivery());
    when(vehAllocationMapper.selectOne(any())).thenReturn(null);

    BusinessException ex = assertThrows(BusinessException.class,
            () -> service.confirmDelivery(50L));

    assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
    verify(lifecycleService, never()).confirmAndAdvance(any(), any(), any(), any(), any());
}

@Test
void confirmDeliveryLocksAndAdvancesToRegistration() {
    VehDelivery delivery = draftDelivery();
    VehAllocation allocation = new VehAllocation();
    allocation.setDealerId(500L);
    allocation.setStageStatus(StageStatus.CONFIRMED.name());
    when(vehDeliveryMapper.selectOne(any())).thenReturn(delivery);
    when(vehAllocationMapper.selectOne(any())).thenReturn(allocation);
    doAnswer(invocation -> { invocation.<Runnable>getArgument(4).run(); return null; })
            .when(lifecycleService).confirmAndAdvance(
                    eq(50L), eq("DRAFT"), eq(LifecycleStage.PENDING_DELIVERY),
                    eq(LifecycleStage.PENDING_REGISTRATION), any(Runnable.class));

    service.confirmDelivery(50L);

    assertEquals("CONFIRMED", delivery.getStageStatus());
    assertNotNull(delivery.getConfirmedAt());
}
```

`draftDelivery()` 必须设置 `vehicleId=50L`、`stageStatus=DRAFT`、`receivedDate=2026-07-16`。另测 `trollyType="8 units"` 被拒绝、缺少 `receivedDate` 被拒绝、详情返回 `deliveryStatusLabel`。

- [ ] **Step 2: 运行并确认失败**

Run: `mvn -Dtest=VehDeliveryServiceImplTest test`

Expected: FAIL at compilation。

- [ ] **Step 3: 实现配送 Service**

依赖：`VehDeliveryMapper`、`VehAllocationMapper`、`LifecycleService`、`BusinessStatusLabelService`。保存前统一调用：

```java
private void validateDraft(DeliverySaveRequest request) {
    if (request.getEtdToDealer() != null && request.getEtaToDealer() != null
            && request.getEtaToDealer().isBefore(request.getEtdToDealer())) {
        throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "预计到达日期不能早于发车日期");
    }
    if (request.getTrollyType() != null
            && !"4 units".equals(request.getTrollyType())
            && !"6 units".equals(request.getTrollyType())) {
        throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "轿运车类型只能为4 units或6 units");
    }
}
```

确认时：

```java
@Override
@Transactional
public void confirmDelivery(Long vehicleId) {
    VehDelivery delivery = getDeliveryEntity(vehicleId);
    lifecycleService.assertNotConfirmed(delivery.getStageStatus());
    if (delivery.getReceivedDate() == null) {
        throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "经销商签收日期不能为空");
    }
    VehAllocation allocation = vehAllocationMapper.selectOne(
            new LambdaQueryWrapper<VehAllocation>()
                    .eq(VehAllocation::getVehicleId, vehicleId)
                    .eq(VehAllocation::getStageStatus, StageStatus.CONFIRMED.name())
                    .eq(VehAllocation::getDeleted, 0));
    if (allocation == null || allocation.getDealerId() == null) {
        throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "车辆未完成经销商分配");
    }
    lifecycleService.confirmAndAdvance(vehicleId, delivery.getStageStatus(),
            LifecycleStage.PENDING_DELIVERY, LifecycleStage.PENDING_REGISTRATION, () -> {
                delivery.setStageStatus(StageStatus.CONFIRMED.name());
                delivery.setConfirmedBy(SecurityUtils.getCurrentUsername());
                delivery.setConfirmedAt(LocalDateTime.now());
                vehDeliveryMapper.updateById(delivery);
            });
}
```

创建先调用 `assertStage(PENDING_DELIVERY)`；更新先查询记录并调用 `assertNotConfirmed`，再调用 `assertStage(PENDING_DELIVERY)`，确保已确认记录返回锁定错误、被另一入口推进的遗留草稿返回阶段冲突。详情和分页应用生命周期、阶段、`delivery_status` 三类中文标签。

- [ ] **Step 4: 创建配送 Controller**

接口前缀 `/api/deliveries`，权限为 `vlm:delivery:list/add/edit/confirm`，五个端点与入库完全对应。确认操作日志名称为“确认单车配送签收”。

- [ ] **Step 5: 运行配送测试和编译**

Run: `mvn -Dtest=VehDeliveryServiceImplTest test`

Expected: PASS。

Run: `mvn -DskipTests compile`

Expected: BUILD SUCCESS。

- [ ] **Step 6: 提交配送业务/API**

```bash
git add src/main/java/com/company/admin/service/VehDeliveryService.java src/main/java/com/company/admin/service/impl/VehDeliveryServiceImpl.java src/main/java/com/company/admin/controller/VehDeliveryController.java src/test/java/com/company/admin/service/impl/VehDeliveryServiceImplTest.java
git commit -m "feat: add VIN delivery workflow API"
```

---

### Task 6: 全景视图与导出切换到新阶段表

**Files:**
- Modify: `src/main/java/com/company/admin/dto/response/VehiclePanoramaResponse.java`
- Modify: `src/main/java/com/company/admin/service/impl/VehiclePanoramaServiceImpl.java`
- Modify: `src/test/java/com/company/admin/service/impl/VehiclePanoramaServiceImplTest.java`

**Interfaces:**
- Adds: `InboundResponse inbound`、`DeliveryResponse delivery`。
- Preserves: `TransportOrderDetailResponse transportOrder`、`DispatchListResponse dispatch` 属性仍存在。
- Changes: `getPanorama` 不再填充旧两个物流字段。

- [ ] **Step 1: 写失败的全景测试**

在测试类用 `VehInboundMapper`、`VehDeliveryMapper` 替换旧物流 Mapper mocks；保留旧 Mapper 类型无需注入。增加：

```java
@Mock private VehInboundMapper vehInboundMapper;
@Mock private VehDeliveryMapper vehDeliveryMapper;

@Test
void getPanoramaUsesVinLevelInboundAndDelivery() {
    Vehicle vehicle = new Vehicle();
    vehicle.setId(99L);
    vehicle.setVin("LSJW56U95RG000001");
    vehicle.setLifecycleStage(LifecycleStage.PENDING_REGISTRATION.name());
    VehicleBasicInfo basic = new VehicleBasicInfo();
    basic.setId(99L);
    basic.setVin(vehicle.getVin());
    InboundResponse inbound = new InboundResponse();
    inbound.setId(61L);
    inbound.setVehicleId(99L);
    inbound.setStageStatus("CONFIRMED");
    inbound.setConfirmedAt(LocalDateTime.of(2026, 7, 14, 10, 0));
    DeliveryResponse delivery = new DeliveryResponse();
    delivery.setId(62L);
    delivery.setVehicleId(99L);
    delivery.setStageStatus("CONFIRMED");
    delivery.setDeliveryStatus("DELIVERED");
    delivery.setConfirmedAt(LocalDateTime.of(2026, 7, 16, 10, 0));
    when(vehicleMapper.selectOne(any())).thenReturn(vehicle);
    when(vehicleMapper.selectBasicInfoById(99L)).thenReturn(basic);
    when(vehInboundMapper.selectInboundByVehicleId(99L)).thenReturn(inbound);
    when(vehDeliveryMapper.selectDeliveryByVehicleId(99L)).thenReturn(delivery);
    when(vehInvoiceMapper.selectList(any())).thenReturn(List.of());

    VehiclePanoramaResponse response = service.getPanorama(vehicle.getVin());

    assertEquals(inbound.getVehicleId(), response.getInbound().getVehicleId());
    assertEquals("DELIVERED", response.getDelivery().getDeliveryStatus());
    assertEquals(List.of("PENDING_ALLOCATION", "PENDING_REGISTRATION"),
            response.getTimeline().stream().map(VehiclePanoramaResponse.TimelineNode::getStage).toList());
    assertNull(response.getTransportOrder());
    assertNull(response.getDispatch());
}
```

增加导出测试，断言生成的 `ExportRow` 或导出字节非空并包含新分区；若直接解析 xlsx 成本过高，将 `toExportRows` 改为 package-private 并断言 `section` 包含 `inbound`、`delivery`。

- [ ] **Step 2: 运行并确认缺少新全景字段**

Run: `mvn -Dtest=VehiclePanoramaServiceImplTest test`

Expected: FAIL at compilation。

- [ ] **Step 3: 扩展响应并替换聚合依赖**

在 `VehiclePanoramaResponse` 新增：

```java
@Schema(description = "按 VIN 入库阶段")
private InboundResponse inbound;

@Schema(description = "按 VIN 配送阶段")
private DeliveryResponse delivery;
```

旧字段保留并增加 `@Deprecated` Java 注解与“兼容字段，新流程不再填充”的 Schema 说明。

在 Service 中删除旧物流 Mapper 构造依赖和 `fillTransport`、`fillDispatch` 调用，改为注入 `VehInboundMapper`、`VehDeliveryMapper` 并调用 `fillInbound`、`fillDelivery`。不要修改旧 Mapper 或旧 Service。

`fillInbound`/`fillDelivery` 分别调用 `selectInboundByVehicleId(vehicleId)`、`selectDeliveryByVehicleId(vehicleId)` 获取已经包含车辆与经销商信息的响应；返回 null 或 `id=null` 时不设置全景物流分区。确认记录分别调用：

```java
addTimeline(response, LifecycleStage.PENDING_ALLOCATION.name(), "车厂到中转仓库",
        inbound.getStageStatus(), inbound.getConfirmedBy(), inbound.getConfirmedAt());
addTimeline(response, LifecycleStage.PENDING_REGISTRATION.name(), "配送签收",
        delivery.getStageStatus(), delivery.getConfirmedBy(), delivery.getConfirmedAt());
```

- [ ] **Step 4: 更新全景标签与导出**

`applyLabels` 为新字段设置 `stageStatusLabel`、`lifecycleStageLabel`，配送额外设置 `deliveryStatusLabel`。`toExportRows` 增加：

```java
if (panorama.getInbound() != null) {
    addRow(rows, "inbound", "saicBuyOffDate", String.valueOf(panorama.getInbound().getSaicBuyOffDate()));
    addRow(rows, "inbound", "dateToStorageYard", String.valueOf(panorama.getInbound().getDateToStorageYard()));
    addRow(rows, "inbound", "confirmedBy", panorama.getInbound().getConfirmedBy());
    addRow(rows, "inbound", "confirmedAt", String.valueOf(panorama.getInbound().getConfirmedAt()));
}
if (panorama.getDelivery() != null) {
    addRow(rows, "delivery", "etdToDealer", String.valueOf(panorama.getDelivery().getEtdToDealer()));
    addRow(rows, "delivery", "etaToDealer", String.valueOf(panorama.getDelivery().getEtaToDealer()));
    addRow(rows, "delivery", "trollyType", panorama.getDelivery().getTrollyType());
    addRow(rows, "delivery", "fullyLoad", String.valueOf(panorama.getDelivery().getFullyLoad()));
    addRow(rows, "delivery", "receivedDate", String.valueOf(panorama.getDelivery().getReceivedDate()));
    addRow(rows, "delivery", "deliveryStatus", panorama.getDelivery().getDeliveryStatusLabel());
    addRow(rows, "delivery", "confirmedBy", panorama.getDelivery().getConfirmedBy());
    addRow(rows, "delivery", "confirmedAt", String.valueOf(panorama.getDelivery().getConfirmedAt()));
}
```

在 `addRow` 中允许 null 值，不抛异常。

- [ ] **Step 5: 运行全景和既有状态标签测试**

Run: `mvn -Dtest=VehiclePanoramaServiceImplTest,BusinessStatusLabelServiceImplTest test`

Expected: PASS。

- [ ] **Step 6: 提交全景适配**

```bash
git add src/main/java/com/company/admin/dto/response/VehiclePanoramaResponse.java src/main/java/com/company/admin/service/impl/VehiclePanoramaServiceImpl.java src/test/java/com/company/admin/service/impl/VehiclePanoramaServiceImplTest.java
git commit -m "feat: use VIN logistics in vehicle panorama"
```

---

### Task 7: 菜单切换、权限保留与角色授权

**Files:**
- Modify: `src/main/resources/sql/admin_system.sql`
- Modify: `src/main/resources/sql/d00003_vin_logistics_migration.sql`
- Modify: `src/test/java/com/company/admin/sql/VlmMenuSeedDataTest.java`

**Interfaces:**
- Visible pages: menu 133 `中转运输`、134 `发车清单`。
- Hidden legacy pages: 131、132 set `status=0`。
- Callable legacy API permissions: 1030–1033、1040–1043 remain `status=1`。
- New permissions: 1100–1103、1110–1113。

- [ ] **Step 1: 扩展菜单种子失败测试**

保留现有 `pageMenusHavePermissionCodes`，增加基于行内容的断言：

```java
@Test
void seedHidesLegacyPagesButKeepsLegacyApiPermissionsEnabled() throws IOException {
    String sql = seedSql();
    assertTrue(sql.contains("(131, 130, '车厂到仓库', 2, '/transport/inbound', 'vlm:transport:list', 'inbound', 121, 0,"));
    assertTrue(sql.contains("(132, 130, '仓库到经销商', 2, '/transport/outbound', 'vlm:dispatch:list', 'outbound', 122, 0,"));
    assertTrue(sql.contains("(1030, 131, '运输单查询', 3, NULL, 'vlm:transport:list', NULL, 1030, 1,"));
    assertTrue(sql.contains("(1043, 132, '发车清单确认', 3, NULL, 'vlm:dispatch:confirm', NULL, 1043, 1,"));
}

@Test
void seedDefinesNewVinLogisticsMenusAndPermissions() throws IOException {
    String sql = seedSql();
    assertTrue(sql.contains("'中转运输'"));
    assertTrue(sql.contains("'/transport/vin-inbound'"));
    assertTrue(sql.contains("'vlm:inbound:confirm'"));
    assertTrue(sql.contains("'/transport/vin-delivery'"));
    assertTrue(sql.contains("'vlm:delivery:confirm'"));
}

private String seedSql() throws IOException {
    InputStream stream = getClass().getResourceAsStream("/sql/admin_system.sql");
    assertNotNull(stream);
    return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
}
```

增加正则或精确 `sys_role_menu` 断言：物流专员 101 拥有 133、134 和八个按钮；业务主管 105 只拥有 133、134、1100、1110；不拥有 1101–1103、1111–1113。

- [ ] **Step 2: 运行并确认失败**

Run: `mvn -Dtest=VlmMenuSeedDataTest test`

Expected: FAIL，新菜单尚不存在，旧页面仍启用。

- [ ] **Step 3: 修改全量种子菜单**

- 将菜单 131、132 的 `status` 从 1 改为 0，其他列不变。
- 保持 1030–1033、1040–1043 的 `status=1` 和原角色映射不变。
- 新增页面 133、134，parent 均为 130。
- 新增按钮 1100–1103 parent=133，1110–1113 parent=134。
- `LOGISTICS_SPECIALIST(101)` 关联页面和全部新按钮。
- `BUSINESS_MANAGER(105)` 关联页面和两个 list 按钮。
- `ADMIN(1)` 显式关联 133、134、1100–1103、1110–1113，`sys_role_menu.id` 使用 267–276。
- `LOGISTICS_SPECIALIST(101)` 的十条关联使用 `sys_role_menu.id` 277–286。
- `BUSINESS_MANAGER(105)` 的 133、134、1100、1110 四条关联使用 `sys_role_menu.id` 287–290。

- [ ] **Step 4: 在增量迁移中加入幂等菜单 SQL**

使用固定主键 `INSERT ... ON DUPLICATE KEY UPDATE` 创建新节点；用：

```sql
UPDATE `sys_menu` SET `status` = 0 WHERE `id` IN (131, 132);
UPDATE `sys_menu` SET `status` = 1
WHERE `id` IN (1030,1031,1032,1033,1040,1041,1042,1043);
```

角色关联使用 `INSERT ... SELECT ... WHERE NOT EXISTS`，避免重复执行迁移时报唯一键冲突。迁移不得删除旧 `sys_role_menu` 行。

- [ ] **Step 5: 运行菜单与用户权限测试**

Run: `mvn -Dtest=VlmMenuSeedDataTest,MapperSqlSmokeTest test`

Expected: PASS；既有 ADMIN/菜单祖先测试继续通过。

- [ ] **Step 6: 提交菜单切换**

```bash
git add src/main/resources/sql/admin_system.sql src/main/resources/sql/d00003_vin_logistics_migration.sql src/test/java/com/company/admin/sql/VlmMenuSeedDataTest.java
git commit -m "feat: switch transport menus to VIN workflows"
```

---

### Task 8: 端到端回归与交付检查

**Files:**
- Verify only: all files under `src/main/java`, `src/main/resources`, `src/test/java`, `src/test/resources`。

**Interfaces:**
- No new code unless a test exposes a concrete defect.
- Acceptance gate: all old and new tests pass; old classes/tables/API routes remain present.

- [ ] **Step 1: 运行新功能定向测试**

Run:

```bash
mvn -Dtest=VinLogisticsSchemaSqlTest,VlmMenuSeedDataTest,MapperSqlSmokeTest,VehInboundServiceImplTest,VehDeliveryServiceImplTest,VehiclePanoramaServiceImplTest test
```

Expected: BUILD SUCCESS，0 failures，0 errors。

- [ ] **Step 2: 运行旧运输回归测试**

Run:

```bash
mvn -Dtest=TransportOrderServiceImplTest,DispatchListServiceImplTest test
```

Expected: BUILD SUCCESS；不得修改这两个测试的既有断言来获得通过。

- [ ] **Step 3: 运行完整测试套件**

Run: `mvn test`

Expected: BUILD SUCCESS，全部新旧测试通过。

- [ ] **Step 4: 做静态不回归检查**

Run:

```bash
git diff --name-status HEAD~7..HEAD
rg -n "class TransportOrder|class DispatchList|class Waybill" src/main/java/com/company/admin
rg -n "t_transport_order|t_dispatch_list|t_waybill_dealer_vin" src/main/resources/sql/admin_system.sql
rg -n "RequestMapping\(\"/api/(transport-orders|dispatch-lists)" src/main/java/com/company/admin/controller
```

Expected:

- diff 中不存在旧运输 Java 文件的删除或重命名。
- 旧实体、表和 Controller 路由仍能检索到。
- 新表中检索不到 `dealer_id`：`rg -n "dealer_id" src/main/resources/mapper/vlm/VehDeliveryMapper.xml` 只能命中销售分配 JOIN/SELECT，不得命中 `t_veh_delivery` 字段。

- [ ] **Step 5: 检查工作区和提交边界**

Run: `git status --short`

Expected: 当前功能提交无遗漏；用户原有的删除、D00003/D00004 和其他未跟踪文件保持原样，没有被误纳入任何提交。

---

## Final Acceptance Checklist

- [ ] `/api/inbounds` 五类接口和四个权限均存在。
- [ ] `/api/deliveries` 五类接口和四个权限均存在。
- [ ] 入库确认锁定记录并推进 `PENDING_INBOUND → PENDING_ALLOCATION`。
- [ ] 配送确认校验签收日期与已确认经销商分配，并推进 `PENDING_DELIVERY → PENDING_REGISTRATION`。
- [ ] 两张新表 `vehicle_id` 唯一，配送表没有 `dealer_id`。
- [ ] 全景响应/导出使用新阶段表，旧兼容字段仍存在但不填充。
- [ ] 导航只显示新页面，旧页面节点停用，旧 API 权限节点保持启用。
- [ ] 旧六张表、旧 Java/API 和旧测试全部保留。
- [ ] 完整 Maven 测试套件通过。
