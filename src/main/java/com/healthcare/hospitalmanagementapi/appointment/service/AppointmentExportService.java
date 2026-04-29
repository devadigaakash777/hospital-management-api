package com.healthcare.hospitalmanagementapi.appointment.service;

import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentExportRequestDTO;

public interface AppointmentExportService {

    byte[] exportToExcel(AppointmentExportRequestDTO request);
}