# 业务状态中文展示 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 保留车辆全生命周期业务编码，并为业务接口响应补充统一的中文状态/阶段展示字段。

**Architecture:** 新增 `BusinessStatusLabelService` 作为唯一展示映射入口。固定生命周期、阶段和物流状态从现有枚举读取中文标签；发票、收款、配送、上牌和销售状态从启用字典项读取中文标签。业务服务只负责在响应组装后填充 `*Label` 字段，数据库编码、请求参数和状态机不变。

**Tech Stack:** Java 8+、Spring Boot、MyBatis-Plus、JUnit 5、Mockito、MySQL 初始化 SQL。

## Global Constraints

- 原有编码字段和值必须保持不变，新增字段统一使用原字段名加 `Label` 后缀。
- 只处理车辆全生命周期业务状态；不新增用户、角色、菜单、车型、经销商、颜色等 `status=0/1` 的 `statusLabel`。
- 空编码对应的标签保持 `null`；未知编码或未配置字典项对应的标签返回原编码。
- 字典编码保持不变，只把业务字典的展示名称、类型名称和备注改为中文。
- 每个新增行为先写测试并确认测试因功能缺失失败，再写最小实现。

---

### Task 1: 建立统一业务状态标签转换服务

**Files:**
- Create: `src/main/java/com/company/admin/service/BusinessStatusLabelService.java`
- Create: `src/main/java/com/company/admin/service/impl/BusinessStatusLabelServiceImpl.java`
- Create: `src/test/java/com/company/admin/service/impl/BusinessStatusLabelServiceImplTest.java`

**Interfaces:**
- Consumes: `DictTypeMapper`, `DictItemMapper`, `StageStatus`, `OrderStatus`, `LifecycleStage`。
- Produces: `lifecycleStageLabel(String)`, `stageStatusLabel(String)`, `orderStatusLabel(String)`, `dictLabel(String, String)`, `dictLabels(String)`。

- [ ] **Step 1: Write the failing tests**

```java
@ExtendWith(MockitoExtension.class)
class BusinessStatusLabelServiceImplTest {

    @Mock
    private DictTypeMapper dictTypeMapper;
    @Mock
    private DictItemMapper dictItemMapper;

    @InjectMocks
    private BusinessStatusLabelServiceImpl service;

    @Test
    void resolvesEnumLabelsAndFallsBackToCode() {
        assertEquals("待收款", service.lifecycleStageLabel("PENDING_PAYMENT"));
        assertEquals("草稿", service.stageStatusLabel("DRAFT"));
        assertEquals("已确认", service.orderStatusLabel("CONFIRMED"));
        assertEquals("UNEXPECTED", service.lifecycleStageLabel("UNEXPECTED"));
        assertNull(service.stageStatusLabel(null));
    }

    @Test
    void resolvesEnabledDictionaryLabelAndFallsBackToValue() {
        DictType type = new DictType();
        type.setId(14L);
        DictItem item = new DictItem();
        item.setDictTypeId(14L);
        item.setItemValue("UNPAID");
        item.setItemLabel("未收款");
        when(dictTypeMapper.selectOne(any())).thenReturn(type);
        when(dictItemMapper.selectList(any())).thenReturn(List.of(item));

        assertEquals("未收款", service.dictLabel("payment_status", "UNPAID"));
        assertEquals("UNKNOWN", service.dictLabel("payment_status", "UNKNOWN"));
        assertNull(service.dictLabel("payment_status", null));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=BusinessStatusLabelServiceImplTest test`

Expected: FAIL because `BusinessStatusLabelService` and its implementation do not exist.

- [ ] **Step 3: Write minimal implementation**

Create an interface with the five methods above. Implement it as a Spring `@Service` using `DictTypeMapper.selectOne` to find an enabled dictionary type, then `DictItemMapper.selectList` to load enabled items ordered by `sort_order`. Convert enum values with `Enum.valueOf`; catch `IllegalArgumentException` and return the original code. Return `null` for null input and return the original value when the dictionary type/item is unavailable.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=BusinessStatusLabelServiceImplTest test`

Expected: PASS with 2 tests and 0 failures.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/com/company/admin/service/BusinessStatusLabelService.java src/main/java/com/company/admin/service/impl/BusinessStatusLabelServiceImpl.java src/test/java/com/company/admin/service/impl/BusinessStatusLabelServiceImplTest.java
git commit -m "feat: add business status label resolver"
```

### Task 2: 扩展生命周期业务响应 DTO

**Files:**
- Modify: `src/main/java/com/company/admin/dto/response/VehicleBasicInfo.java`
- Modify: `src/main/java/com/company/admin/dto/response/VehicleListResponse.java`
- Modify: `src/main/java/com/company/admin/dto/response/ProductionResponse.java`
- Modify: `src/main/java/com/company/admin/dto/response/AllocationResponse.java`
- Modify: `src/main/java/com/company/admin/dto/response/InvoiceListResponse.java`
- Modify: `src/main/java/com/company/admin/dto/response/InvoiceResponse.java`
- Modify: `src/main/java/com/company/admin/dto/response/PaymentResponse.java`
- Modify: `src/main/java/com/company/admin/dto/response/RegistrationResponse.java`
- Modify: `src/main/java/com/company/admin/dto/response/TransportOrderListResponse.java`
- Modify: `src/main/java/com/company/admin/dto/response/TransportOrderDetailResponse.java`
- Modify: `src/main/java/com/company/admin/dto/response/DispatchListResponse.java`
- Modify: `src/main/java/com/company/admin/dto/response/WaybillDealerResponse.java`
- Modify: `src/main/java/com/company/admin/dto/response/VehiclePanoramaResponse.java`
- Create: `src/test/java/com/company/admin/dto/response/BusinessStatusLabelFieldsTest.java`

**Interfaces:**
- Consumes: existing response DTO JSON contracts.
- Produces: nullable label properties that serialize alongside existing code properties.

- [ ] **Step 1: Write the failing test**

```java
class BusinessStatusLabelFieldsTest {

    @Test
    void responseDtosExposeLabelFieldsWithoutChangingCodeFields() {
        VehicleBasicInfo vehicle = new VehicleBasicInfo();
        vehicle.setLifecycleStage("PENDING_PAYMENT");
        vehicle.setLifecycleStageLabel("待收款");

        PaymentResponse payment = new PaymentResponse();
        payment.setStageStatus("DRAFT");
        payment.setStageStatusLabel("草稿");

        assertEquals("PENDING_PAYMENT", vehicle.getLifecycleStage());
        assertEquals("待收款", vehicle.getLifecycleStageLabel());
        assertEquals("DRAFT", payment.getStageStatus());
        assertEquals("草稿", payment.getStageStatusLabel());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=BusinessStatusLabelFieldsTest test`

Expected: FAIL because the label accessors do not exist.

- [ ] **Step 3: Add the label fields**

Add these fields with Chinese `@Schema` descriptions:

```text
VehicleBasicInfo: lifecycleStageLabel
VehicleListResponse: lifecycleStageLabel, productionStatusLabel
ProductionResponse: stageStatusLabel
AllocationResponse: stageStatusLabel, lifecycleStageLabel, salesStatusLabel
InvoiceListResponse: lifecycleStageLabel, latestStageStatusLabel, latestInvoiceTypeLabel
InvoiceResponse: stageStatusLabel, invoiceTypeLabel
PaymentResponse: lifecycleStageLabel, stageStatusLabel, paymentStatusLabel
RegistrationResponse: lifecycleStageLabel, stageStatusLabel, drosstechStatusLabel
TransportOrderListResponse: orderStatusLabel
TransportOrderDetailResponse: orderStatusLabel
DispatchListResponse: listStatusLabel
WaybillDealerResponse: deliveryStatusLabel, rowStatusLabel
VehiclePanoramaResponse.TimelineNode: stageLabel
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=BusinessStatusLabelFieldsTest test`

Expected: PASS with 1 test and 0 failures.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/com/company/admin/dto/response src/test/java/com/company/admin/dto/response/BusinessStatusLabelFieldsTest.java
git commit -m "feat: add Chinese label fields to business responses"
```

### Task 3: 填充车辆与阶段响应标签

**Files:**
- Modify: `src/main/java/com/company/admin/service/impl/VehicleBasicServiceImpl.java`
- Modify: `src/main/java/com/company/admin/service/impl/VehicleServiceImpl.java`
- Modify: `src/main/java/com/company/admin/service/impl/VehProductionServiceImpl.java`
- Modify: `src/main/java/com/company/admin/service/impl/VehAllocationServiceImpl.java`
- Modify: `src/main/java/com/company/admin/service/impl/VehInvoiceServiceImpl.java`
- Modify: `src/main/java/com/company/admin/service/impl/VehPaymentServiceImpl.java`
- Modify: `src/main/java/com/company/admin/service/impl/VehRegistrationServiceImpl.java`
- Create: `src/test/java/com/company/admin/service/impl/VehicleBasicServiceImplTest.java`
- Create: `src/test/java/com/company/admin/service/impl/VehicleServiceImplTest.java`
- Modify: `src/test/java/com/company/admin/service/impl/VehProductionServiceImplTest.java`
- Modify: `src/test/java/com/company/admin/service/impl/VehAllocationServiceImplTest.java`
- Modify: `src/test/java/com/company/admin/service/impl/VehInvoiceServiceImplTest.java`
- Modify: `src/test/java/com/company/admin/service/impl/VehPaymentServiceImplTest.java`
- Modify: `src/test/java/com/company/admin/service/impl/VehRegistrationServiceImplTest.java`

**Interfaces:**
- Consumes: `BusinessStatusLabelService` and the label fields from Task 2。
- Produces: Chinese labels in vehicle basic/list, production, allocation, invoice, payment and registration responses。

- [ ] **Step 1: Write failing service assertions**

Extend the existing service tests so mocked records with codes assert both fields, for example:

```java
@Test
void getPaymentAddsChineseLabelsAndKeepsPaymentCodes() {
    when(vehPaymentMapper.selectOne(any())).thenReturn(draftPayment());
    when(lifecycleService.hasValidFormalInvoice(99L)).thenReturn(true);
    when(vehicleBasicService.getBasicInfo(99L)).thenReturn(basicInfo("PENDING_PAYMENT"));
    when(statusLabelService.lifecycleStageLabel("PENDING_PAYMENT")).thenReturn("待收款");
    when(statusLabelService.stageStatusLabel("DRAFT")).thenReturn("草稿");
    when(statusLabelService.dictLabel("payment_status", "UNPAID")).thenReturn("未收款");

    PaymentResponse response = service.getPayment(99L);

    assertEquals("PENDING_PAYMENT", response.getLifecycleStage());
    assertEquals("待收款", response.getLifecycleStageLabel());
    assertEquals("DRAFT", response.getStageStatus());
    assertEquals("草稿", response.getStageStatusLabel());
    assertEquals("未收款", response.getPaymentStatusLabel());
}
```

Add equivalent assertions for lifecycle lists, production, allocation, invoice and registration. Use `@Mock BusinessStatusLabelService` in existing Mockito tests.

- [ ] **Step 2: Run the focused tests to verify they fail**

Run: `mvn -q -Dtest=VehPaymentServiceImplTest,VehRegistrationServiceImplTest,VehProductionServiceImplTest,VehAllocationServiceImplTest test`

Expected: FAIL because the services do not yet inject or call the resolver and the new labels remain null.

- [ ] **Step 3: Implement response enrichment**

Inject `BusinessStatusLabelService` into each service with constructor injection. Add a small `applyLabels` method beside each existing response conversion method:

```java
private void applyLabels(PaymentResponse response) {
    response.setLifecycleStageLabel(statusLabelService.lifecycleStageLabel(response.getLifecycleStage()));
    response.setStageStatusLabel(statusLabelService.stageStatusLabel(response.getStageStatus()));
    response.setPaymentStatusLabel(statusLabelService.dictLabel("payment_status", response.getPaymentStatus()));
}
```

Apply the same pattern to every response listed in Task 2. For `stageStatus` values that may contain a lifecycle code (`PENDING_ALLOCATION`, `PENDING_PAYMENT`, or `PENDING_REGISTRATION`), make `stageStatusLabel` resolve `StageStatus` first and `LifecycleStage` second. For paged responses, apply the method to every record after Mapper retrieval. For invoice list rows, label lifecycle, latest stage status and invoice type.

- [ ] **Step 4: Run focused tests to verify they pass**

Run: `mvn -q -Dtest=VehPaymentServiceImplTest,VehRegistrationServiceImplTest,VehProductionServiceImplTest,VehAllocationServiceImplTest,VehInvoiceServiceImplTest test`

Expected: PASS with 0 failures.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/com/company/admin/service/impl/VehicleBasicServiceImpl.java src/main/java/com/company/admin/service/impl/VehicleServiceImpl.java src/main/java/com/company/admin/service/impl/VehProductionServiceImpl.java src/main/java/com/company/admin/service/impl/VehAllocationServiceImpl.java src/main/java/com/company/admin/service/impl/VehInvoiceServiceImpl.java src/main/java/com/company/admin/service/impl/VehPaymentServiceImpl.java src/main/java/com/company/admin/service/impl/VehRegistrationServiceImpl.java src/test/java/com/company/admin/service/impl
git commit -m "feat: label vehicle lifecycle responses"
```

### Task 4: 填充物流、配送与全景响应标签

**Files:**
- Modify: `src/main/java/com/company/admin/service/impl/TransportOrderServiceImpl.java`
- Modify: `src/main/java/com/company/admin/service/impl/DispatchListServiceImpl.java`
- Modify: `src/main/java/com/company/admin/service/impl/VehiclePanoramaServiceImpl.java`
- Modify: `src/test/java/com/company/admin/service/impl/TransportOrderServiceImplTest.java`
- Modify: `src/test/java/com/company/admin/service/impl/DispatchListServiceImplTest.java`
- Modify: `src/test/java/com/company/admin/service/impl/VehiclePanoramaServiceImplTest.java`

**Interfaces:**
- Consumes: `BusinessStatusLabelService` and response fields from Task 2。
- Produces: labels for transport order, dispatch list, waybill dealer rows and timeline nodes。

- [ ] **Step 1: Write failing assertions**

Add tests asserting:

```java
assertEquals("草稿", detail.getOrderStatusLabel());
assertEquals("草稿", dispatch.getListStatusLabel());
assertEquals("运输中", dealer.getDeliveryStatusLabel());
assertEquals("已确认", dealer.getRowStatusLabel());
assertEquals("待收款", timelineNode.getStageLabel());
```

- [ ] **Step 2: Run focused tests to verify they fail**

Run: `mvn -q -Dtest=TransportOrderServiceImplTest,DispatchListServiceImplTest,VehiclePanoramaServiceImplTest test`

Expected: FAIL because logistics and panorama response labels are not populated.

- [ ] **Step 3: Implement logistics and panorama enrichment**

Inject `BusinessStatusLabelService` into the transport, dispatch and panorama services. Enrich list/detail DTOs after `BeanUtils.copyProperties`. Use `orderStatusLabel` for transport order status, `stageStatusLabel` for dispatch list and row status, and dictionary lookups for `delivery_status`. In `VehiclePanoramaServiceImpl`, enrich all nested responses after `fillRegistration` and set `TimelineNode.stageLabel` from the lifecycle stage code. Keep `TimelineNode.name` unchanged.

- [ ] **Step 4: Run focused tests to verify they pass**

Run: `mvn -q -Dtest=TransportOrderServiceImplTest,DispatchListServiceImplTest,VehiclePanoramaServiceImplTest test`

Expected: PASS with 0 failures.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/com/company/admin/service/impl/TransportOrderServiceImpl.java src/main/java/com/company/admin/service/impl/DispatchListServiceImpl.java src/main/java/com/company/admin/service/impl/VehiclePanoramaServiceImpl.java src/test/java/com/company/admin/service/impl
git commit -m "feat: label logistics and panorama statuses"
```

### Task 5: 将业务字典种子数据改为中文并增加回归测试

**Files:**
- Modify: `src/main/resources/sql/admin_system.sql`
- Modify: `src/test/java/com/company/admin/mapper/MapperSqlSmokeTest.java`

**Interfaces:**
- Consumes: existing dictionary type/item schema and codes。
- Produces: Chinese business dictionary labels while preserving all item values。

- [ ] **Step 1: Write failing seed-data assertions**

Add a `MapperSqlSmokeTest` assertion that the checked-in SQL seed contains these exact labels:

```java
assertEquals("正式发票", item("invoice_status", "INVOICED").getItemLabel());
assertEquals("形式发票", item("invoice_status", "PROFORMA_INVOICED").getItemLabel());
assertEquals("未收款", item("payment_status", "UNPAID").getItemLabel());
assertEquals("运输中", item("delivery_status", "IN_TRANSIT").getItemLabel());
assertEquals("待上传", item("drosstech_status", "PENDING").getItemLabel());
assertEquals("已分配", item("sales_status", "ALLOCATED").getItemLabel());
```

- [ ] **Step 2: Run the focused regression test to verify it fails**

Run: `mvn -q -Dtest=MapperSqlSmokeTest test`

Expected: FAIL because the checked-in SQL seed still contains the current English labels.

- [ ] **Step 3: Update SQL seed labels**

Change only the `dict_name`, `remark` and `item_label` values for the five business dictionaries in `src/main/resources/sql/admin_system.sql`. Keep IDs, `dict_code`, `item_value`, sort order, status and timestamps unchanged.

- [ ] **Step 4: Run focused regression tests to verify they pass**

Run: `mvn -q -Dtest=MapperSqlSmokeTest test`

Expected: PASS with 0 failures.

- [ ] **Step 5: Commit**

```powershell
git add src/main/resources/sql/admin_system.sql src/test/java/com/company/admin/service/impl/DictServiceImplTest.java src/test/java/com/company/admin/mapper/MapperSqlSmokeTest.java
git commit -m "data: localize business dictionary labels"
```

### Task 6: 全量验证并检查未覆盖的英文业务状态

**Files:**
- Modify only if verification exposes an in-scope omission: relevant response/service/test files above。

- [ ] **Step 1: Search for remaining raw business status responses**

Run:

```powershell
rg -n "private String (lifecycleStage|stageStatus|orderStatus|listStatus|deliveryStatus|rowStatus|paymentStatus|salesStatus|drosstechStatus|invoiceType)|set(Label)?(LifecycleStage|StageStatus|OrderStatus|ListStatus|DeliveryStatus|RowStatus|PaymentStatus|SalesStatus|DrosstechStatus|InvoiceType)" src/main/java/com/company/admin/dto/response src/main/java/com/company/admin/service/impl
```

Expected: Every in-scope response code field has its matching `*Label` field and an enrichment call; system management `Integer status` fields are the only intentionally untouched status fields.

- [ ] **Step 2: Run formatting and compile checks**

Run: `mvn -q -DskipTests compile`

Expected: BUILD SUCCESS.

- [ ] **Step 3: Run the complete test suite**

Run: `mvn -q test`

Expected: BUILD SUCCESS with 0 failures.

- [ ] **Step 4: Inspect the final diff and working tree**

Run: `git diff --check; git status --short; git diff --stat HEAD~6..HEAD`

Expected: no whitespace errors; only the planned files are committed, while unrelated pre-existing untracked files remain untouched.

- [ ] **Step 5: Commit any final in-scope correction**

If the preceding checks expose an in-scope omission, stage the exact corrected files from Tasks 1–5 and commit them with:

```powershell
git commit -m "test: verify Chinese business status labels"
```
