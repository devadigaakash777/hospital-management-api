package com.healthcare.hospitalmanagementapi.healthpackage.mapper;

import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.CreateHealthPackageAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.HealthPackageAppointmentResponseDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.dto.appointment.UpdateHealthPackageAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.healthpackage.entity.HealthPackageAppointment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface HealthPackageAppointmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "healthPackage", ignore = true)
    @Mapping(target = "healthPackageTimeSlot", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "appointmentTime", ignore = true)
    @Mapping(target = "appointmentStatus", ignore = true)
    @Mapping(target = "tokenNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdByUser", ignore = true)
    HealthPackageAppointment toEntity(CreateHealthPackageAppointmentRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "healthPackage", ignore = true)
    @Mapping(target = "healthPackageTimeSlot", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "appointmentDate", ignore = true)
    @Mapping(target = "appointmentTime", ignore = true)
    @Mapping(target = "tokenNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntityFromDto(
            UpdateHealthPackageAppointmentRequestDTO dto,
            @MappingTarget HealthPackageAppointment entity
    );

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientUhId", source = "patient.uhId")
    @Mapping(target = "patientName", expression = "java(entity.getPatient().getFirstName() + \" \" + entity.getPatient().getLastName())")
    @Mapping(target = "patientPhoneNumber", source = "patient.phoneNumber")
    @Mapping(target = "patientEmail", source = "patient.email")
    @Mapping(target = "healthPackageId", source = "healthPackage.id")
    @Mapping(target = "healthPackageName", source = "healthPackage.packageName")
    @Mapping(target = "healthPackageDescription", source = "healthPackage.description")
    @Mapping(target = "healthPackagePrice", source = "healthPackage.packagePrice")
    @Mapping(target = "healthPackageTimeSlotId", source = "healthPackageTimeSlot.id")
    @Mapping(target = "slotStartTime", source = "healthPackageTimeSlot.startTime")
    @Mapping(target = "slotEndTime", source = "healthPackageTimeSlot.endTime")
    @Mapping(target = "createdByUserId", source = "createdByUser.id")
    @Mapping(
            target = "createdByUserName",
            expression = "java(entity.getCreatedByUser() != null ? entity.getCreatedByUser().getFirstName() + \" \" + entity.getCreatedByUser().getLastName() : null)"
    )
    HealthPackageAppointmentResponseDTO toResponseDTO(HealthPackageAppointment entity);
}