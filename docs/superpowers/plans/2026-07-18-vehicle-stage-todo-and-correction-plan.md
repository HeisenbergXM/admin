# 车辆阶段待办收敛与管理修订 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让七个车辆业务列表只返回各自当前生命周期待办，并新增仅管理员可用的完整车辆数据修订 API、菜单权限和字段级审计。

**Architecture:** 普通列表继续使用原接口，但 Mapper 永久叠加固定 `lifecycle_stage` 条件；完整车辆列表迁移到独立的 `VehicleCorrection` 纵向切片。修订接口以聚合请求更新已有阶段记录，逐条加锁、校验归属并仅复制白名单业务字段；一个事务负责全部更新和结构化差异日志，普通阶段服务及其确认锁定规则保持不变。

**Tech Stack:** Java 17、Spring Boot 2.7.18、Spring Security Method Security、MyBatis-Plus 3.5.5、MySQL/H2、Bean Validation、JUnit 5、Mockito、MockMvc、Maven。

## Global Constraints

- 普通业务列表固定为：`PENDING_OFFLINE`、`PENDING_INBOUND`、`PENDING_ALLOCATION`、`PENDING_INVOICE`、`PENDING_PAYMENT`、`PENDING_DELIVERY`、`PENDING_REGISTRATION`。
- `lifecycleStage` 和 `stageStatus` 只可缩小结果集，不得查询出已离开当前阶段的车辆。
- 修订入口只更新已存在且未删除的阶段记录，不新增、不删除、不重新确认、不推进或回退生命周期。
- VIN、`lifecycleStage`、`stageStatus`、`invoiceSeq`、确认字段、创建/更新审计字段和删除标记不接受客户端写入。
- 独立权限固定为 `vlm:vehicle-correction:list` 和 `vlm:vehicle-correction:edit`，初始化时只授予 `ADMIN`（角色 ID 1）。
- 运输单、发车清单、行车路单等旧批量运输模块不在本次修改范围内。
- 使用现有依赖，不新增第三方库。
- 所有行为变更严格执行 TDD：先看到针对需求的失败，再写最小实现。
- 本机 Maven 命令固定使用 `C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd`。
- 工作区已有用户改动和未跟踪文件不得加入本功能提交；每次提交只暂存任务明确列出的文件。

---

## File Structure

### 新增文件

- `src/main/java/com/company/admin/dto/request/VehicleCorrectionUpdateRequest.java`：聚合修订请求和七类嵌套阶段白名单。
- `src/main/java/com/company/admin/service/VehicleCorrectionService.java`：完整列表、聚合详情、聚合修订接口。
- `src/main/java/com/company/admin/service/VehicleCorrectionAuditService.java`：将字段级前后差异写入现有操作日志表。
- `src/main/java/com/company/admin/service/impl/VehicleCorrectionServiceImpl.java`：查询编排、锁、归属校验、主数据/字典校验和事务更新。
- `src/main/java/com/company/admin/service/impl/VehicleCorrectionAuditServiceImpl.java`：结构化成功日志写入。
- `src/main/java/com/company/admin/controller/VehicleCorrectionController.java`：三条独立权限 API。
- `src/main/resources/sql/d00004_vehicle_stage_todo_correction_migration.sql`：幂等菜单和管理员授权迁移。
- `src/test/java/com/company/admin/dto/request/VehicleCorrectionUpdateRequestTest.java`：请求白名单与 Bean Validation 测试。
- `src/test/java/com/company/admin/service/impl/VehicleCorrectionServiceImplTest.java`：修订服务单元测试。
- `src/test/java/com/company/admin/service/impl/VehicleCorrectionServiceIntegrationTest.java`：真实事务、锁定 Mapper 和差异日志测试。
- `src/test/java/com/company/admin/controller/VehicleCorrectionControllerTest.java`：方法权限 403/200 测试。
- `src/test/java/com/company/admin/sql/VehicleCorrectionMenuSqlTest.java`：菜单、授权和迁移幂等契约测试。

### 修改文件

- `src/main/resources/mapper/vlm/VehicleMapper.xml`：生产待办查询、完整修订列表查询、车辆行锁。
- `src/main/resources/mapper/vlm/VehInboundMapper.xml`
- `src/main/resources/mapper/vlm/VehAllocationMapper.xml`
- `src/main/resources/mapper/vlm/VehInvoiceMapper.xml`
- `src/main/resources/mapper/vlm/VehPaymentMapper.xml`
- `src/main/resources/mapper/vlm/VehDeliveryMapper.xml`
- `src/main/resources/mapper/vlm/VehRegistrationMapper.xml`：其余六类固定生命周期查询及按 ID 行锁。
- `src/main/resources/mapper/vlm/VehProductionMapper.xml`：新增生产记录按 ID 行锁 SQL。
- `src/main/java/com/company/admin/mapper/VehicleMapper.java`：完整修订分页和车辆行锁签名。
- `src/main/java/com/company/admin/mapper/VehProductionMapper.java`
- `src/main/java/com/company/admin/mapper/VehInboundMapper.java`
- `src/main/java/com/company/admin/mapper/VehAllocationMapper.java`
- `src/main/java/com/company/admin/mapper/VehInvoiceMapper.java`
- `src/main/java/com/company/admin/mapper/VehPaymentMapper.java`
- `src/main/java/com/company/admin/mapper/VehDeliveryMapper.java`
- `src/main/java/com/company/admin/mapper/VehRegistrationMapper.java`：阶段记录按 ID 行锁签名。
- `src/main/java/com/company/admin/common/ErrorCode.java`：增加修订记录归属不匹配错误码。
- `src/main/resources/sql/admin_system.sql`：修订菜单、按钮和管理员角色关联。
- `src/test/resources/sql/h2-mapper-smoke-schema.sql`：补足修订集成测试需要的字段和操作日志表。
- `src/test/java/com/company/admin/mapper/MapperSqlSmokeTest.java`：七类待办过滤回归测试。
- `docs/api-document.md`：更新列表语义并记录三条修订 API。

---

### Task 1: 收紧七个普通业务列表

**Files:**
- Modify: `src/test/java/com/company/admin/mapper/MapperSqlSmokeTest.java`
- Modify: `src/main/resources/mapper/vlm/VehicleMapper.xml`
- Modify: `src/main/resources/mapper/vlm/VehInboundMapper.xml`
- Modify: `src/main/resources/mapper/vlm/VehAllocationMapper.xml`
- Modify: `src/main/resources/mapper/vlm/VehInvoiceMapper.xml`
- Modify: `src/main/resources/mapper/vlm/VehPaymentMapper.xml`
- Modify: `src/main/resources/mapper/vlm/VehDeliveryMapper.xml`
- Modify: `src/main/resources/mapper/vlm/VehRegistrationMapper.xml`

**Interfaces:**
- Preserves: 所有现有分页方法签名和响应 DTO。
- Changes: 每个分页方法无条件叠加自身固定生命周期。
- Compatibility: 匹配当前页面的 `DRAFT` 可继续缩小到草稿；`CONFIRMED` 不能命中已经推进的车辆。

- [ ] **Step 1: 把现有“确认后仍可查”测试改成失败的“确认后不可查”测试**

在 `MapperSqlSmokeTest` 注入 `VehicleMapper`，并将已有 `inboundConfirmedFilterFindsAdvancedVehicleByStorageDate`、`deliveryConfirmedFilterFindsAdvancedVehicle` 改为断言空列表；补充生产、销售分配、发票、收款、上牌用例：

```java
@Autowired
private VehicleMapper vehicleMapper;

@Test
void vehiclePageOnlyReturnsPendingOfflineEvenWhenCompletedIsRequested() {
    jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (60, 'VIN00000000000060', 'PENDING_OFFLINE', 0)");
    jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (61, 'VIN00000000000061', 'PENDING_INBOUND', 0)");
    VehicleQueryRequest request = new VehicleQueryRequest();
    request.setLifecycleStage("PENDING_INBOUND");

    Page<VehicleListResponse> page = vehicleMapper.selectVehiclePage(new Page<>(1, 10), request);

    assertTrue(page.getRecords().isEmpty());
}

@Test
void inboundConfirmedFilterCannotFindAdvancedVehicle() {
    jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (62, 'VIN00000000000062', 'PENDING_ALLOCATION', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_inbound (vehicle_id, stage_status, deleted) VALUES (62, 'CONFIRMED', 0)");
    InboundQueryRequest request = new InboundQueryRequest();
    request.setStageStatus("CONFIRMED");

    assertTrue(vehInboundMapper.selectInboundPage(new Page<>(1, 10), request).getRecords().isEmpty());
}

@Test
void everyStagePageExcludesVehiclesThatAlreadyAdvanced() {
    jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (63, 'VIN00000000000063', 'PENDING_INVOICE', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_allocation (vehicle_id, stage_status, deleted) VALUES (63, 'CONFIRMED', 0)");
    jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (64, 'VIN00000000000064', 'PENDING_PAYMENT', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_invoice (vehicle_id, stage_status, invoice_seq, deleted) VALUES (64, 'CONFIRMED', 1, 0)");
    jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (65, 'VIN00000000000065', 'PENDING_DELIVERY', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_payment (vehicle_id, stage_status, deleted) VALUES (65, 'CONFIRMED', 0)");
    jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (66, 'VIN00000000000066', 'COMPLETED', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_registration (vehicle_id, stage_status, deleted) VALUES (66, 'CONFIRMED', 0)");

    AllocationQueryRequest allocation = new AllocationQueryRequest();
    allocation.setStageStatus("CONFIRMED");
    PaymentQueryRequest payment = new PaymentQueryRequest();
    payment.setStageStatus("CONFIRMED");
    RegistrationQueryRequest registration = new RegistrationQueryRequest();
    registration.setStageStatus("CONFIRMED");

    assertTrue(vehAllocationMapper.selectAllocationPage(new Page<>(1, 10), allocation).getRecords().isEmpty());
    assertTrue(vehInvoiceMapper.selectInvoicePage(new Page<>(1, 10), new InvoiceQueryRequest()).getRecords().isEmpty());
    assertTrue(vehPaymentMapper.selectPaymentPage(new Page<>(1, 10), payment).getRecords().isEmpty());
    assertTrue(vehRegistrationMapper.selectRegistrationPage(new Page<>(1, 10), registration).getRecords().isEmpty());
}
```

同时保留并运行已有“当前阶段无草稿也可见”和“当前阶段草稿可见”用例，证明收紧没有误删待办。

- [ ] **Step 2: 运行 Mapper 测试并确认旧查询泄露历史数据**

Run:

```powershell
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=MapperSqlSmokeTest test
```

Expected: FAIL；至少入库、配送、销售分配、发票或上牌的高级阶段车辆仍被查出，生产列表也没有固定 `PENDING_OFFLINE`。

- [ ] **Step 3: 在每个 Mapper SQL 中永久叠加固定生命周期**

`VehicleMapper.xml` 的 `selectVehiclePage` 在 `WHERE v.deleted = 0` 后加入：

```xml
AND v.lifecycle_stage = 'PENDING_OFFLINE'
<if test="query.lifecycleStage != null and query.lifecycleStage != ''">
    AND v.lifecycle_stage = #{query.lifecycleStage}
</if>
```

其余 Mapper 都先无条件加入固定阶段，再把 `stageStatus` 变成只针对当前结果集的附加条件。以入库为完整范式：

```xml
AND v.lifecycle_stage = 'PENDING_INBOUND'
<choose>
    <when test="query.stageStatus == null or query.stageStatus == '' or query.stageStatus == 'PENDING_INBOUND'"/>
    <when test="query.stageStatus == 'DRAFT' or query.stageStatus == 'CONFIRMED'">
        AND i.stage_status = #{query.stageStatus}
    </when>
    <otherwise>
        AND 1 = 0
    </otherwise>
</choose>
```

分别使用别名 `a`、`p`、`dv`、`r` 应用到销售分配、收款、配送、上牌。发票把：

```sql
AND v.lifecycle_stage IN ('PENDING_INVOICE', 'PENDING_PAYMENT')
```

替换为：

```sql
AND v.lifecycle_stage = 'PENDING_INVOICE'
```

保留现有 VIN、车型、经销商、日期和有效正式发票筛选，不修改 JOIN 或响应列。

- [ ] **Step 4: 运行 Mapper 测试并确认通过**

Run: 同 Step 2。

Expected: BUILD SUCCESS；当前阶段待办/草稿测试通过，所有确认后不可查测试通过。

- [ ] **Step 5: 提交待办查询收敛**

```powershell
git add src/test/java/com/company/admin/mapper/MapperSqlSmokeTest.java src/main/resources/mapper/vlm/VehicleMapper.xml src/main/resources/mapper/vlm/VehInboundMapper.xml src/main/resources/mapper/vlm/VehAllocationMapper.xml src/main/resources/mapper/vlm/VehInvoiceMapper.xml src/main/resources/mapper/vlm/VehPaymentMapper.xml src/main/resources/mapper/vlm/VehDeliveryMapper.xml src/main/resources/mapper/vlm/VehRegistrationMapper.xml
git commit -m "fix: scope vehicle lists to current lifecycle stage"
```

---

### Task 2: 建立完整车辆修订只读 API

**Files:**
- Create: `src/main/java/com/company/admin/service/VehicleCorrectionService.java`
- Create: `src/main/java/com/company/admin/service/impl/VehicleCorrectionServiceImpl.java`
- Create: `src/main/java/com/company/admin/controller/VehicleCorrectionController.java`
- Create: `src/test/java/com/company/admin/service/impl/VehicleCorrectionServiceImplTest.java`
- Modify: `src/main/java/com/company/admin/mapper/VehicleMapper.java`
- Modify: `src/main/resources/mapper/vlm/VehicleMapper.xml`

**Interfaces:**
- Produces: `PageResult<VehicleListResponse> pageCorrections(VehicleQueryRequest request)`.
- Produces: `VehiclePanoramaResponse getCorrection(Long vehicleId)`.
- Produces: `Page<VehicleListResponse> selectVehicleCorrectionPage(Page<VehicleListResponse>, VehicleQueryRequest)`.
- Reuses: `VehiclePanoramaService.getPanorama(String vin)` for the aggregate read model.

- [ ] **Step 1: 写只读服务失败测试**

创建 `VehicleCorrectionServiceImplTest`，使用 Mockito 验证完整列表不调用生产待办查询、详情按 ID 定位 VIN 后复用全景聚合：

```java
@ExtendWith(MockitoExtension.class)
class VehicleCorrectionServiceImplTest {
    @Mock VehicleMapper vehicleMapper;
    @Mock VehiclePanoramaService vehiclePanoramaService;
    @Mock BusinessStatusLabelService statusLabelService;
    VehicleCorrectionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VehicleCorrectionServiceImpl(
                vehicleMapper, vehiclePanoramaService, statusLabelService);
    }

    @Test
    void pageCorrectionsUsesUnscopedCorrectionQuery() {
        VehicleQueryRequest request = new VehicleQueryRequest();
        Page<VehicleListResponse> page = new Page<>(1, 10);
        VehicleListResponse completed = new VehicleListResponse();
        completed.setLifecycleStage("COMPLETED");
        page.setRecords(List.of(completed));
        page.setTotal(1);
        when(vehicleMapper.selectVehicleCorrectionPage(any(), eq(request))).thenReturn(page);

        PageResult<VehicleListResponse> result = service.pageCorrections(request);

        assertEquals(1, result.getTotal());
        assertEquals("COMPLETED", result.getList().get(0).getLifecycleStage());
        verify(vehicleMapper, never()).selectVehiclePage(any(), any());
    }

    @Test
    void getCorrectionResolvesVehicleByIdAndReturnsPanorama() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(70L);
        vehicle.setVin("VIN00000000000070");
        VehiclePanoramaResponse panorama = new VehiclePanoramaResponse();
        when(vehicleMapper.selectById(70L)).thenReturn(vehicle);
        when(vehiclePanoramaService.getPanorama(vehicle.getVin())).thenReturn(panorama);

        assertSame(panorama, service.getCorrection(70L));
    }

    @Test
    void getCorrectionRejectsDeletedOrMissingVehicle() {
        when(vehicleMapper.selectById(70L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.getCorrection(70L));
        assertEquals(ErrorCode.VEHICLE_NOT_FOUND.getCode(), ex.getCode());
    }
}
```

- [ ] **Step 2: 运行测试并确认类和方法尚不存在**

Run:

```powershell
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=VehicleCorrectionServiceImplTest test
```

Expected: FAIL at compilation，缺少 `VehicleCorrectionService`、实现类和 Mapper 方法。

- [ ] **Step 3: 增加专用完整分页 Mapper**

在 `VehicleMapper` 增加：

```java
Page<VehicleListResponse> selectVehicleCorrectionPage(
        Page<VehicleListResponse> page,
        @Param("query") VehicleQueryRequest query);
```

在 XML 把原完整车辆 SELECT/JOIN/业务筛选抽成 `<sql id="VehiclePageColumns">`、`<sql id="VehiclePageJoins">` 和 `<sql id="VehiclePageFilters">`，两个分页查询分别为：

```xml
<select id="selectVehiclePage" resultType="com.company.admin.dto.response.VehicleListResponse">
    SELECT <include refid="VehiclePageColumns"/>
    <include refid="VehiclePageJoins"/>
    WHERE v.deleted = 0
      AND v.lifecycle_stage = 'PENDING_OFFLINE'
    <include refid="VehiclePageFilters"/>
    ORDER BY v.id DESC
</select>

<select id="selectVehicleCorrectionPage" resultType="com.company.admin.dto.response.VehicleListResponse">
    SELECT <include refid="VehiclePageColumns"/>
    <include refid="VehiclePageJoins"/>
    WHERE v.deleted = 0
    <include refid="VehiclePageFilters"/>
    ORDER BY v.id DESC
</select>
```

`VehiclePageFilters` 必须包含原 VIN、modelId、modelName、series、lifecycleStage、dealerId 六个条件，不能加入固定阶段。

- [ ] **Step 4: 实现只读服务和控制器**

接口在本任务只定义两个只读方法：

```java
public interface VehicleCorrectionService {
    PageResult<VehicleListResponse> pageCorrections(VehicleQueryRequest request);
    VehiclePanoramaResponse getCorrection(Long vehicleId);
}
```

`pageCorrections` 调用专用 Mapper，逐行填充生命周期和生产状态标签；`getCorrection` 先按 ID 读取未删除车辆，再调用 `vehiclePanoramaService.getPanorama(vehicle.getVin())`。控制器：

```java
@RestController
@RequestMapping("/api/vehicle-corrections")
@RequiredArgsConstructor
@Tag(name = "车辆数据修订")
public class VehicleCorrectionController {
    private final VehicleCorrectionService vehicleCorrectionService;

    @GetMapping
    @PreAuthorize("hasAuthority('vlm:vehicle-correction:list')")
    public Result<PageResult<VehicleListResponse>> list(VehicleQueryRequest request) {
        return Result.success(vehicleCorrectionService.pageCorrections(request));
    }

    @GetMapping("/{vehicleId}")
    @PreAuthorize("hasAuthority('vlm:vehicle-correction:list')")
    public Result<VehiclePanoramaResponse> detail(@PathVariable Long vehicleId) {
        return Result.success(vehicleCorrectionService.getCorrection(vehicleId));
    }

}
```

- [ ] **Step 5: 运行只读服务测试和编译**

Run:

```powershell
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=VehicleCorrectionServiceImplTest test
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' -DskipTests compile
```

Expected: 两条命令均 BUILD SUCCESS。

- [ ] **Step 6: 提交只读修订切片**

```powershell
git add src/main/java/com/company/admin/mapper/VehicleMapper.java src/main/resources/mapper/vlm/VehicleMapper.xml src/main/java/com/company/admin/service/VehicleCorrectionService.java src/main/java/com/company/admin/service/impl/VehicleCorrectionServiceImpl.java src/main/java/com/company/admin/controller/VehicleCorrectionController.java src/test/java/com/company/admin/service/impl/VehicleCorrectionServiceImplTest.java
git commit -m "feat: add vehicle correction query API"
```

---

### Task 3: 锁定修订请求白名单

**Files:**
- Create: `src/main/java/com/company/admin/dto/request/VehicleCorrectionUpdateRequest.java`
- Create: `src/test/java/com/company/admin/dto/request/VehicleCorrectionUpdateRequestTest.java`

**Interfaces:**
- Produces: 七个 `@Valid` 嵌套阶段对象；发票为 `List<InvoiceCorrection>`。
- Security property: 请求类型中不存在任何流程、确认、VIN 或审计字段。

- [ ] **Step 1: 写请求白名单和校验失败测试**

```java
class VehicleCorrectionUpdateRequestTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void requestContractDoesNotExposeProtectedFields() {
        Set<String> forbidden = Set.of("vin", "vehicleId", "lifecycleStage", "stageStatus",
                "invoiceSeq", "confirmedBy", "confirmedAt", "createdBy", "createdAt",
                "updatedBy", "updatedAt", "deleted");
        List<Class<?>> contracts = List.of(VehicleCorrectionUpdateRequest.class,
                VehicleCorrectionUpdateRequest.ProductionCorrection.class,
                VehicleCorrectionUpdateRequest.InboundCorrection.class,
                VehicleCorrectionUpdateRequest.AllocationCorrection.class,
                VehicleCorrectionUpdateRequest.InvoiceCorrection.class,
                VehicleCorrectionUpdateRequest.PaymentCorrection.class,
                VehicleCorrectionUpdateRequest.DeliveryCorrection.class,
                VehicleCorrectionUpdateRequest.RegistrationCorrection.class);

        for (Class<?> contract : contracts) {
            Set<String> fields = Arrays.stream(contract.getDeclaredFields())
                    .map(Field::getName).collect(Collectors.toSet());
            assertTrue(Collections.disjoint(fields, forbidden), contract.getSimpleName());
        }
    }

    @Test
    void nestedRecordsRequireIdsAndRequiredBusinessFields() {
        VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
        request.setProduction(new VehicleCorrectionUpdateRequest.ProductionCorrection());
        request.setInvoices(List.of(new VehicleCorrectionUpdateRequest.InvoiceCorrection()));

        Set<String> paths = validator.validate(request).stream()
                .map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());

        assertTrue(paths.contains("production.id"));
        assertTrue(paths.contains("production.modelId"));
        assertTrue(paths.contains("production.engineNumber"));
        assertTrue(paths.contains("invoices[0].id"));
        assertTrue(paths.contains("invoices[0].invoiceType"));
        assertTrue(paths.contains("invoices[0].invoiceNo"));
        assertTrue(paths.contains("invoices[0].invoiceDate"));
    }
}
```

- [ ] **Step 2: 运行并确认请求契约尚不存在**

Run:

```powershell
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=VehicleCorrectionUpdateRequestTest test
```

Expected: FAIL at compilation，`VehicleCorrectionUpdateRequest` 及其嵌套类型尚不存在。

- [ ] **Step 3: 完成聚合请求 DTO**

顶层只包含：

```java
@Data
public class VehicleCorrectionUpdateRequest {
    @Valid private ProductionCorrection production;
    @Valid private InboundCorrection inbound;
    @Valid private AllocationCorrection allocation;
    @Valid private List<InvoiceCorrection> invoices;
    @Valid private PaymentCorrection payment;
    @Valid private DeliveryCorrection delivery;
    @Valid private RegistrationCorrection registration;
```

七个嵌套 `public static @Data class` 的精确字段与约束：

```java
public static class ProductionCorrection {
    @NotNull private Long id;
    @NotNull private Long modelId;
    @NotNull private Long exteriorColorId;
    @NotNull private Long interiorColorId;
    @NotBlank private String engineNumber;
    private String yearMake;
    private String material;
    private String shipment;
    private String batch;
    private LocalDate offlineEpmbDate;
    private LocalDate epmbOkDate;
    @Size(max = 500) private String remark1;
}
public static class InboundCorrection {
    @NotNull private Long id;
    private LocalDate saicBuyOffDate;
    private LocalDate dateToStorageYard;
    @Size(max = 500) private String remark2;
}
public static class AllocationCorrection {
    @NotNull private Long id;
    private LocalDate allocatedDate;
    @NotNull private Long dealerId;
    private String salesStatus;
    @Size(max = 500) private String remark3;
}
public static class InvoiceCorrection {
    @NotNull private Long id;
    @NotBlank private String invoiceType;
    @NotBlank private String invoiceNo;
    @NotNull private LocalDate invoiceDate;
    @Size(max = 500) private String remark;
}
public static class PaymentCorrection {
    @NotNull private Long id;
    @NotNull private LocalDate paymentDate;
    private LocalDate creditFullPaymentDate;
    private String paymentStatus;
    @Size(max = 500) private String remark5;
}
public static class DeliveryCorrection {
    @NotNull private Long id;
    private LocalDate etdToDealer;
    private LocalDate etaToDealer;
    private String trollyType;
    private Boolean fullyLoad;
    private LocalDate receivedDate;
    private String deliveryStatus;
    @Size(max = 500) private String remark7;
}
public static class RegistrationCorrection {
    @NotNull private Long id;
    private String drosstechStatus;
    private LocalDate uploadDate;
    private LocalDate registrationDate;
    @Size(max = 100) private String customerRegion;
    @Size(max = 500) private String remark8;
}
}
```

不要添加 Lombok builder，也不要复用包含 VIN 的 `ProductionSaveRequest`。

- [ ] **Step 4: 运行 DTO 测试**

Run: 同 Step 2。

Expected: BUILD SUCCESS。

- [ ] **Step 5: 提交请求契约**

```powershell
git add src/main/java/com/company/admin/dto/request/VehicleCorrectionUpdateRequest.java src/test/java/com/company/admin/dto/request/VehicleCorrectionUpdateRequestTest.java
git commit -m "feat: define vehicle correction whitelist"
```

---

### Task 4: 实现加锁、归属校验与白名单更新

**Files:**
- Create: `src/main/java/com/company/admin/service/VehicleCorrectionAuditService.java`
- Modify: `src/main/java/com/company/admin/common/ErrorCode.java`
- Modify: `src/main/java/com/company/admin/mapper/VehicleMapper.java`
- Modify: `src/main/java/com/company/admin/mapper/VehProductionMapper.java`
- Modify: `src/main/java/com/company/admin/mapper/VehInboundMapper.java`
- Modify: `src/main/java/com/company/admin/mapper/VehAllocationMapper.java`
- Modify: `src/main/java/com/company/admin/mapper/VehInvoiceMapper.java`
- Modify: `src/main/java/com/company/admin/mapper/VehPaymentMapper.java`
- Modify: `src/main/java/com/company/admin/mapper/VehDeliveryMapper.java`
- Modify: `src/main/java/com/company/admin/mapper/VehRegistrationMapper.java`
- Create: `src/main/resources/mapper/vlm/VehProductionMapper.xml`
- Modify: the other seven XML mapper files listed in File Structure
- Modify: `src/main/java/com/company/admin/service/VehicleCorrectionService.java`
- Modify: `src/main/java/com/company/admin/service/impl/VehicleCorrectionServiceImpl.java`
- Modify: `src/main/java/com/company/admin/controller/VehicleCorrectionController.java`
- Modify: `src/test/java/com/company/admin/service/impl/VehicleCorrectionServiceImplTest.java`

**Interfaces:**
- Produces: `Vehicle selectByIdForUpdate(Long id)` and one `selectByIdForUpdate(Long id)` per stage mapper.
- Produces: `void updateCorrection(Long vehicleId, VehicleCorrectionUpdateRequest request)`.
- Produces: `VehicleCorrectionAuditService.CorrectionDiff` for Task 5's persistent audit implementation.
- Error: `CORRECTION_RECORD_MISMATCH(6018, "修订记录与车辆不匹配")`.
- Transaction: `updateCorrection` is public and annotated `@Transactional`.

- [ ] **Step 1: 写已确认可修、保护字段不变、跨车拒绝和多发票测试**

扩展 Mockito 测试；核心断言如下：

```java
@Test
void updateConfirmedProductionChangesOnlyBusinessFields() {
    Vehicle vehicle = vehicle(80L, "VIN00000000000080", "COMPLETED");
    VehProduction production = new VehProduction();
    production.setId(801L);
    production.setVehicleId(80L);
    production.setStageStatus("CONFIRMED");
    production.setConfirmedBy("original-confirmer");
    production.setEngineNumber("OLD");
    when(vehicleMapper.selectByIdForUpdate(80L)).thenReturn(vehicle);
    when(vehProductionMapper.selectByIdForUpdate(801L)).thenReturn(production);
    stubActiveMasterData();

    VehicleCorrectionUpdateRequest request = new VehicleCorrectionUpdateRequest();
    ProductionCorrection change = validProduction(801L);
    change.setEngineNumber("NEW");
    request.setProduction(change);
    service.updateCorrection(80L, request);

    assertEquals("NEW", production.getEngineNumber());
    assertEquals("CONFIRMED", production.getStageStatus());
    assertEquals("original-confirmer", production.getConfirmedBy());
    assertEquals("COMPLETED", vehicle.getLifecycleStage());
    verify(vehProductionMapper).updateById(production);
}

@Test
void updateRejectsStageRecordOwnedByAnotherVehicle() {
    when(vehicleMapper.selectByIdForUpdate(80L)).thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
    VehPayment payment = new VehPayment();
    payment.setId(802L);
    payment.setVehicleId(81L);
    when(vehPaymentMapper.selectByIdForUpdate(802L)).thenReturn(payment);
    VehicleCorrectionUpdateRequest request = requestWithPayment(802L, "PAID");

    BusinessException ex = assertThrows(BusinessException.class,
            () -> service.updateCorrection(80L, request));

    assertEquals(ErrorCode.CORRECTION_RECORD_MISMATCH.getCode(), ex.getCode());
    verify(vehPaymentMapper, never()).updateById(any());
}

@Test
void updateInvoicesPreservesSequenceAndRejectsMissingRecord() {
    when(vehicleMapper.selectByIdForUpdate(80L)).thenReturn(vehicle(80L, "VIN00000000000080", "COMPLETED"));
    VehInvoice first = invoice(803L, 80L, 1);
    when(vehInvoiceMapper.selectByIdForUpdate(803L)).thenReturn(first);
    when(vehInvoiceMapper.selectByIdForUpdate(804L)).thenReturn(null);
    when(statusLabelService.dictLabels("invoice_status")).thenReturn(Map.of("INVOICED", "正式发票"));
    VehicleCorrectionUpdateRequest request = requestWithInvoices(invoiceChange(803L), invoiceChange(804L));

    assertThrows(BusinessException.class, () -> service.updateCorrection(80L, request));
    assertEquals(1, first.getInvoiceSeq());
}
```

另加：无效 model/color/dealer 返回 `BAD_REQUEST`；`etaToDealer` 早于 `etdToDealer` 返回 `BAD_REQUEST`；`trollyType` 非 `4 units`/`6 units` 返回 `BAD_REQUEST`；无效 `sales_status`、`invoice_status`、`payment_status`、`delivery_status`、`drosstech_status` 被拒绝。

- [ ] **Step 2: 运行服务测试并确认更新尚未实现**

Run:

```powershell
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=VehicleCorrectionServiceImplTest test
```

Expected: FAIL at compilation，缺少锁定 Mapper 方法、错误码和更新实现。

- [ ] **Step 3: 增加按 ID 行锁 Mapper**

所有 Mapper 接口统一增加：

```java
VehProduction selectByIdForUpdate(@Param("id") Long id);
```

类型按各实体替换；`VehicleMapper` 返回 `Vehicle`。XML 统一使用：

```xml
<select id="selectByIdForUpdate" resultType="com.company.admin.entity.VehProduction">
    SELECT * FROM t_veh_production
    WHERE id = #{id} AND deleted = 0
    FOR UPDATE
</select>
```

分别替换表名和实体类型。不要移除入库、配送现有的 `selectByVehicleIdForUpdate`，普通阶段服务仍依赖它们。

- [ ] **Step 4: 定义差异对象并实现记录加载、归属与引用校验 helper**

创建审计接口；Task 4 的单元测试 mock 此接口，Task 5 提供持久化实现：

```java
public interface VehicleCorrectionAuditService {
    void recordSuccess(CorrectionDiff diff);

    @Data
    @RequiredArgsConstructor
    class CorrectionDiff {
        private final Long vehicleId;
        private final String vin;
        private final Map<String, Map<String, Object>> before = new LinkedHashMap<>();
        private final Map<String, Map<String, Object>> after = new LinkedHashMap<>();

        public void before(String stage, Long id, Map<String, Object> values) {
            before.put(stage + ":" + id, new LinkedHashMap<>(values));
        }

        public void after(String stage, Long id, Map<String, Object> values) {
            after.put(stage + ":" + id, new LinkedHashMap<>(values));
        }
    }
}
```

在 `VehicleCorrectionServiceImpl` 增加显式 helper，避免反射复制字段：

```java
private <T> T requireOwned(T entity, Long ownerVehicleId, Long requestedVehicleId) {
    if (entity == null) {
        throw new BusinessException(ErrorCode.STAGE_DATA_NOT_FOUND);
    }
    if (!requestedVehicleId.equals(ownerVehicleId)) {
        throw new BusinessException(ErrorCode.CORRECTION_RECORD_MISMATCH);
    }
    return entity;
}

private void requireDictionary(String dictCode, String value) {
    if (value != null && !value.isBlank()
            && !statusLabelService.dictLabels(dictCode).containsKey(value)) {
        throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(),
                "无效字典值: " + dictCode + "=" + value);
    }
}

private void requireActive(long count, String name) {
    if (count == 0) {
        throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), name + "不存在或已停用");
    }
}
```

车型、颜色、经销商分别使用对应 Mapper 的 `selectCount`，条件固定为 `id`、`status=1`、`deleted=0`。DTO 中必填的 ID 不允许以 null 跳过校验。

- [ ] **Step 5: 实现事务更新和显式白名单复制**

先在 `VehicleCorrectionService` 增加：

```java
void updateCorrection(Long vehicleId, VehicleCorrectionUpdateRequest request);
```

公开方法结构固定为：

```java
@Override
@Transactional
public void updateCorrection(Long vehicleId, VehicleCorrectionUpdateRequest request) {
    Vehicle vehicle = vehicleMapper.selectByIdForUpdate(vehicleId);
    if (vehicle == null) {
        throw new BusinessException(ErrorCode.VEHICLE_NOT_FOUND);
    }
    CorrectionDiff diff = new CorrectionDiff(vehicleId, vehicle.getVin());
    if (request.getProduction() != null) updateProduction(vehicleId, request.getProduction(), diff);
    if (request.getInbound() != null) updateInbound(vehicleId, request.getInbound(), diff);
    if (request.getAllocation() != null) updateAllocation(vehicleId, request.getAllocation(), diff);
    if (request.getInvoices() != null) {
        for (InvoiceCorrection invoice : request.getInvoices()) updateInvoice(vehicleId, invoice, diff);
    }
    if (request.getPayment() != null) updatePayment(vehicleId, request.getPayment(), diff);
    if (request.getDelivery() != null) updateDelivery(vehicleId, request.getDelivery(), diff);
    if (request.getRegistration() != null) updateRegistration(vehicleId, request.getRegistration(), diff);
    auditService.recordSuccess(diff);
}
```

每个 `update*` 都必须按 ID 加锁、调用 `requireOwned`、在修改前把白名单字段放入 `diff.before(stage, id, map)`，逐字段 setter 更新、调用 `updateById`，再记录 `diff.after(...)`。各阶段的赋值语句必须完整且仅为：

```java
// production
entity.setModelId(change.getModelId());
entity.setExteriorColorId(change.getExteriorColorId());
entity.setInteriorColorId(change.getInteriorColorId());
entity.setEngineNumber(change.getEngineNumber());
entity.setYearMake(change.getYearMake());
entity.setMaterial(change.getMaterial());
entity.setShipment(change.getShipment());
entity.setBatch(change.getBatch());
entity.setOfflineEpmbDate(change.getOfflineEpmbDate());
entity.setEpmbOkDate(change.getEpmbOkDate());
entity.setRemark1(change.getRemark1());

// inbound
entity.setSaicBuyOffDate(change.getSaicBuyOffDate());
entity.setDateToStorageYard(change.getDateToStorageYard());
entity.setRemark2(change.getRemark2());

// allocation
entity.setAllocatedDate(change.getAllocatedDate());
entity.setDealerId(change.getDealerId());
entity.setSalesStatus(change.getSalesStatus());
entity.setRemark3(change.getRemark3());

// invoice
entity.setInvoiceType(change.getInvoiceType());
entity.setInvoiceNo(change.getInvoiceNo());
entity.setInvoiceDate(change.getInvoiceDate());
entity.setRemark(change.getRemark());

// payment
entity.setPaymentDate(change.getPaymentDate());
entity.setCreditFullPaymentDate(change.getCreditFullPaymentDate());
entity.setPaymentStatus(change.getPaymentStatus());
entity.setRemark5(change.getRemark5());

// delivery
entity.setEtdToDealer(change.getEtdToDealer());
entity.setEtaToDealer(change.getEtaToDealer());
entity.setTrollyType(change.getTrollyType());
entity.setFullyLoad(change.getFullyLoad());
entity.setReceivedDate(change.getReceivedDate());
entity.setDeliveryStatus(change.getDeliveryStatus());
entity.setRemark7(change.getRemark7());

// registration
entity.setDrosstechStatus(change.getDrosstechStatus());
entity.setUploadDate(change.getUploadDate());
entity.setRegistrationDate(change.getRegistrationDate());
entity.setCustomerRegion(change.getCustomerRegion());
entity.setRemark8(change.getRemark8());
```

为每种实体创建对应的 `*Values(entity)` 方法，返回以上同名字段组成的 `LinkedHashMap<String,Object>`；`before` 在任何 setter 前调用，`after` 在 setter 后调用。不得使用 `Map.of`（业务值允许 null），不得调用 `BeanUtils.copyProperties`，不得设置流程/确认字段。

配送更新前执行：

```java
if (change.getEtdToDealer() != null && change.getEtaToDealer() != null
        && change.getEtaToDealer().isBefore(change.getEtdToDealer())) {
    throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "预计到达日期不能早于发车日期");
}
if (change.getTrollyType() != null
        && !"4 units".equals(change.getTrollyType())
        && !"6 units".equals(change.getTrollyType())) {
    throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "拖运车类型只能为4 units或6 units");
}
```

字典映射固定为：`sales_status`、`invoice_status`、`payment_status`、`delivery_status`、`drosstech_status`。发票列表先检查请求 ID 不重复：

```java
Set<Long> invoiceIds = new HashSet<>();
for (InvoiceCorrection invoice : request.getInvoices()) {
    if (!invoiceIds.add(invoice.getId())) {
        throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "发票记录重复");
    }
    updateInvoice(vehicleId, invoice, diff);
}
```

最后在 `VehicleCorrectionController` 增加写接口；它与两个 GET 端点使用不同权限：

```java
@PutMapping("/{vehicleId}")
@PreAuthorize("hasAuthority('vlm:vehicle-correction:edit')")
@OpLog(value = "车辆数据修订", type = OpLog.LogType.UPDATE)
public Result<Void> update(@PathVariable Long vehicleId,
                           @Valid @RequestBody VehicleCorrectionUpdateRequest request) {
    vehicleCorrectionService.updateCorrection(vehicleId, request);
    return Result.success();
}
```

- [ ] **Step 6: 运行服务测试**

Run: 同 Step 2。

Expected: BUILD SUCCESS；确认字段、阶段状态、生命周期和发票序号保持原值。

- [ ] **Step 7: 提交修订写入核心**

```powershell
git add src/main/java/com/company/admin/common/ErrorCode.java src/main/java/com/company/admin/mapper/VehicleMapper.java src/main/java/com/company/admin/mapper/VehProductionMapper.java src/main/java/com/company/admin/mapper/VehInboundMapper.java src/main/java/com/company/admin/mapper/VehAllocationMapper.java src/main/java/com/company/admin/mapper/VehInvoiceMapper.java src/main/java/com/company/admin/mapper/VehPaymentMapper.java src/main/java/com/company/admin/mapper/VehDeliveryMapper.java src/main/java/com/company/admin/mapper/VehRegistrationMapper.java src/main/resources/mapper/vlm/VehicleMapper.xml src/main/resources/mapper/vlm/VehProductionMapper.xml src/main/resources/mapper/vlm/VehInboundMapper.xml src/main/resources/mapper/vlm/VehAllocationMapper.xml src/main/resources/mapper/vlm/VehInvoiceMapper.xml src/main/resources/mapper/vlm/VehPaymentMapper.xml src/main/resources/mapper/vlm/VehDeliveryMapper.xml src/main/resources/mapper/vlm/VehRegistrationMapper.xml src/main/java/com/company/admin/service/VehicleCorrectionService.java src/main/java/com/company/admin/service/VehicleCorrectionAuditService.java src/main/java/com/company/admin/service/impl/VehicleCorrectionServiceImpl.java src/main/java/com/company/admin/controller/VehicleCorrectionController.java src/test/java/com/company/admin/service/impl/VehicleCorrectionServiceImplTest.java
git commit -m "feat: update confirmed vehicle business data safely"
```

---

### Task 5: 字段级审计、真实事务回滚和接口权限

**Files:**
- Create: `src/main/java/com/company/admin/service/impl/VehicleCorrectionAuditServiceImpl.java`
- Create: `src/test/java/com/company/admin/service/impl/VehicleCorrectionServiceIntegrationTest.java`
- Create: `src/test/java/com/company/admin/controller/VehicleCorrectionControllerTest.java`
- Modify: `src/test/resources/sql/h2-mapper-smoke-schema.sql`
- Modify: `src/main/java/com/company/admin/service/impl/VehicleCorrectionServiceImpl.java`
- Modify: `src/main/java/com/company/admin/controller/VehicleCorrectionController.java`

**Interfaces:**
- Produces: `void recordSuccess(CorrectionDiff diff)`.
- Audit storage: existing `sys_operation_log.params` stores JSON with `vehicleId`, `vin`, `before`, `after`.
- Failure audit: existing `@OpLog` aspect records failed request and exception; success also gets the structured service log.

- [ ] **Step 1: 扩展 H2 schema 并写真实事务失败测试**

给 H2 阶段表补齐 Task 3 会更新的业务列；增加：

```sql
CREATE TABLE sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100),
    operation VARCHAR(100),
    method VARCHAR(255),
    params CLOB,
    result CLOB,
    ip VARCHAR(100),
    create_time TIMESTAMP
);
```

创建 `VehicleCorrectionServiceIntegrationTest`：

```java
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/h2-mapper-smoke-schema.sql")
class VehicleCorrectionServiceIntegrationTest {
    @Autowired VehicleCorrectionService service;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void laterValidationFailureRollsBackEarlierProductionUpdate() {
        seedCompletedVehicleWithProductionAndPayment();
        VehicleCorrectionUpdateRequest request = validProductionAndPaymentRequest();
        request.getProduction().setEngineNumber("NEW-ENGINE");
        request.getPayment().setPaymentStatus("NOT_A_DICTIONARY_VALUE");

        assertThrows(BusinessException.class, () -> service.updateCorrection(90L, request));

        assertEquals("OLD-ENGINE", jdbcTemplate.queryForObject(
                "SELECT engine_number FROM t_veh_production WHERE id=901", String.class));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_operation_log WHERE operation='车辆数据修订-字段差异'", Integer.class));
    }

    @Test
    void successfulCorrectionPersistsStructuredBeforeAndAfterAudit() {
        seedCompletedVehicleWithProductionAndPayment();
        VehicleCorrectionUpdateRequest request = validProductionRequest(901L, "NEW-ENGINE");

        service.updateCorrection(90L, request);

        String params = jdbcTemplate.queryForObject(
                "SELECT params FROM sys_operation_log WHERE operation='车辆数据修订-字段差异'", String.class);
        assertTrue(params.contains("\"vehicleId\":90"));
        assertTrue(params.contains("OLD-ENGINE"));
        assertTrue(params.contains("NEW-ENGINE"));
        assertEquals("CONFIRMED", jdbcTemplate.queryForObject(
                "SELECT stage_status FROM t_veh_production WHERE id=901", String.class));
    }
}
```

测试 seed 同时插入有效车型/颜色和字典类型/字典项；失败测试中的支付字典值故意无效。

- [ ] **Step 2: 运行集成测试并确认日志/事务支持未完成**

Run:

```powershell
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=VehicleCorrectionServiceIntegrationTest test
```

Expected: FAIL，审计服务尚未写入结构化差异，或 H2 缺少更新所需字段。

- [ ] **Step 3: 实现结构化差异日志**

使用 Task 4 已定义的 `VehicleCorrectionAuditService.CorrectionDiff`。实现类注入 `OperationLogMapper` 和 Spring 管理的 `ObjectMapper`：

```java
@Service
@RequiredArgsConstructor
public class VehicleCorrectionAuditServiceImpl implements VehicleCorrectionAuditService {
    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void recordSuccess(CorrectionDiff diff) {
        OperationLog log = new OperationLog();
        log.setUsername(SecurityUtils.getCurrentUsername());
        log.setOperation("车辆数据修订-字段差异");
        log.setMethod("VehicleCorrectionService.updateCorrection");
        log.setCreateTime(LocalDateTime.now());
        try {
            log.setParams(objectMapper.writeValueAsString(diff));
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        log.setResult("SUCCESS");
        operationLogMapper.insert(log);
    }
}
```

服务仅把白名单业务字段放入 `before/after` map；不要序列化整个实体，以免未来新增字段时意外扩大日志内容。

- [ ] **Step 4: 运行集成测试并确认事务和日志通过**

Run: 同 Step 2。

Expected: BUILD SUCCESS；失败更新无业务字段和成功日志残留，成功日志包含前后值。

- [ ] **Step 5: 写并运行控制器权限失败测试**

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VehicleCorrectionControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean VehicleCorrectionService vehicleCorrectionService;

    @Test
    @WithMockUser(authorities = "vlm:vehicle-correction:list")
    void listPermissionCanRead() throws Exception {
        when(vehicleCorrectionService.pageCorrections(any())).thenReturn(new PageResult<>());
        mockMvc.perform(get("/api/vehicle-corrections")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "vlm:vehicle-correction:list")
    void listPermissionCannotEdit() throws Exception {
        mockMvc.perform(put("/api/vehicle-corrections/90")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "vlm:vehicle-correction:edit")
    void editPermissionCanSave() throws Exception {
        mockMvc.perform(put("/api/vehicle-corrections/90")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        verify(vehicleCorrectionService).updateCorrection(eq(90L), any());
    }
}
```

Run:

```powershell
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=VehicleCorrectionControllerTest test
```

Expected: BUILD SUCCESS。若 JWT filter 干扰 `@WithMockUser`，在该测试中用 `@MockBean JwtAuthenticationFilter` 并让它直接 `chain.doFilter`；不要关闭 method security。

- [ ] **Step 6: 提交事务、审计和权限保护**

```powershell
git add src/main/java/com/company/admin/service/impl/VehicleCorrectionAuditServiceImpl.java src/main/java/com/company/admin/service/impl/VehicleCorrectionServiceImpl.java src/main/java/com/company/admin/controller/VehicleCorrectionController.java src/test/resources/sql/h2-mapper-smoke-schema.sql src/test/java/com/company/admin/service/impl/VehicleCorrectionServiceIntegrationTest.java src/test/java/com/company/admin/controller/VehicleCorrectionControllerTest.java
git commit -m "feat: audit privileged vehicle corrections"
```

---

### Task 6: 新增管理员菜单和幂等迁移

**Files:**
- Create: `src/main/resources/sql/d00004_vehicle_stage_todo_correction_migration.sql`
- Create: `src/test/java/com/company/admin/sql/VehicleCorrectionMenuSqlTest.java`
- Modify: `src/main/resources/sql/admin_system.sql`

**Interfaces:**
- Menu ID 122: page under production/vehicle center parent 120.
- Button IDs 1120/1121: list/edit.
- Full seed role-menu IDs 291/292/293: ADMIN only.
- Migration role-menu inserts omit primary key and use `WHERE NOT EXISTS`.

- [ ] **Step 1: 写菜单和授权失败测试**

```java
class VehicleCorrectionMenuSqlTest {
    @Test
    void fullSeedDefinesAdminOnlyCorrectionMenu() throws IOException {
        String sql = resource("/sql/admin_system.sql");
        assertTrue(sql.contains("(122, 120, '车辆数据修订', 2, '/production/vehicle-correction', 'vlm:vehicle-correction:list'"));
        assertTrue(sql.contains("(1120, 122, '车辆修订查询', 3, NULL, 'vlm:vehicle-correction:list'"));
        assertTrue(sql.contains("(1121, 122, '车辆修订编辑', 3, NULL, 'vlm:vehicle-correction:edit'"));
        assertTrue(sql.contains("(291, 1, 122)"));
        assertTrue(sql.contains("(292, 1, 1120)"));
        assertTrue(sql.contains("(293, 1, 1121)"));
        assertFalse(Pattern.compile("VALUES \\(\\d+, (100|101|102|103|104|105), (122|1120|1121)\\)")
                .matcher(sql).find());
    }

    @Test
    void migrationUsesIdempotentRoleAssignmentsWithoutFixedJoinIds() throws IOException {
        String sql = resource("/sql/d00004_vehicle_stage_todo_correction_migration.sql");
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE"));
        assertTrue(sql.contains("INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)"));
        assertTrue(sql.contains("WHERE NOT EXISTS"));
        assertFalse(sql.contains("INSERT INTO `sys_role_menu` (`id`"));
    }
}
```

- [ ] **Step 2: 运行并确认新菜单尚不存在**

Run:

```powershell
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=VehicleCorrectionMenuSqlTest test
```

Expected: FAIL；全量 seed 和增量迁移都没有新菜单。

- [ ] **Step 3: 修改全量 seed**

在 `sys_menu` 记录区加入：

```sql
INSERT INTO `sys_menu` VALUES (122, 120, '车辆数据修订', 2, '/production/vehicle-correction', 'vlm:vehicle-correction:list', 'edit', 112, 1, '2026-07-18 00:00:00', '2026-07-18 00:00:00', 0);
INSERT INTO `sys_menu` VALUES (1120, 122, '车辆修订查询', 3, NULL, 'vlm:vehicle-correction:list', NULL, 1120, 1, '2026-07-18 00:00:00', '2026-07-18 00:00:00', 0);
INSERT INTO `sys_menu` VALUES (1121, 122, '车辆修订编辑', 3, NULL, 'vlm:vehicle-correction:edit', NULL, 1121, 1, '2026-07-18 00:00:00', '2026-07-18 00:00:00', 0);
```

在角色关联区加入：

```sql
INSERT INTO `sys_role_menu` VALUES (291, 1, 122);
INSERT INTO `sys_role_menu` VALUES (292, 1, 1120);
INSERT INTO `sys_role_menu` VALUES (293, 1, 1121);
```

把 `sys_role_menu` 的 `AUTO_INCREMENT` 至少调整为 294。不要给 `BUSINESS_MANAGER` 或任何专员角色分配新节点。

- [ ] **Step 4: 创建幂等迁移**

迁移对三个 `sys_menu` ID 使用 `INSERT ... ON DUPLICATE KEY UPDATE`，更新所有业务列但不修改 `created_at`；三条管理员映射分别使用：

```sql
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 122
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 122
);
```

对 1120、1121 重复完整语句。迁移不删除或重写任何既有角色关联。

- [ ] **Step 5: 运行菜单契约及既有菜单回归**

Run:

```powershell
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' -Dtest=VehicleCorrectionMenuSqlTest,VlmMenuSeedDataTest test
```

Expected: BUILD SUCCESS；原 D00003 菜单契约继续通过。

- [ ] **Step 6: 提交菜单与迁移**

```powershell
git add src/main/resources/sql/admin_system.sql src/main/resources/sql/d00004_vehicle_stage_todo_correction_migration.sql src/test/java/com/company/admin/sql/VehicleCorrectionMenuSqlTest.java
git commit -m "feat: add admin vehicle correction permissions"
```

---

### Task 7: API 文档与端到端验收

**Files:**
- Modify: `docs/api-document.md`
- Verify: all implementation and test files from Tasks 1–6.

**Interfaces:**
- Documents: fixed-stage list semantics, protected fields, three correction endpoints and two permissions.
- Acceptance: all targeted and full Maven tests pass with no unintended working-tree files staged.

- [ ] **Step 1: 更新 API 文档**

在车辆列表章节明确 `GET /api/vehicles` 固定 `PENDING_OFFLINE`；在六个阶段列表章节分别写明固定生命周期以及 `CONFIRMED` 不再提供历史查询。新增“车辆数据修订”章节，至少包含：

```markdown
### GET /api/vehicle-corrections
权限：`vlm:vehicle-correction:list`
说明：管理员完整车辆分页查询，查询参数与车辆列表一致，但不固定生命周期。

### GET /api/vehicle-corrections/{vehicleId}
权限：`vlm:vehicle-correction:list`
说明：返回车辆全景聚合数据；不存在的阶段为 null/空列表。

### PUT /api/vehicle-corrections/{vehicleId}
权限：`vlm:vehicle-correction:edit`
说明：只更新请求中出现且已经存在的阶段记录；VIN、生命周期、阶段状态、发票序号、确认及审计字段不可写。
```

列出七个嵌套对象字段和多发票 `id` 归属规则；说明任一失败会回滚全部更新。

- [ ] **Step 2: 运行全部定向测试**

```powershell
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' '-Dtest=MapperSqlSmokeTest,VehicleCorrectionUpdateRequestTest,VehicleCorrectionServiceImplTest,VehicleCorrectionServiceIntegrationTest,VehicleCorrectionControllerTest,VehicleCorrectionMenuSqlTest,VlmMenuSeedDataTest' test
```

Expected: BUILD SUCCESS，0 failures，0 errors。

- [ ] **Step 3: 运行原生命周期服务回归**

```powershell
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' '-Dtest=VehProductionServiceImplTest,VehInboundServiceImplTest,VehAllocationServiceImplTest,VehInvoiceServiceImplTest,VehPaymentServiceImplTest,VehDeliveryServiceImplTest,VehRegistrationServiceImplTest,VehiclePanoramaServiceImplTest' test
```

Expected: BUILD SUCCESS；普通更新仍拒绝 `CONFIRMED`，确认仍推进到下一阶段。

- [ ] **Step 4: 运行完整测试套件**

```powershell
& 'C:\Users\arthu\software\apache-maven-3.9.16\bin\mvn.cmd' test
```

Expected: BUILD SUCCESS，全部新旧测试通过。

- [ ] **Step 5: 做静态边界检查**

```powershell
rg -n "confirmAndAdvance|advanceStage|setLifecycleStage|setStageStatus|setConfirmedBy|setConfirmedAt" src/main/java/com/company/admin/service/impl/VehicleCorrectionServiceImpl.java
rg -n "vlm:vehicle-correction:(list|edit)" src/main/java src/main/resources/sql
git diff --check
git status --short
```

Expected:

- 修订服务中不存在生命周期推进调用，也不存在对保护字段的 setter。
- 两个新权限只出现在修订控制器和菜单 SQL 中。
- `git diff --check` 无输出。
- `git status --short` 只显示本任务明确文件；用户原有 `.DS_Store`、`.codex/`、`.gstack/`、已有未跟踪文档仍保持原样且未被暂存。

- [ ] **Step 6: 提交 API 文档**

```powershell
git add docs/api-document.md
git commit -m "docs: document vehicle correction workflow"
```

---

## Final Acceptance Checklist

- [ ] 七个普通列表始终按对应当前生命周期返回待办；确认后无法通过 `CONFIRMED` 等参数查回。
- [ ] 当前阶段未建草稿和已有草稿仍能出现在对应待办列表。
- [ ] `/api/vehicle-corrections` 保留原完整车辆列表查询能力。
- [ ] 修订详情返回生产、入库、销售分配、多发票、收款、配送、上牌的既有数据。
- [ ] 修订保存只更新白名单业务字段，且只更新属于路径车辆的既有记录。
- [ ] VIN、生命周期、阶段状态、发票序号、确认字段和审计字段无法由请求修改。
- [ ] 修订请求逐条加锁并在单事务中完成；中途失败全部回滚。
- [ ] 成功修订写入字段级前后差异日志，失败请求由现有操作日志记录异常。
- [ ] 新菜单及 list/edit 权限默认只授予 ADMIN；普通专员和业务主管默认无权访问。
- [ ] 旧批量运输模块、全景查询及普通阶段确认流程没有行为回归。
- [ ] 定向测试、生命周期回归测试和完整 Maven 测试全部通过。
