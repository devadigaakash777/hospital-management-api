package com.healthcare.hospitalmanagementapi.appointment.mapper;

import com.healthcare.hospitalmanagementapi.appointment.dto.AppointmentResponseDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.CreateAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.dto.UpdateAppointmentRequestDTO;
import com.healthcare.hospitalmanagementapi.appointment.entity.Appointment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctorTimeSlot", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "appointmentTime", ignore = true)
    @Mapping(target = "tokenNumber", ignore = true)
    @Mapping(target = "doctorDesignationSnapshot", ignore = true)
    @Mapping(target = "doctorSpecializationSnapshot", ignore = true)
    @Mapping(target = "departmentNameSnapshot", ignore = true)
    @Mapping(target = "appointmentStatus", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Appointment toEntity(CreateAppointmentRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctorId", ignore = true)
    @Mapping(target = "doctorTimeSlot", ignore = true)
    @Mapping(target = "appointmentDate", ignore = true)
    @Mapping(target = "appointmentTime", ignore = true)
    @Mapping(target = "tokenNumber", ignore = true)
    @Mapping(target = "doctorDesignationSnapshot", ignore = true)
    @Mapping(target = "doctorSpecializationSnapshot", ignore = true)
    @Mapping(target = "departmentNameSnapshot", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(
            UpdateAppointmentRequestDTO dto,
            @MappingTarget Appointment appointment
    );

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientUhId", source = "patient.uhId")
    @Mapping(
            target = "patientName",
            expression = "java(appointment.getPatient().getFirstName() + \" \" + appointment.getPatient().getLastName())"
    )
    @Mapping(target = "patientPhoneNumber", source = "patient.phoneNumber")
    @Mapping(target = "patientEmail", source = "patient.email")

    @Mapping(target = "doctorId", source = "doctorId")
    @Mapping(
            target = "doctorName",
            expression = """
                java(
                    appointment.getDoctorTimeSlot().getDoctor().getUser().getFirstName()
                    + " "
                    + appointment.getDoctorTimeSlot().getDoctor().getUser().getLastName()
                )
                """
    )

    @Mapping(target = "doctorTimeSlotId", source = "doctorTimeSlot.id")
    @Mapping(target = "slotStartTime", source = "doctorTimeSlot.startTime")
    @Mapping(target = "slotEndTime", source = "doctorTimeSlot.endTime")

    @Mapping(target = "departmentName", source = "departmentNameSnapshot")
    @Mapping(target = "doctorDesignation", source = "doctorDesignationSnapshot")
    @Mapping(target = "doctorSpecialization", source = "doctorSpecializationSnapshot")

    @Mapping(target = "createdByUserId", source = "createdByUser.id")
    @Mapping(
            target = "createdByUserName",
            expression = """
                    java(
                        appointment.getCreatedByUser().getFirstName()
                        + " "
                        + appointment.getCreatedByUser().getLastName()
                    )
                    """
    )
    AppointmentResponseDTO toResponseDTO(Appointment appointment);
}