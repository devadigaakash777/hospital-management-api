package com.healthcare.hospitalmanagementapi.doctor.util;

import com.healthcare.hospitalmanagementapi.doctor.dto.weeklyschedule.CreateDoctorWeeklyScheduleRequestDTO;
import com.healthcare.hospitalmanagementapi.enums.DayOfWeek;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DoctorWeeklyScheduleUtil {

//    private DoctorWeeklyScheduleUtil() {
//    }
//
//    public static List<CreateDoctorWeeklyScheduleRequestDTO> buildRegularSchedules(
//            List<String> regularDays
//    ) {
//        List<CreateDoctorWeeklyScheduleRequestDTO> schedules = new ArrayList<>();
//
//        for (String day : regularDays) {
//            schedules.add(CreateDoctorWeeklyScheduleRequestDTO.builder()
//                    .weekNumber(0)
//                    .dayOfWeek(convertDay(day))
//                    .build());
//        }
//
//        return schedules;
//    }
//
//    public static List<CreateDoctorWeeklyScheduleRequestDTO> buildSpecificSchedules(
//            Map<String, List<String>> specificWeeks
//    ) {
//        List<CreateDoctorWeeklyScheduleRequestDTO> schedules = new ArrayList<>();
//
//        specificWeeks.forEach((week, days) -> {
//            int weekNumber = switch (week) {
//                case "1st Week" -> 1;
//                case "2nd Week" -> 2;
//                case "3rd Week" -> 3;
//                case "4th Week" -> 4;
//                case "Last Week" -> 5;
//                default -> throw new IllegalArgumentException("Invalid week: " + week);
//            };
//
//            for (String day : days) {
//                schedules.add(CreateDoctorWeeklyScheduleRequestDTO.builder()
//                        .weekNumber(weekNumber)
//                        .dayOfWeek(convertDay(day))
//                        .build());
//            }
//        });
//
//        return schedules;
//    }
//
//    private static DayOfWeek convertDay(String day) {
//        return switch (day.toUpperCase()) {
//            case "MON", "MONDAY" -> DayOfWeek.MONDAY;
//            case "TUE", "TUESDAY" -> DayOfWeek.TUESDAY;
//            case "WED", "WEDNESDAY" -> DayOfWeek.WEDNESDAY;
//            case "THU", "THURSDAY" -> DayOfWeek.THURSDAY;
//            case "FRI", "FRIDAY" -> DayOfWeek.FRIDAY;
//            case "SAT", "SATURDAY" -> DayOfWeek.SATURDAY;
//            case "SUN", "SUNDAY" -> DayOfWeek.SUNDAY;
//            default -> throw new IllegalArgumentException("Invalid day: " + day);
//        };
//    }
}