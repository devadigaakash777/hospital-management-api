package com.healthcare.hospitalmanagementapi.appointment.export;

import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentResponseDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class responsible for building the Excel workbook from a list of
 * {@link AppointmentResponseDTO} objects.
 *
 * <p>Uses {@link SXSSFWorkbook} (streaming workbook) so that large result sets
 * are written to disk-backed temporary files rather than kept entirely in heap
 * memory, keeping memory consumption predictable even near the 10 000-row cap.
 */
public class AppointmentExcelExporter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // Column headers in display order
    private static final String[] HEADERS = {
            "Token No.",
            "Appointment Date",
            "Appointment Time",
            "Slot Start",
            "Slot End",
            "Status",
            "VIP",
            "Patient Name",
            "UH ID",
            "Patient Phone",
            "Patient Email",
            "Doctor Name",
            "Designation",
            "Specialization",
            "Department",
            "Patient Message",
            "Created By"
    };

    private AppointmentExcelExporter() {
        // utility class — no instances
    }

    /**
     * Generates an Excel workbook byte array for the supplied appointments.
     *
     * @param appointments the data rows to write
     * @return raw bytes of the generated {@code .xlsx} file
     * @throws IOException if the workbook cannot be serialised
     */
    public static byte[] export(List<AppointmentResponseDTO> appointments) throws IOException {

        // Keep 100 rows in memory; the rest are flushed to a temp file
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {

            workbook.setCompressTempFiles(true);

            Sheet sheet = workbook.createSheet("Appointments");
            sheet.createFreezePane(0, 2); // freeze title + header rows

            CellStyle titleStyle  = buildTitleStyle(workbook);
            CellStyle headerStyle = buildHeaderStyle(workbook);
            CellStyle dataStyle   = buildDataStyle(workbook);
            CellStyle altStyle    = buildAltRowStyle(workbook);

            // ── Row 0: report title ──────────────────────────────────────────
            Row titleRow = sheet.createRow(0);
            titleRow.setHeightInPoints(28);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Appointment Report  —  generated on " +
                    LocalDate.now().format(DATE_FMT));
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

            // ── Row 1: column headers ────────────────────────────────────────
            Row headerRow = sheet.createRow(1);
            headerRow.setHeightInPoints(20);
            for (int col = 0; col < HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
                cell.setCellStyle(headerStyle);
            }

            // ── Rows 2+: data ────────────────────────────────────────────────
            int rowIndex = 2;
            for (AppointmentResponseDTO appt : appointments) {
                Row row = sheet.createRow(rowIndex);
                CellStyle rowStyle = (rowIndex % 2 == 0) ? dataStyle : altStyle;

                writeCell(row, 0,  appt.getTokenNumber(),                             rowStyle);
                writeCell(row, 1,  format(appt.getAppointmentDate()),                 rowStyle);
                writeCell(row, 2,  format(appt.getAppointmentTime()),                 rowStyle);
                writeCell(row, 3,  format(appt.getSlotStartTime()),                   rowStyle);
                writeCell(row, 4,  format(appt.getSlotEndTime()),                     rowStyle);
                writeCell(row, 5,  appt.getAppointmentStatus() != null
                        ? appt.getAppointmentStatus().name() : "",                    rowStyle);
                writeCell(row, 6,  Boolean.TRUE.equals(appt.getIsVip()) ? "Yes" : "No", rowStyle);
                writeCell(row, 7,  appt.getPatientName(),                             rowStyle);
                writeCell(row, 8,  appt.getPatientUhId(),                             rowStyle);
                writeCell(row, 9,  appt.getPatientPhoneNumber(),                      rowStyle);
                writeCell(row, 10, appt.getPatientEmail(),                            rowStyle);
                writeCell(row, 11, appt.getDoctorName(),                              rowStyle);
                writeCell(row, 12, appt.getDoctorDesignation(),                       rowStyle);
                writeCell(row, 13, appt.getDoctorSpecialization(),                    rowStyle);
                writeCell(row, 14, appt.getDepartmentName(),                          rowStyle);
                writeCell(row, 15, appt.getPatientMessage(),                          rowStyle);
                writeCell(row, 16, appt.getCreatedByUserName(),                       rowStyle);

                rowIndex++;
            }

            // Auto-size every column (SXSSFSheet requires trackAllColumnsForAutoSizing)
            ((org.apache.poi.xssf.streaming.SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
            for (int col = 0; col < HEADERS.length; col++) {
                sheet.autoSizeColumn(col);
                // Add a small padding on top of auto-sized width
                sheet.setColumnWidth(col, sheet.getColumnWidth(col) + 512);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.dispose(); // delete temp files
            return out.toByteArray();
        }
    }

    // ── Cell writers ─────────────────────────────────────────────────────────

    private static void writeCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else if (value != null) {
            cell.setCellValue(value.toString());
        } else {
            cell.setCellValue("");
        }
        cell.setCellStyle(style);
    }

    // ── Formatters ────────────────────────────────────────────────────────────

    private static String format(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "";
    }

    private static String format(LocalTime time) {
        return time != null ? time.format(TIME_FMT) : "";
    }

    // ── Style builders ────────────────────────────────────────────────────────

    private static CellStyle buildTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 13);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle buildHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle buildDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static CellStyle buildAltRowStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}