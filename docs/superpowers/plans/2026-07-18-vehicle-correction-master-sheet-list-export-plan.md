# Vehicle Correction Master Sheet List and Export Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the administrator vehicle-correction list expose the 45 `Sample of Master Sheet.xlsx` columns and add a filtered full-data Excel export with the same two-row header structure.

**Architecture:** Introduce a correction-list-specific flat DTO and one reusable aggregate SQL query joining all vehicle stages, with invoice sequence 1 and 2 joined separately. The service performs batched dictionary-label conversion and numbering for both pagination and export, while a focused POI exporter owns the exact workbook layout. The existing correction detail/update paths remain unchanged.

**Tech Stack:** Java 17, Spring Boot 2.7.18, Spring Security, MyBatis-Plus 3.5.5, MySQL/H2, Apache POI via EasyExcel 3.3.4, JUnit 5, Mockito, MockMvc, Maven.

## Global Constraints

- Preserve all 45 first-row header strings, including capitalization, spaces, line breaks, and existing spellings from `docs/Sample of Master Sheet.xlsx`.
- Preserve the second-row department labels and merges `B2:N2`, `O2:Q2`, `R2:U2`, `V2:Y2`, `Z2:AC2`, `AD2:AG2`, `AH2:AN2`, and `AO2:AS2`.
- `GET /api/vehicle-corrections/export` exports all rows matching the current filters and ignores `pageNum` and `pageSize`.
- The list and export share one field mapping and one display-label conversion path.
- Missing stage records remain blank and never remove the vehicle from the result.
- VIN, correction detail, correction update, workflow state, and audit behavior remain unchanged.
- Export permission is `vlm:vehicle-correction:export` and is seeded only for role `ADMIN` (`role_id=1`).
- Do not modify or commit the existing staged `.DS_Store` files, the modified `docs/Sample of Master Sheet.xlsx`, `.codex/`, `.gstack/`, or unrelated untracked documents.

---

## File Structure

- Create `src/main/java/com/company/admin/dto/response/VehicleCorrectionListResponse.java`: typed API/export row contract for the 45 visible columns plus hidden mapper state.
- Create `src/main/java/com/company/admin/export/VehicleCorrectionExcelExporter.java`: service-facing export contract.
- Create `src/main/java/com/company/admin/export/PoiVehicleCorrectionExcelExporter.java`: workbook construction, headers, merges, styles, widths, and row serialization.
- Create `src/test/java/com/company/admin/export/PoiVehicleCorrectionExcelExporterTest.java`: structural Excel regression tests.
- Modify `src/main/java/com/company/admin/mapper/VehicleMapper.java`: paged and unpaged correction aggregate query signatures.
- Modify `src/main/resources/mapper/vlm/VehicleMapper.xml`: shared aggregate select, joins, filters, and two invoice aliases.
- Modify `src/test/java/com/company/admin/mapper/MapperSqlSmokeTest.java`: H2 integration coverage for all stage joins and invoice de-duplication.
- Modify `src/main/java/com/company/admin/service/VehicleCorrectionService.java`: corrected list type and export method.
- Modify `src/main/java/com/company/admin/service/impl/VehicleCorrectionServiceImpl.java`: numbering, batched labels, all-filtered export, exporter delegation.
- Modify `src/test/java/com/company/admin/service/impl/VehicleCorrectionServiceImplTest.java`: service-level pagination/export behavior.
- Modify `src/main/java/com/company/admin/controller/VehicleCorrectionController.java`: response contract and export endpoint.
- Modify `src/test/java/com/company/admin/controller/VehicleCorrectionControllerTest.java`: response fields, headers, and permission boundary.
- Modify `src/main/resources/sql/d00004_vehicle_stage_todo_correction_migration.sql`: idempotent export permission menu and ADMIN assignment.
- Modify `src/main/resources/sql/admin_system.sql`: full seed export permission and ADMIN mapping.
- Modify `src/test/java/com/company/admin/sql/VehicleCorrectionMenuSqlTest.java`: seed/migration permission regression.
- Modify `docs/api-document.md`: changed list response and new export endpoint.
- Modify `docs/车辆阶段待办与数据修订后端接口变更清单.md`: interface change-list entry and 45-column contract.

---

### Task 1: Add the 45-column row contract and aggregate mapper query

**Files:**
- Create: `src/main/java/com/company/admin/dto/response/VehicleCorrectionListResponse.java`
- Modify: `src/main/java/com/company/admin/mapper/VehicleMapper.java`
- Modify: `src/main/resources/mapper/vlm/VehicleMapper.xml`
- Modify: `src/test/java/com/company/admin/mapper/MapperSqlSmokeTest.java`

**Interfaces:**
- Produces: `Page<VehicleCorrectionListResponse> selectVehicleCorrectionPage(Page<VehicleCorrectionListResponse>, VehicleQueryRequest)`.
- Produces: `List<VehicleCorrectionListResponse> selectVehicleCorrections(VehicleQueryRequest)`.
- Produces: `VehicleCorrectionListResponse` with `id`, `no`, 44 remaining visible master-sheet properties, and `fullyLoadValue` hidden from JSON.

- [ ] **Step 1: Write the failing aggregate mapper test**

Add an import for `VehicleCorrectionListResponse` and this test to `MapperSqlSmokeTest`:

```java
@Test
void correctionMasterSheetQueryMapsAllStagesAndBothInvoiceSequencesOnce() {
    jdbcTemplate.update("INSERT INTO t_vehicle (id, vin, lifecycle_stage, deleted) VALUES (90, 'VIN00000000000090', 'COMPLETED', 0)");
    jdbcTemplate.update("INSERT INTO t_md_model (id, model_name, series, model_code, deleted) VALUES (901, 'MG S5', 'S5', 'ZS3EMA', 0)");
    jdbcTemplate.update("INSERT INTO t_md_exterior_color (id, color_name, deleted) VALUES (902, 'SILVER', 0)");
    jdbcTemplate.update("INSERT INTO t_md_interior_color (id, color_name, deleted) VALUES (903, 'BLACK', 0)");
    jdbcTemplate.update("INSERT INTO t_md_dealer (id, dealer_code, dealer_name, deleted) VALUES (904, '290933', 'SING HUAT', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_production (vehicle_id, stage_status, model_id, exterior_color_id, interior_color_id, engine_number, year_make, material, shipment, batch, offline_epmb_date, epmb_ok_date, remark1, deleted) VALUES (90, 'CONFIRMED', 901, 902, 903, 'ENG-90', '2026', 'MAT-90', 'SHP-90', 'BATCH-90', '2026-05-22', '2026-06-04', 'P-90', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_inbound (vehicle_id, stage_status, saic_buy_off_date, date_to_storage_yard, remark2, deleted) VALUES (90, 'CONFIRMED', '2026-06-04', '2026-06-05', 'I-90', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_allocation (vehicle_id, stage_status, allocated_date, dealer_id, remark3, deleted) VALUES (90, 'CONFIRMED', '2026-06-05', 904, 'A-90', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_invoice (vehicle_id, stage_status, invoice_seq, invoice_type, invoice_no, invoice_date, remark, deleted) VALUES (90, 'CONFIRMED', 1, 'PROFORMA_INVOICED', 'PF-90', '2026-06-06', 'F1-90', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_invoice (vehicle_id, stage_status, invoice_seq, invoice_type, invoice_no, invoice_date, remark, deleted) VALUES (90, 'CONFIRMED', 2, 'INVOICED', 'IV-90', '2026-06-10', 'F2-90', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_payment (vehicle_id, stage_status, payment_date, credit_full_payment_date, payment_status, remark5, deleted) VALUES (90, 'CONFIRMED', '2026-06-10', '2026-06-11', 'PAID', 'PAY-90', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_delivery (vehicle_id, stage_status, etd_to_dealer, eta_to_dealer, trolly_type, fully_load, received_date, delivery_status, remark7, deleted) VALUES (90, 'CONFIRMED', '2026-06-12', '2026-06-13', '6 units', 1, '2026-06-13', 'DELIVERED', 'D-90', 0)");
    jdbcTemplate.update("INSERT INTO t_veh_registration (vehicle_id, stage_status, drosstech_status, upload_date, registration_date, customer_region, remark8, deleted) VALUES (90, 'CONFIRMED', 'UPLOADED', '2026-06-14', '2026-06-25', 'Kuala Lumpur', 'R-90', 0)");

    VehicleQueryRequest query = new VehicleQueryRequest();
    query.setVin("00000000000090");
    List<VehicleCorrectionListResponse> rows = vehicleMapper.selectVehicleCorrections(query);

    assertEquals(1, rows.size());
    VehicleCorrectionListResponse row = rows.get(0);
    assertEquals(90L, row.getId());
    assertEquals("MG S5", row.getModel());
    assertEquals("SILVER", row.getExteriorColor());
    assertEquals("BLACK", row.getInteriorColor());
    assertEquals("VIN00000000000090", row.getVinNumber());
    assertEquals("PF-90", row.getInvoiceNo1());
    assertEquals("IV-90", row.getInvoiceNo2());
    assertEquals(Boolean.TRUE, row.getFullyLoadValue());
    assertEquals("Kuala Lumpur", row.getCustomerRegion());
}
```

- [ ] **Step 2: Run the mapper test to verify it fails**

Run:

```powershell
mvn -Dtest=MapperSqlSmokeTest#correctionMasterSheetQueryMapsAllStagesAndBothInvoiceSequencesOnce test
```

Expected: compilation failure because `VehicleCorrectionListResponse` and `selectVehicleCorrections` do not exist.

- [ ] **Step 3: Create the typed row DTO**

Create `VehicleCorrectionListResponse.java` with Lombok accessors, schema descriptions, stable JSON order, `LocalDate` date fields, and this exact property order:

```java
package com.company.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Vehicle correction master-sheet row")
@JsonPropertyOrder({"id", "no", "model", "exteriorColor", "interiorColor", "vinNumber",
        "engineNumber", "modelCode", "yearMake", "material", "shipment", "batch",
        "offlineEpmb", "epmbOk", "remark1", "saicBuyOff", "dateToStorageYard", "remark2",
        "allocatedDate", "dealerCode", "dealer", "remark3", "status1", "invoiceNo1",
        "invoiceDate1", "remark4", "paymentDate", "creditFullPaymentDate", "paymentStatus",
        "remark5", "status2", "invoiceNo2", "invoiceDate2", "remark6", "etdToDealer",
        "etaToDealer", "trollyType", "fullyLoad", "receivedDateByDealer", "deliveryStatus",
        "remark7", "drosstechStatus", "uploadDate", "registration", "customerRegion", "remark8"})
public class VehicleCorrectionListResponse {
    private Long id;
    private Long no;
    private String model;
    private String exteriorColor;
    private String interiorColor;
    private String vinNumber;
    private String engineNumber;
    private String modelCode;
    private String yearMake;
    private String material;
    private String shipment;
    private String batch;
    private LocalDate offlineEpmb;
    private LocalDate epmbOk;
    private String remark1;
    private LocalDate saicBuyOff;
    private LocalDate dateToStorageYard;
    private String remark2;
    private LocalDate allocatedDate;
    private String dealerCode;
    private String dealer;
    private String remark3;
    private String status1;
    private String invoiceNo1;
    private LocalDate invoiceDate1;
    private String remark4;
    private LocalDate paymentDate;
    private LocalDate creditFullPaymentDate;
    private String paymentStatus;
    private String remark5;
    private String status2;
    private String invoiceNo2;
    private LocalDate invoiceDate2;
    private String remark6;
    private LocalDate etdToDealer;
    private LocalDate etaToDealer;
    private String trollyType;
    private String fullyLoad;
    @JsonIgnore
    private Boolean fullyLoadValue;
    private LocalDate receivedDateByDealer;
    private String deliveryStatus;
    private String remark7;
    private String drosstechStatus;
    private LocalDate uploadDate;
    private LocalDate registration;
    private String customerRegion;
    private String remark8;
}
```

Add `@Schema(description = "<exact master-sheet header>")` to every visible property while implementing; use the exact header strings listed in the design spec, including the newline in the Trolly header.

- [ ] **Step 4: Replace the correction mapper contract and add the unpaged contract**

In `VehicleMapper.java`, import the new DTO and replace the old correction signature with:

```java
Page<VehicleCorrectionListResponse> selectVehicleCorrectionPage(
        Page<VehicleCorrectionListResponse> page,
        @Param("query") VehicleQueryRequest query);

List<VehicleCorrectionListResponse> selectVehicleCorrections(
        @Param("query") VehicleQueryRequest query);
```

- [ ] **Step 5: Implement one shared aggregate SQL select**

In `VehicleMapper.xml`, add `VehicleCorrectionColumns` and `VehicleCorrectionJoins` fragments. Select aliases must match the DTO properties:

```xml
<sql id="VehicleCorrectionColumns">
    SELECT v.id,
           m.model_name AS model,
           ec.color_name AS exterior_color,
           ic.color_name AS interior_color,
           v.vin AS vin_number,
           p.engine_number,
           m.model_code,
           p.year_make,
           p.material,
           p.shipment,
           p.batch,
           p.offline_epmb_date AS offline_epmb,
           p.epmb_ok_date AS epmb_ok,
           p.remark1,
           ib.saic_buy_off_date AS saic_buy_off,
           ib.date_to_storage_yard,
           ib.remark2,
           a.allocated_date,
           d.dealer_code,
           d.dealer_name AS dealer,
           a.remark3,
           i1.invoice_type AS status1,
           i1.invoice_no AS invoice_no1,
           i1.invoice_date AS invoice_date1,
           i1.remark AS remark4,
           pay.payment_date,
           pay.credit_full_payment_date,
           pay.payment_status,
           pay.remark5,
           i2.invoice_type AS status2,
           i2.invoice_no AS invoice_no2,
           i2.invoice_date AS invoice_date2,
           i2.remark AS remark6,
           dl.etd_to_dealer,
           dl.eta_to_dealer,
           dl.trolly_type,
           dl.fully_load AS fully_load_value,
           dl.received_date AS received_date_by_dealer,
           dl.delivery_status,
           dl.remark7,
           r.drosstech_status,
           r.upload_date,
           r.registration_date AS registration,
           r.customer_region,
           r.remark8
</sql>

<sql id="VehicleCorrectionJoins">
    FROM t_vehicle v
    LEFT JOIN t_veh_production p ON p.vehicle_id = v.id AND p.deleted = 0
    LEFT JOIN t_md_model m ON m.id = p.model_id AND m.deleted = 0
    LEFT JOIN t_md_exterior_color ec ON ec.id = p.exterior_color_id AND ec.deleted = 0
    LEFT JOIN t_md_interior_color ic ON ic.id = p.interior_color_id AND ic.deleted = 0
    LEFT JOIN t_veh_inbound ib ON ib.vehicle_id = v.id AND ib.deleted = 0
    LEFT JOIN t_veh_allocation a ON a.vehicle_id = v.id AND a.deleted = 0
    LEFT JOIN t_md_dealer d ON d.id = a.dealer_id AND d.deleted = 0
    LEFT JOIN t_veh_invoice i1 ON i1.vehicle_id = v.id AND i1.invoice_seq = 1 AND i1.deleted = 0
    LEFT JOIN t_veh_payment pay ON pay.vehicle_id = v.id AND pay.deleted = 0
    LEFT JOIN t_veh_invoice i2 ON i2.vehicle_id = v.id AND i2.invoice_seq = 2 AND i2.deleted = 0
    LEFT JOIN t_veh_delivery dl ON dl.vehicle_id = v.id AND dl.deleted = 0
    LEFT JOIN t_veh_registration r ON r.vehicle_id = v.id AND r.deleted = 0
</sql>
```

Use the existing `VehiclePageFilters` fragment, then implement both statements:

```xml
<select id="selectVehicleCorrectionPage" resultType="com.company.admin.dto.response.VehicleCorrectionListResponse">
    <include refid="VehicleCorrectionColumns"/>
    <include refid="VehicleCorrectionJoins"/>
    WHERE v.deleted = 0
    <include refid="VehiclePageFilters"/>
    ORDER BY v.id DESC
</select>

<select id="selectVehicleCorrections" resultType="com.company.admin.dto.response.VehicleCorrectionListResponse">
    <include refid="VehicleCorrectionColumns"/>
    <include refid="VehicleCorrectionJoins"/>
    WHERE v.deleted = 0
    <include refid="VehiclePageFilters"/>
    ORDER BY v.id DESC
</select>
```

- [ ] **Step 6: Run the mapper tests**

Run:

```powershell
mvn -Dtest=MapperSqlSmokeTest#correctionMasterSheetQueryMapsAllStagesAndBothInvoiceSequencesOnce test
```

Expected: PASS with one vehicle row even though two invoice records exist.

- [ ] **Step 7: Commit Task 1**

```powershell
git add -- src/main/java/com/company/admin/dto/response/VehicleCorrectionListResponse.java src/main/java/com/company/admin/mapper/VehicleMapper.java src/main/resources/mapper/vlm/VehicleMapper.xml src/test/java/com/company/admin/mapper/MapperSqlSmokeTest.java
git commit -m "feat: query correction master sheet rows"
```

---

### Task 2: Share numbering and display conversion between list and export

**Files:**
- Create: `src/main/java/com/company/admin/export/VehicleCorrectionExcelExporter.java`
- Modify: `src/main/java/com/company/admin/service/VehicleCorrectionService.java`
- Modify: `src/main/java/com/company/admin/service/impl/VehicleCorrectionServiceImpl.java`
- Modify: `src/test/java/com/company/admin/service/impl/VehicleCorrectionServiceImplTest.java`

**Interfaces:**
- Consumes: the two mapper methods from Task 1.
- Produces: `VehicleCorrectionExcelExporter.export(List<VehicleCorrectionListResponse>)`, the service/export boundary implemented in Task 3.
- Produces: `PageResult<VehicleCorrectionListResponse> pageCorrections(VehicleQueryRequest)`.
- Produces: `byte[] exportCorrections(VehicleQueryRequest)`.

- [ ] **Step 1: Replace the old list test and add an export-scope test**

Update the service test to use `VehicleCorrectionListResponse`, mock `VehicleCorrectionExcelExporter`, pass it to the constructor, and add:

```java
@Test
void pageCorrectionsAddsGlobalNumberAndBatchedDisplayLabels() {
    VehicleQueryRequest request = new VehicleQueryRequest();
    request.setPageNum(3);
    request.setPageSize(10);
    VehicleCorrectionListResponse row = new VehicleCorrectionListResponse();
    row.setStatus1("PROFORMA_INVOICED");
    row.setStatus2("INVOICED");
    row.setPaymentStatus("PAID");
    row.setDeliveryStatus("DELIVERED");
    row.setDrosstechStatus("UPLOADED");
    row.setFullyLoadValue(Boolean.TRUE);
    Page<VehicleCorrectionListResponse> page = new Page<>(3, 10);
    page.setRecords(List.of(row));
    page.setTotal(21);
    when(vehicleMapper.selectVehicleCorrectionPage(any(), eq(request))).thenReturn(page);
    when(statusLabelService.dictLabels("invoice_status")).thenReturn(Map.of(
            "PROFORMA_INVOICED", "Proforma Invoiced", "INVOICED", "Invoiced"));
    when(statusLabelService.dictLabels("payment_status")).thenReturn(Map.of("PAID", "Paid"));
    when(statusLabelService.dictLabels("delivery_status")).thenReturn(Map.of("DELIVERED", "Delivered"));
    when(statusLabelService.dictLabels("drosstech_status")).thenReturn(Map.of("UPLOADED", "Uploaded"));

    PageResult<VehicleCorrectionListResponse> result = service.pageCorrections(request);

    assertEquals(21L, result.getList().get(0).getNo());
    assertEquals("Proforma Invoiced", result.getList().get(0).getStatus1());
    assertEquals("Invoiced", result.getList().get(0).getStatus2());
    assertEquals("Paid", result.getList().get(0).getPaymentStatus());
    assertEquals("Delivered", result.getList().get(0).getDeliveryStatus());
    assertEquals("Uploaded", result.getList().get(0).getDrosstechStatus());
    assertEquals("Full", result.getList().get(0).getFullyLoad());
}

@Test
void exportCorrectionsUsesAllFilteredRowsAndIgnoresPagination() {
    VehicleQueryRequest request = new VehicleQueryRequest();
    request.setPageNum(9);
    request.setPageSize(5);
    request.setVin("VIN90");
    VehicleCorrectionListResponse row = new VehicleCorrectionListResponse();
    when(vehicleMapper.selectVehicleCorrections(request)).thenReturn(List.of(row));
    when(vehicleCorrectionExcelExporter.export(List.of(row))).thenReturn(new byte[]{1, 2, 3});

    assertArrayEquals(new byte[]{1, 2, 3}, service.exportCorrections(request));
    assertEquals(1L, row.getNo());
    verify(vehicleMapper).selectVehicleCorrections(request);
    verify(vehicleMapper, never()).selectVehicleCorrectionPage(any(), any());
}
```

- [ ] **Step 2: Run the service tests to verify they fail**

Run:

```powershell
mvn -Dtest=VehicleCorrectionServiceImplTest test
```

Expected: compilation failure until the service signatures and exporter dependency exist.

- [ ] **Step 3: Change the service contract**

Replace the old list return type and add export:

```java
PageResult<VehicleCorrectionListResponse> pageCorrections(VehicleQueryRequest request);

byte[] exportCorrections(VehicleQueryRequest request);
```

Create the exporter contract so this task compiles independently:

```java
package com.company.admin.export;

import com.company.admin.dto.response.VehicleCorrectionListResponse;

import java.util.List;

public interface VehicleCorrectionExcelExporter {
    byte[] export(List<VehicleCorrectionListResponse> rows);
}
```

- [ ] **Step 4: Implement one batch transformation path**

Inject `VehicleCorrectionExcelExporter`. Replace `pageCorrections` and add export plus helpers:

```java
@Override
public PageResult<VehicleCorrectionListResponse> pageCorrections(VehicleQueryRequest request) {
    Page<VehicleCorrectionListResponse> page = vehicleMapper.selectVehicleCorrectionPage(
            new Page<>(request.getPageNum(), request.getPageSize()), request);
    long firstNo = ((long) request.getPageNum() - 1L) * request.getPageSize() + 1L;
    prepareRows(page.getRecords(), firstNo);
    return new PageResult<>(page.getRecords(), page.getTotal(), request.getPageNum(), request.getPageSize());
}

@Override
public byte[] exportCorrections(VehicleQueryRequest request) {
    List<VehicleCorrectionListResponse> rows = vehicleMapper.selectVehicleCorrections(request);
    prepareRows(rows, 1L);
    return vehicleCorrectionExcelExporter.export(rows);
}

private void prepareRows(List<VehicleCorrectionListResponse> rows, long firstNo) {
    if (rows == null || rows.isEmpty()) {
        return;
    }
    Map<String, String> invoiceLabels = statusLabelService.dictLabels("invoice_status");
    Map<String, String> paymentLabels = statusLabelService.dictLabels("payment_status");
    Map<String, String> deliveryLabels = statusLabelService.dictLabels("delivery_status");
    Map<String, String> drosstechLabels = statusLabelService.dictLabels("drosstech_status");
    for (int index = 0; index < rows.size(); index++) {
        VehicleCorrectionListResponse row = rows.get(index);
        row.setNo(firstNo + index);
        row.setStatus1(label(invoiceLabels, row.getStatus1()));
        row.setStatus2(label(invoiceLabels, row.getStatus2()));
        row.setPaymentStatus(label(paymentLabels, row.getPaymentStatus()));
        row.setDeliveryStatus(label(deliveryLabels, row.getDeliveryStatus()));
        row.setDrosstechStatus(label(drosstechLabels, row.getDrosstechStatus()));
        row.setFullyLoad(row.getFullyLoadValue() == null
                ? null : Boolean.TRUE.equals(row.getFullyLoadValue()) ? "Full" : "Not Full");
    }
}

private String label(Map<String, String> labels, String value) {
    return value == null ? null : labels.getOrDefault(value, value);
}
```

Remove the correction-list use of the old `applyLabels(VehicleListResponse)` method but retain all update/detail helpers.

- [ ] **Step 5: Run the service tests**

Run:

```powershell
mvn -Dtest=VehicleCorrectionServiceImplTest test
```

Expected: PASS, including existing correction update and audit tests.

- [ ] **Step 6: Commit Task 2**

```powershell
git add -- src/main/java/com/company/admin/export/VehicleCorrectionExcelExporter.java src/main/java/com/company/admin/service/VehicleCorrectionService.java src/main/java/com/company/admin/service/impl/VehicleCorrectionServiceImpl.java src/test/java/com/company/admin/service/impl/VehicleCorrectionServiceImplTest.java
git commit -m "feat: prepare correction list and export rows"
```

---

### Task 3: Generate the exact two-row master-sheet workbook

**Files:**
- Create: `src/main/java/com/company/admin/export/PoiVehicleCorrectionExcelExporter.java`
- Create: `src/test/java/com/company/admin/export/PoiVehicleCorrectionExcelExporterTest.java`

**Interfaces:**
- Consumes: display-ready `VehicleCorrectionListResponse` rows from Task 2.
- Implements: `VehicleCorrectionExcelExporter.export(List<VehicleCorrectionListResponse> rows)`.

- [ ] **Step 1: Write structural workbook regression tests**

Create `PoiVehicleCorrectionExcelExporterTest` and verify the exact contract:

```java
@Test
void exportPreservesMasterSheetHeadersMergesAndDataStartRow() throws Exception {
    VehicleCorrectionListResponse row = new VehicleCorrectionListResponse();
    row.setNo(1L);
    row.setModel("MG S5");
    row.setVinNumber("VIN00000000000090");
    row.setOfflineEpmb(LocalDate.of(2026, 5, 22));
    row.setStatus1("Proforma Invoiced");
    row.setFullyLoad("Full");
    row.setRemark8("checked");

    byte[] bytes = new PoiVehicleCorrectionExcelExporter().export(List.of(row));

    try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
        Sheet sheet = workbook.getSheet("Sheet1");
        assertNotNull(sheet);
        assertEquals(45, sheet.getRow(0).getLastCellNum());
        assertEquals("NO.", sheet.getRow(0).getCell(0).getStringCellValue());
        assertEquals("Date to Strogare Yard", sheet.getRow(0).getCell(15).getStringCellValue());
        assertEquals("Trolly type\n4 units/ 6units", sheet.getRow(0).getCell(35).getStringCellValue());
        assertEquals("remark8", sheet.getRow(0).getCell(44).getStringCellValue());
        assertEquals("生产部门维护", sheet.getRow(1).getCell(1).getStringCellValue());
        assertEquals(8, sheet.getNumMergedRegions());
        assertTrue(mergedRegions(sheet).containsAll(List.of(
                "B2:N2", "O2:Q2", "R2:U2", "V2:Y2", "Z2:AC2", "AD2:AG2", "AH2:AN2", "AO2:AS2")));
        assertEquals("MG S5", sheet.getRow(2).getCell(1).getStringCellValue());
        assertEquals("VIN00000000000090", sheet.getRow(2).getCell(4).getStringCellValue());
        assertEquals("2026-05-22", sheet.getRow(2).getCell(11).getStringCellValue());
        assertEquals("checked", sheet.getRow(2).getCell(44).getStringCellValue());
    }
}

@Test
void emptyExportStillContainsBothHeaderRowsWithoutSampleData() throws Exception {
    byte[] bytes = new PoiVehicleCorrectionExcelExporter().export(List.of());
    try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
        Sheet sheet = workbook.getSheet("Sheet1");
        assertEquals(1, sheet.getLastRowNum());
        assertNull(sheet.getRow(2));
    }
}

@Test
void writePreviewWorkbookWhenPathProvided() throws Exception {
    String previewPath = System.getProperty("vehicleCorrection.previewPath");
    Assumptions.assumeTrue(previewPath != null && !previewPath.isBlank());
    Path path = Path.of(previewPath);
    Files.createDirectories(path.toAbsolutePath().getParent());
    VehicleCorrectionListResponse row = new VehicleCorrectionListResponse();
    row.setNo(1L);
    row.setModel("MG S5");
    row.setVinNumber("VIN00000000000090");
    row.setStatus1("Proforma Invoiced");
    row.setPaymentStatus("Paid");
    row.setDeliveryStatus("Delivered");
    row.setDrosstechStatus("Uploaded");
    Files.write(path, new PoiVehicleCorrectionExcelExporter().export(List.of(row)));
}
```

The test helper is:

```java
private List<String> mergedRegions(Sheet sheet) {
    return IntStream.range(0, sheet.getNumMergedRegions())
            .mapToObj(index -> sheet.getMergedRegion(index).formatAsString())
            .collect(Collectors.toList());
}
```

- [ ] **Step 2: Run the exporter test to verify it fails**

Run:

```powershell
mvn -Dtest=PoiVehicleCorrectionExcelExporterTest test
```

Expected: compilation failure because the exporter does not exist.

- [ ] **Step 3: Implement the exporter constants and workbook skeleton**

Create `PoiVehicleCorrectionExcelExporter`, annotate it with `@Component`, implement
`VehicleCorrectionExcelExporter`, and add these constants:

```java
private static final String[] HEADERS = {
        "NO.", "MODEL", "EXTERIOR COLOR", "INTERIOR COLOR", "VIN NUMBER", "ENGINE NUMBER",
        "MODEL CODE", "Year Make", "Material", "Shipment", "Batch ", "Offline EPMB", "EPMB ok ",
        "Remark1", "SAIC buy off ", "Date to Strogare Yard", "remark2", "Allocated Date", "Dealer Code",
        "Dealer", "Remark3", "Status1", "Invoice#", "Invoice Date", "remark4", "Payment Date",
        "Credit Full Payment Date", "Payment Status", "remark5", "Status2", "Invoice#", "Invoice Date",
        "remark6", "ETD  to Dealer", "ETA to Dealer ", "Trolly type\n4 units/ 6units",
        "Fully load or not", "Received date by Dealer ", "Delivery Status", "remark7", "Drosstech Status",
        "Upload Date", "Registration", "Customer region", "remark8"
};

private static final Group[] GROUPS = {
        new Group(1, 13, "生产部门维护", (short) 3, 0.74999),
        new Group(14, 16, "物流部门维护", (short) 2, -0.24998),
        new Group(17, 20, "销售部门维护", (short) 8, 0.59999),
        new Group(21, 24, "财务部门第一次维护（发票种类）", (short) 6, 0.59999),
        new Group(25, 28, "财务部门第二次维护（收款状态）", (short) 9, 0.59999),
        new Group(29, 32, "财务部门第三次维护（如果第一次发票为 Proforma Invoice）", (short) 9, 0.59999),
        new Group(33, 39, "物流部门负责维护", (short) 2, -0.24998),
        new Group(40, 44, "销售部门根据列X维护", (short) 8, 0.59999)
};

private record Group(int firstColumn, int lastColumn, String title, short theme, double tint) {}
```

Implement `export` with an `SXSSFWorkbook` row window of 100, `Sheet1`, frozen first two rows, header rows 0/1, data starting at row 2, and guaranteed `dispose()`:

```java
public byte[] export(List<VehicleCorrectionListResponse> rows) {
    SXSSFWorkbook workbook = new SXSSFWorkbook(100);
    try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
        Sheet sheet = workbook.createSheet("Sheet1");
        sheet.createFreezePane(0, 2);
        writeHeaders(workbook, sheet);
        writeRows(workbook, sheet, rows == null ? List.of() : rows);
        workbook.write(output);
        return output.toByteArray();
    } catch (IOException exception) {
        throw new IllegalStateException("Failed to export vehicle corrections", exception);
    } finally {
        workbook.dispose();
    }
}
```

- [ ] **Step 4: Implement exact row serialization and styles**

Use `Arrays.asList` so null cells are allowed, and serialize all 45 columns in exact order:

```java
private List<Object> values(VehicleCorrectionListResponse row) {
    return Arrays.asList(row.getNo(), row.getModel(), row.getExteriorColor(), row.getInteriorColor(),
            row.getVinNumber(), row.getEngineNumber(), row.getModelCode(), row.getYearMake(), row.getMaterial(),
            row.getShipment(), row.getBatch(), row.getOfflineEpmb(), row.getEpmbOk(), row.getRemark1(),
            row.getSaicBuyOff(), row.getDateToStorageYard(), row.getRemark2(), row.getAllocatedDate(),
            row.getDealerCode(), row.getDealer(), row.getRemark3(), row.getStatus1(), row.getInvoiceNo1(),
            row.getInvoiceDate1(), row.getRemark4(), row.getPaymentDate(), row.getCreditFullPaymentDate(),
            row.getPaymentStatus(), row.getRemark5(), row.getStatus2(), row.getInvoiceNo2(), row.getInvoiceDate2(),
            row.getRemark6(), row.getEtdToDealer(), row.getEtaToDealer(), row.getTrollyType(), row.getFullyLoad(),
            row.getReceivedDateByDealer(), row.getDeliveryStatus(), row.getRemark7(), row.getDrosstechStatus(),
            row.getUploadDate(), row.getRegistration(), row.getCustomerRegion(), row.getRemark8());
}
```

Write `LocalDate` as its ISO `yyyy-MM-dd` string, numbers as numeric cells, and all other non-null values as strings. Create reusable styles only once per workbook:

- Header row: Calibri 12 bold, black, white/theme-0 fill, thin black borders, centered, wrapped.
- Department row: bold, centered, wrapped, thin borders, theme/tint from `GROUPS`; use Microsoft YaHei 12 except Z:AN groups use 11 to match the template.
- Data rows: Calibri 11, thin light borders, vertically centered; date values already render as `yyyy-MM-dd` strings.
- Set row 0 height to 36 points and row 1 height to 26 points.
- Set column widths explicitly in the same order: `{8,24,18,18,24,22,18,12,20,20,14,16,14,18,16,24,18,16,16,32,28,18,18,16,18,16,24,18,18,18,18,16,18,16,16,22,18,24,18,18,18,16,16,20,18}` characters, capped at Excel's 255-character limit.

For every group, create cells across its entire second-row range, apply the group style, set the title only in the first cell, and then call:

```java
sheet.addMergedRegion(new CellRangeAddress(1, 1, group.firstColumn(), group.lastColumn()));
```

Create `A2` as a bordered blank cell so all 45 columns have a complete second header row.

- [ ] **Step 5: Run the exporter and service tests**

Run:

```powershell
mvn -Dtest=PoiVehicleCorrectionExcelExporterTest,VehicleCorrectionServiceImplTest test
```

Expected: PASS; the exported workbook has 45 columns, 8 merges, two header rows, and no sample rows.

- [ ] **Step 6: Commit Task 3**

```powershell
git add -- src/main/java/com/company/admin/export/PoiVehicleCorrectionExcelExporter.java src/test/java/com/company/admin/export/PoiVehicleCorrectionExcelExporterTest.java
git commit -m "feat: export correction master sheet workbook"
```

---

### Task 4: Expose the export endpoint and enforce its permission

**Files:**
- Modify: `src/main/java/com/company/admin/controller/VehicleCorrectionController.java`
- Modify: `src/test/java/com/company/admin/controller/VehicleCorrectionControllerTest.java`

**Interfaces:**
- Consumes: `VehicleCorrectionService.exportCorrections(VehicleQueryRequest)`.
- Produces: `GET /api/vehicle-corrections/export` with authority `vlm:vehicle-correction:export`.

- [ ] **Step 1: Write controller response and authorization tests**

Add tests:

```java
@Test
@WithMockUser(authorities = "vlm:vehicle-correction:export")
void exportPermissionDownloadsFilteredWorkbook() throws Exception {
    when(vehicleCorrectionService.exportCorrections(any())).thenReturn(new byte[]{1, 2, 3});

    mockMvc.perform(get("/api/vehicle-corrections/export")
                    .param("vin", "VIN90")
                    .param("pageNum", "9")
                    .param("pageSize", "5"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .andExpect(header().string("Content-Disposition",
                    Matchers.matchesPattern("attachment;.*vehicle-corrections-\\d{14}\\.xlsx.*")))
            .andExpect(content().bytes(new byte[]{1, 2, 3}));

    ArgumentCaptor<VehicleQueryRequest> captor = ArgumentCaptor.forClass(VehicleQueryRequest.class);
    verify(vehicleCorrectionService).exportCorrections(captor.capture());
    assertEquals("VIN90", captor.getValue().getVin());
}

@Test
@WithMockUser(authorities = "vlm:vehicle-correction:list")
void listPermissionCannotExport() throws Exception {
    mockMvc.perform(get("/api/vehicle-corrections/export"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode()));
    verify(vehicleCorrectionService, never()).exportCorrections(any());
}
```

Also update `listPermissionCanRead` to return `PageResult<VehicleCorrectionListResponse>` and assert one master-sheet field such as `$.data.list[0].vinNumber`.

- [ ] **Step 2: Run the controller tests to verify they fail**

Run:

```powershell
mvn -Dtest=VehicleCorrectionControllerTest test
```

Expected: FAIL because `/export` and the new response DTO are not wired.

- [ ] **Step 3: Implement the controller contract**

Change list return type to `Result<PageResult<VehicleCorrectionListResponse>>`. Add the static export mapping before the `/{vehicleId}` mapping:

```java
@GetMapping("/export")
@PreAuthorize("hasAuthority('vlm:vehicle-correction:export')")
public ResponseEntity<byte[]> export(VehicleQueryRequest request) {
    byte[] bytes = vehicleCorrectionService.exportCorrections(request);
    String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .format(LocalDateTime.now());
    ContentDisposition disposition = ContentDisposition.attachment()
            .filename("vehicle-corrections-" + timestamp + ".xlsx", StandardCharsets.UTF_8)
            .build();
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(bytes);
}
```

- [ ] **Step 4: Run the controller tests**

Run:

```powershell
mvn -Dtest=VehicleCorrectionControllerTest test
```

Expected: PASS for list, detail/edit authorization, export download, and export denial.

- [ ] **Step 5: Commit Task 4**

```powershell
git add -- src/main/java/com/company/admin/controller/VehicleCorrectionController.java src/test/java/com/company/admin/controller/VehicleCorrectionControllerTest.java
git commit -m "feat: expose vehicle correction export endpoint"
```

---

### Task 5: Seed the ADMIN-only export permission

**Files:**
- Modify: `src/main/resources/sql/d00004_vehicle_stage_todo_correction_migration.sql`
- Modify: `src/main/resources/sql/admin_system.sql`
- Modify: `src/test/java/com/company/admin/sql/VehicleCorrectionMenuSqlTest.java`

**Interfaces:**
- Produces: menu id `1122`, permission `vlm:vehicle-correction:export`, parent `122`.
- Produces: ADMIN role mapping to menu `1122` only.

- [ ] **Step 1: Add failing seed and migration assertions**

Extend `VehicleCorrectionMenuSqlTest` assertions:

```java
assertTrue(sql.contains("(1122, 122, '车辆修订导出', 3, NULL, 'vlm:vehicle-correction:export'"));
assertTrue(sql.contains("(294, 1, 1122)"));
assertFalse(Pattern.compile("VALUES \\(\\d+, (100|101|102|103|104|105), (122|1120|1121|1122)\\)")
        .matcher(sql).find());
```

For the migration test, assert it contains menu 1122 and an idempotent `SELECT 1, 1122 WHERE NOT EXISTS` assignment without an explicit join-table id.

- [ ] **Step 2: Run the SQL test to verify it fails**

Run:

```powershell
mvn -Dtest=VehicleCorrectionMenuSqlTest test
```

Expected: FAIL because menu 1122 is absent.

- [ ] **Step 3: Add export permission to both SQL paths**

In the incremental migration, add to the existing menu `VALUES` list:

```sql
(1122, 122, '车辆修订导出', 3, NULL, 'vlm:vehicle-correction:export', NULL, 1122, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
```

Add the idempotent ADMIN assignment:

```sql
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 1122
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 1122
);
```

In `admin_system.sql`, add the same menu row and full-seed mapping:

```sql
INSERT INTO `sys_menu` VALUES (1122, 122, '车辆修订导出', 3, NULL, 'vlm:vehicle-correction:export', NULL, 1122, 1, '2026-07-18 00:00:00', '2026-07-18 00:00:00', 0);
INSERT INTO `sys_role_menu` VALUES (294, 1, 1122);
```

- [ ] **Step 4: Run the SQL tests**

Run:

```powershell
mvn -Dtest=VehicleCorrectionMenuSqlTest test
```

Expected: PASS and no non-ADMIN correction permission assignment.

- [ ] **Step 5: Commit Task 5**

```powershell
git add -- src/main/resources/sql/d00004_vehicle_stage_todo_correction_migration.sql src/main/resources/sql/admin_system.sql src/test/java/com/company/admin/sql/VehicleCorrectionMenuSqlTest.java
git commit -m "feat: grant correction export to administrators"
```

---

### Task 6: Update API documentation and run full verification

**Files:**
- Modify: `docs/api-document.md`
- Modify: `docs/车辆阶段待办与数据修订后端接口变更清单.md`
- Test: all tests under `src/test/java`

**Interfaces:**
- Documents: changed `GET /api/vehicle-corrections` response.
- Documents: new `GET /api/vehicle-corrections/export` request, permission, and file response.

- [ ] **Step 1: Update the main API document**

In section 6.2, change the list return type to:

```text
Result<PageResult<VehicleCorrectionListResponse>>
```

Add a table listing `id` as non-display metadata and the 45 visible properties in the exact A:AS order from the design spec. Add the export subsection:

```markdown
#### GET /api/vehicle-corrections/export

**权限：** `vlm:vehicle-correction:export`

请求筛选参数与分页列表相同；`pageNum`、`pageSize` 不限制导出范围。接口导出当前筛选条件下的全部车辆，返回 `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` 文件。文件包含与 `Sample of Master Sheet.xlsx` 一致的 45 列第一层表头、部门分组第二层表头和 8 个合并区域，数据从第三行开始。
```

- [ ] **Step 2: Update the backend interface change list**

Change the correction list return type, add the export row to the endpoint summary, and include:

```markdown
| GET | `/api/vehicle-corrections/export` | 导出当前筛选条件下的全部车辆 | Excel 文件 | `vlm:vehicle-correction:export` |
```

Document that list/export share the 45-column order and that the export ignores pagination only, not the remaining filters.

- [ ] **Step 3: Run focused feature tests**

Run:

```powershell
mvn -Dtest=MapperSqlSmokeTest,VehicleCorrectionServiceImplTest,PoiVehicleCorrectionExcelExporterTest,VehicleCorrectionControllerTest,VehicleCorrectionMenuSqlTest test
```

Expected: all focused tests pass with 0 failures and 0 errors.

- [ ] **Step 4: Run the full test suite from a clean Maven test output**

Run:

```powershell
mvn clean test
```

Expected: all project tests pass with `BUILD SUCCESS`, 0 failures, and 0 errors.

- [ ] **Step 5: Inspect the generated workbook visually**

Generate a deterministic preview workbook:

```powershell
mvn "-Dtest=PoiVehicleCorrectionExcelExporterTest#writePreviewWorkbookWhenPathProvided" "-DvehicleCorrection.previewPath=target/vehicle-corrections-preview.xlsx" test
```

Use the Spreadsheets artifact tool to import `target/vehicle-corrections-preview.xlsx`, render
`Sheet1!A1:AS3` to PNG, and inspect the PNG with `view_image`. Verify:

- all 45 headers are readable;
- the department colors and merged ranges visually match `docs/Sample of Master Sheet.xlsx`;
- the Trolly header wraps onto two lines;
- data begins on row 3;
- there are no template sample vehicles.

Do not add the rendered preview or generated workbook to Git.

- [ ] **Step 6: Check the exact diff scope**

Run:

```powershell
git diff --check
git status --short
git diff --stat HEAD
```

Expected: no whitespace errors; only files listed in this plan are part of the feature diff; pre-existing staged/untracked user files remain untouched.

- [ ] **Step 7: Commit Task 6**

```powershell
git add -- docs/api-document.md docs/车辆阶段待办与数据修订后端接口变更清单.md
git commit -m "docs: document correction master sheet export"
```

- [ ] **Step 8: Final verification after the documentation commit**

Run:

```powershell
mvn test
git log --oneline -7
git status --short
```

Expected: Maven reports `BUILD SUCCESS`; the feature commits are present; unrelated user files are still uncommitted and unchanged.
