package com.healthcare.hospitalmanagementapi.appointment.service.impl;

import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentExportRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentResponseDTO;
import com.healthcare.hospitalmanagementapi.appointment.export.AppointmentExcelExporter;
import com.healthcare.hospitalmanagementapi.appointment.mapper.AppointmentMapper;
import com.healthcare.hospitalmanagementapi.appointment.repository.AppointmentExportRepository;
import com.healthcare.hospitalmanagementapi.appointment.service.AppointmentExportService;
import com.healthcare.hospitalmanagementapi.common.exception.custom.BadRequestException;
import com.healthcare.hospitalmanagementapi.common.exception.custom.InternalServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentExportServiceImpl implements AppointmentExportService {
    private static final int EXPORT_ROW_LIMIT = 10_000;

    private final AppointmentExportRepository appointmentExportRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    public byte[] exportToExcel(AppointmentExportRequestDTO request) {

        LocalDate startDate = request.getStartDate() != null
                ? request.getStartDate()
                : LocalDate.now();

        LocalDate endDate = request.getEndDate() != null
                ? request.getEndDate()
                : LocalDate.now();

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException(
                    "startDate must not be after endDate"
            );
        }

        String statusName = request.getAppointmentStatus() != null
                ? request.getAppointmentStatus().name()
                : null;

        List<AppointmentResponseDTO> appointments =
                appointmentExportRepository.findForExport(
                                request.getDoctorId(),
                                request.getCreatedByUserId(),
                                statusName,
                                startDate,
                                endDate
                        )
                        .stream()
                        .map(appointmentMapper::toResponseDTO)
                        .toList();

        if (appointments.size() > EXPORT_ROW_LIMIT) {
            log.warn(
                    "Export rejected: result set size {} exceeds limit {}. " +
                            "doctorId={}, createdByUserId={}, status={}, startDate={}, endDate={}",
                    appointments.size(), EXPORT_ROW_LIMIT,
                    request.getDoctorId(), request.getCreatedByUserId(),
                    statusName, startDate, endDate
            );
            throw new BadRequestException(
                    "Export exceeds the maximum allowed limit of " + EXPORT_ROW_LIMIT +
                            " records. Please narrow your search using filters such as a " +
                            "smaller date range, a specific doctor, status, or created-by user."
            );
        }

        log.info(
                "Exporting {} appointment(s). doctorId={}, createdByUserId={}, " +
                        "status={}, startDate={}, endDate={}",
                appointments.size(),
                request.getDoctorId(), request.getCreatedByUserId(),
                statusName, startDate, endDate
        );

        try {
            return AppointmentExcelExporter.export(appointments);
        } catch (IOException ex) {
            log.error("Failed to generate appointment Excel report", ex);
            throw new InternalServerException(
                    "Failed to generate the Excel report. Please try again."
            );
        }
    }
}