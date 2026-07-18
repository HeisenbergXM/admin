package com.company.admin.export;

import com.company.admin.dto.response.VehicleCorrectionListResponse;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

@Component
public class PoiVehicleCorrectionExcelExporter implements VehicleCorrectionExcelExporter {

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
            new Group(1, 13, "生产部门维护", "F9F9F9"),
            new Group(14, 16, "物流部门维护", "333F50"),
            new Group(17, 20, "销售部门维护", "B4C6E7"),
            new Group(21, 24, "财务部门第一次维护（发票种类）", "D9D9D9"),
            new Group(25, 28, "财务部门第二次维护（收款状态）\t\t", "C6E0B4"),
            new Group(29, 32, "财务部门第三次维护（如果第一次发票为 Proforma Invoice）\t\t", "C6E0B4"),
            new Group(33, 39, "物流部门负责维护", "333F50"),
            new Group(40, 44, "销售部门根据列X维护", "B4C6E7")
    };

    private static final int[] COLUMN_WIDTHS = {
            8, 24, 18, 18, 24, 22, 18, 12, 20, 20, 14, 16, 14, 18, 16, 24, 18, 16, 16, 32, 28, 18,
            18, 16, 18, 16, 24, 18, 18, 18, 18, 16, 18, 16, 16, 22, 18, 24, 18, 18, 18, 16, 16, 20, 18
    };

    @Override
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

    private void writeHeaders(SXSSFWorkbook workbook, Sheet sheet) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(36);
        for (int column = 0; column < HEADERS.length; column++) {
            Cell cell = headerRow.createCell(column);
            cell.setCellValue(HEADERS[column]);
            cell.setCellStyle(headerStyle);
        }

        Row departmentRow = sheet.createRow(1);
        departmentRow.setHeightInPoints(26);
        Cell blankCell = departmentRow.createCell(0);
        blankCell.setCellStyle(headerStyle);
        for (Group group : GROUPS) {
            CellStyle groupStyle = createDepartmentStyle(workbook, group);
            for (int column = group.firstColumn(); column <= group.lastColumn(); column++) {
                Cell cell = departmentRow.createCell(column);
                cell.setCellStyle(groupStyle);
                if (column == group.firstColumn()) {
                    cell.setCellValue(group.title());
                }
            }
            sheet.addMergedRegion(new CellRangeAddress(1, 1, group.firstColumn(), group.lastColumn()));
        }

        for (int column = 0; column < COLUMN_WIDTHS.length; column++) {
            sheet.setColumnWidth(column, Math.min(COLUMN_WIDTHS[column], 255) * 256);
        }
    }

    private void writeRows(SXSSFWorkbook workbook, Sheet sheet, List<VehicleCorrectionListResponse> rows) {
        CellStyle dataStyle = createDataStyle(workbook);
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Row excelRow = sheet.createRow(rowIndex + 2);
            List<Object> values = values(rows.get(rowIndex));
            for (int column = 0; column < values.size(); column++) {
                Cell cell = excelRow.createCell(column);
                cell.setCellStyle(dataStyle);
                writeValue(cell, values.get(column));
            }
        }
    }

    private void writeValue(Cell cell, Object value) {
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof LocalDate date) {
            cell.setCellValue(date.toString());
        } else if (value != null) {
            cell.setCellValue(value.toString());
        }
    }

    private CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(rgbColor("FFFFFF"));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        setBorders(style, IndexedColors.BLACK);
        return style;
    }

    private CellStyle createDepartmentStyle(SXSSFWorkbook workbook, Group group) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Microsoft YaHei");
        font.setFontHeightInPoints((short) (group.firstColumn() >= 25 && group.lastColumn() <= 39 ? 11 : 12));
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(rgbColor(group.rgb()));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        setBorders(style, IndexedColors.BLACK);
        return style;
    }

    private CellStyle createDataStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style, IndexedColors.GREY_25_PERCENT);
        return style;
    }

    private XSSFColor rgbColor(String rgb) {
        return new XSSFColor(HexFormat.of().parseHex(rgb), new DefaultIndexedColorMap());
    }

    private void setBorders(CellStyle style, IndexedColors color) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setTopBorderColor(color.getIndex());
        style.setRightBorderColor(color.getIndex());
        style.setBottomBorderColor(color.getIndex());
        style.setLeftBorderColor(color.getIndex());
    }

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

    private record Group(int firstColumn, int lastColumn, String title, String rgb) {
    }
}
