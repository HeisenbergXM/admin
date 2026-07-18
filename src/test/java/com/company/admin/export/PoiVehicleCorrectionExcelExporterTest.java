package com.company.admin.export;

import com.company.admin.dto.response.VehicleCorrectionListResponse;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoiVehicleCorrectionExcelExporterTest {

    private static final String[] EXPECTED_HEADERS = {
            "NO.", "MODEL", "EXTERIOR COLOR", "INTERIOR COLOR", "VIN NUMBER", "ENGINE NUMBER",
            "MODEL CODE", "Year Make", "Material", "Shipment", "Batch ", "Offline EPMB", "EPMB ok ",
            "Remark1", "SAIC buy off ", "Date to Strogare Yard", "remark2", "Allocated Date", "Dealer Code",
            "Dealer", "Remark3", "Status1", "Invoice#", "Invoice Date", "remark4", "Payment Date",
            "Credit Full Payment Date", "Payment Status", "remark5", "Status2", "Invoice#", "Invoice Date",
            "remark6", "ETD  to Dealer", "ETA to Dealer ", "Trolly type\n4 units/ 6units",
            "Fully load or not", "Received date by Dealer ", "Delivery Status", "remark7", "Drosstech Status",
            "Upload Date", "Registration", "Customer region", "remark8"
    };

    private static final int[] EXPECTED_WIDTHS = {
            8, 24, 18, 18, 24, 22, 18, 12, 20, 20, 14, 16, 14, 18, 16, 24, 18, 16, 16, 32, 28, 18,
            18, 16, 18, 16, 24, 18, 18, 18, 18, 16, 18, 16, 16, 22, 18, 24, 18, 18, 18, 16, 16, 20, 18
    };

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
            assertArrayEquals(EXPECTED_HEADERS, IntStream.range(0, EXPECTED_HEADERS.length)
                    .mapToObj(column -> sheet.getRow(0).getCell(column).getStringCellValue())
                    .toArray(String[]::new));
            int[] departmentColumns = {1, 14, 17, 21, 25, 29, 33, 40};
            assertArrayEquals(new String[]{
                            "生产部门维护",
                            "物流部门维护",
                            "销售部门维护",
                            "财务部门第一次维护（发票种类）",
                            "财务部门第二次维护（收款状态）\t\t",
                            "财务部门第三次维护（如果第一次发票为 Proforma Invoice）\t\t",
                            "物流部门负责维护",
                            "销售部门根据列X维护"
                    }, IntStream.of(departmentColumns)
                            .mapToObj(column -> sheet.getRow(1).getCell(column).getStringCellValue())
                            .toArray(String[]::new));
            assertEquals(45, sheet.getRow(1).getPhysicalNumberOfCells());
            assertEquals(8, sheet.getNumMergedRegions());
            assertTrue(mergedRegions(sheet).containsAll(List.of(
                    "B2:N2", "O2:Q2", "R2:U2", "V2:Y2", "Z2:AC2", "AD2:AG2", "AH2:AN2", "AO2:AS2")));
            assertNotNull(sheet.getPaneInformation());
            assertTrue(sheet.getPaneInformation().isFreezePane());
            assertEquals(2, sheet.getPaneInformation().getHorizontalSplitPosition());
            assertEquals("MG S5", sheet.getRow(2).getCell(1).getStringCellValue());
            assertEquals("VIN00000000000090", sheet.getRow(2).getCell(4).getStringCellValue());
            assertEquals("2026-05-22", sheet.getRow(2).getCell(11).getStringCellValue());
            assertEquals("checked", sheet.getRow(2).getCell(44).getStringCellValue());
            assertEquals(1.0, sheet.getRow(2).getCell(0).getNumericCellValue());
            assertEquals(45, sheet.getRow(2).getLastCellNum());
        }
    }

    @Test
    void exportSerializesEveryVisiblePropertyInExactColumnOrderAndType() throws Exception {
        VehicleCorrectionListResponse row = fullyPopulatedSentinelRow();

        byte[] bytes = new PoiVehicleCorrectionExcelExporter().export(List.of(row));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Row dataRow = workbook.getSheet("Sheet1").getRow(2);
            List<Object> expected = expectedSentinelValues();
            assertEquals(45, dataRow.getLastCellNum());
            assertEquals(45, expected.size());
            for (int column = 0; column < expected.size(); column++) {
                Cell cell = dataRow.getCell(column);
                Object expectedValue = expected.get(column);
                if (expectedValue instanceof Number number) {
                    assertEquals(CellType.NUMERIC, cell.getCellType(), "cell type at column " + column);
                    assertEquals(number.doubleValue(), cell.getNumericCellValue(), "value at column " + column);
                } else {
                    assertEquals(CellType.STRING, cell.getCellType(), "cell type at column " + column);
                    String expectedText = expectedValue instanceof LocalDate date
                            ? date.toString() : expectedValue.toString();
                    assertEquals(expectedText, cell.getStringCellValue(), "value at column " + column);
                }
            }
        }
    }

    @Test
    void nullVisiblePropertyProducesABlankCellWithoutShiftingLaterColumns() throws Exception {
        VehicleCorrectionListResponse row = new VehicleCorrectionListResponse();
        row.setNo(102L);
        row.setModel(null);
        row.setRemark8("last-column-45");

        byte[] bytes = new PoiVehicleCorrectionExcelExporter().export(List.of(row));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Row dataRow = workbook.getSheet("Sheet1").getRow(2);
            assertEquals(CellType.BLANK, dataRow.getCell(1).getCellType());
            assertEquals("last-column-45", dataRow.getCell(44).getStringCellValue());
            assertEquals(45, dataRow.getLastCellNum());
        }
    }

    @Test
    void exportAppliesTemplateDimensionsAndReusableStyles() throws Exception {
        byte[] bytes = new PoiVehicleCorrectionExcelExporter().export(List.of(new VehicleCorrectionListResponse()));

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Sheet1");
            assertEquals(36.0f, sheet.getRow(0).getHeightInPoints());
            assertEquals(26.0f, sheet.getRow(1).getHeightInPoints());
            for (int column = 0; column < EXPECTED_WIDTHS.length; column++) {
                assertEquals(EXPECTED_WIDTHS[column] * 256, sheet.getColumnWidth(column));
            }

            assertHeaderStyle(workbook, sheet.getRow(0).getCell(0));
            assertDepartmentStyle(workbook, sheet.getRow(1).getCell(1), 12, 3, 0.74999);
            assertDepartmentStyle(workbook, sheet.getRow(1).getCell(25), 11, 9, 0.59999);
            assertDataStyle(workbook, sheet.getRow(2).getCell(0));
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
    void nullRowsAreExportedAsAnEmptyMasterSheet() throws Exception {
        byte[] bytes = new PoiVehicleCorrectionExcelExporter().export(null);
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("Sheet1");
            assertEquals(1, sheet.getLastRowNum());
            assertNull(sheet.getRow(2));
        }
    }

    @Test
    void exporterIsTheOnlySpringManagedImplementationInTheExportPackage() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.scan("com.company.admin.export");
            context.refresh();

            var exporters = context.getBeansOfType(VehicleCorrectionExcelExporter.class);
            assertEquals(1, exporters.size());
            assertTrue(exporters.values().iterator().next() instanceof PoiVehicleCorrectionExcelExporter);
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

    private void assertHeaderStyle(Workbook workbook, Cell cell) {
        CellStyle style = cell.getCellStyle();
        Font font = workbook.getFontAt(style.getFontIndexAsInt());
        assertEquals("Calibri", font.getFontName());
        assertEquals(12, font.getFontHeightInPoints());
        assertTrue(font.getBold());
        assertEquals(IndexedColors.BLACK.getIndex(), font.getColor());
        assertEquals(FillPatternType.SOLID_FOREGROUND, style.getFillPattern());
        assertEquals(HorizontalAlignment.CENTER, style.getAlignment());
        assertEquals(VerticalAlignment.CENTER, style.getVerticalAlignment());
        assertTrue(style.getWrapText());
        assertThinBorders(style);
    }

    private VehicleCorrectionListResponse fullyPopulatedSentinelRow() {
        VehicleCorrectionListResponse row = new VehicleCorrectionListResponse();
        row.setNo(101L);
        row.setModel("model-02");
        row.setExteriorColor("exterior-color-03");
        row.setInteriorColor("interior-color-04");
        row.setVinNumber("vin-number-05");
        row.setEngineNumber("engine-number-06");
        row.setModelCode("model-code-07");
        row.setYearMake("year-make-08");
        row.setMaterial("material-09");
        row.setShipment("shipment-10");
        row.setBatch("batch-11");
        row.setOfflineEpmb(LocalDate.of(2026, 1, 1));
        row.setEpmbOk(LocalDate.of(2026, 1, 2));
        row.setRemark1("remark1-14");
        row.setSaicBuyOff(LocalDate.of(2026, 1, 3));
        row.setDateToStorageYard(LocalDate.of(2026, 1, 4));
        row.setRemark2("remark2-17");
        row.setAllocatedDate(LocalDate.of(2026, 1, 5));
        row.setDealerCode("dealer-code-19");
        row.setDealer("dealer-20");
        row.setRemark3("remark3-21");
        row.setStatus1("status1-22");
        row.setInvoiceNo1("invoice-no1-23");
        row.setInvoiceDate1(LocalDate.of(2026, 1, 6));
        row.setRemark4("remark4-25");
        row.setPaymentDate(LocalDate.of(2026, 1, 7));
        row.setCreditFullPaymentDate(LocalDate.of(2026, 1, 8));
        row.setPaymentStatus("payment-status-28");
        row.setRemark5("remark5-29");
        row.setStatus2("status2-30");
        row.setInvoiceNo2("invoice-no2-31");
        row.setInvoiceDate2(LocalDate.of(2026, 1, 9));
        row.setRemark6("remark6-33");
        row.setEtdToDealer(LocalDate.of(2026, 1, 10));
        row.setEtaToDealer(LocalDate.of(2026, 1, 11));
        row.setTrollyType("trolly-type-36");
        row.setFullyLoad("fully-load-37");
        row.setReceivedDateByDealer(LocalDate.of(2026, 1, 12));
        row.setDeliveryStatus("delivery-status-39");
        row.setRemark7("remark7-40");
        row.setDrosstechStatus("drosstech-status-41");
        row.setUploadDate(LocalDate.of(2026, 1, 13));
        row.setRegistration(LocalDate.of(2026, 1, 14));
        row.setCustomerRegion("customer-region-44");
        row.setRemark8("remark8-45");
        return row;
    }

    private List<Object> expectedSentinelValues() {
        return List.of(101L, "model-02", "exterior-color-03", "interior-color-04", "vin-number-05",
                "engine-number-06", "model-code-07", "year-make-08", "material-09", "shipment-10",
                "batch-11", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), "remark1-14",
                LocalDate.of(2026, 1, 3), LocalDate.of(2026, 1, 4), "remark2-17",
                LocalDate.of(2026, 1, 5), "dealer-code-19", "dealer-20", "remark3-21", "status1-22",
                "invoice-no1-23", LocalDate.of(2026, 1, 6), "remark4-25", LocalDate.of(2026, 1, 7),
                LocalDate.of(2026, 1, 8), "payment-status-28", "remark5-29", "status2-30",
                "invoice-no2-31", LocalDate.of(2026, 1, 9), "remark6-33", LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 11), "trolly-type-36", "fully-load-37", LocalDate.of(2026, 1, 12),
                "delivery-status-39", "remark7-40", "drosstech-status-41", LocalDate.of(2026, 1, 13),
                LocalDate.of(2026, 1, 14), "customer-region-44", "remark8-45");
    }

    private void assertDepartmentStyle(Workbook workbook, Cell cell, int fontSize, int theme, double tint) {
        CellStyle style = cell.getCellStyle();
        Font font = workbook.getFontAt(style.getFontIndexAsInt());
        assertEquals("Microsoft YaHei", font.getFontName());
        assertEquals(fontSize, font.getFontHeightInPoints());
        assertTrue(font.getBold());
        assertEquals(HorizontalAlignment.CENTER, style.getAlignment());
        assertEquals(VerticalAlignment.CENTER, style.getVerticalAlignment());
        assertTrue(style.getWrapText());
        assertThinBorders(style);
        XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
        assertEquals(theme, xssfStyle.getFillForegroundXSSFColor().getTheme());
        assertEquals(tint, xssfStyle.getFillForegroundXSSFColor().getTint(), 0.000001);
    }

    private void assertDataStyle(Workbook workbook, Cell cell) {
        CellStyle style = cell.getCellStyle();
        Font font = workbook.getFontAt(style.getFontIndexAsInt());
        assertEquals("Calibri", font.getFontName());
        assertEquals(11, font.getFontHeightInPoints());
        assertFalse(font.getBold());
        assertEquals(VerticalAlignment.CENTER, style.getVerticalAlignment());
        assertThinBorders(style);
    }

    private void assertThinBorders(CellStyle style) {
        assertEquals(BorderStyle.THIN, style.getBorderTop());
        assertEquals(BorderStyle.THIN, style.getBorderRight());
        assertEquals(BorderStyle.THIN, style.getBorderBottom());
        assertEquals(BorderStyle.THIN, style.getBorderLeft());
    }

    private List<String> mergedRegions(Sheet sheet) {
        return IntStream.range(0, sheet.getNumMergedRegions())
                .mapToObj(index -> sheet.getMergedRegion(index).formatAsString())
                .collect(Collectors.toList());
    }
}
