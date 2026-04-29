package com.healthcare.hospitalmanagementapi.appointment.controller;

import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentExportRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.service.AppointmentExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/appointments/export")
@RequiredArgsConstructor
@Tag(
        name = "Appointment Management",
        description = "Operations related to appointment management"
)
@ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
public class AppointmentExportController {

    private static final DateTimeFormatter FILE_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final MediaType EXCEL_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final AppointmentExportService appointmentExportService;

    @Operation(
            summary = "Export appointments to Excel",
            description = """
                    Generates and downloads an Excel (.xlsx) report for appointments
                    matching the supplied filter criteria.

                    Filter behaviour:
                    - startDate  — defaults to today if omitted (inclusive)
                    - endDate    — defaults to today if omitted (inclusive)
                    - appointmentStatus — if omitted, all statuses are included
                    - doctorId / createdByUserId — if omitted, all records are included

                    Constraints:
                    - startDate must not be after endDate
                    - The result set must not exceed 10 000 rows; narrow your filters if this limit is hit
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Excel file generated successfully",
            content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    )
    @ApiResponse(
            responseCode = "400",
            description = """
                    Bad request:
                    - startDate is after endDate
                    - Result set exceeds the 10 000-row export limit
                    """,
            content = @Content
    )
    @PreAuthorize("hasAuthority('CAN_EXPORT_REPORTS')")
    @GetMapping
    public ResponseEntity<byte[]> exportAppointments(
            @ParameterObject @ModelAttribute AppointmentExportRequestDTO request
    ) {
        byte[] excelBytes = appointmentExportService.exportToExcel(request);

        String filename = "appointments_" + LocalDate.now().format(FILE_DATE_FMT) + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(EXCEL_MEDIA_TYPE);
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build()
        );
        headers.setContentLength(excelBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }
}