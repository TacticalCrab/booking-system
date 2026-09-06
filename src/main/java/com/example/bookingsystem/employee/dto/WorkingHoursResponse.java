package com.example.bookingsystem.employee.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record WorkingHoursResponse(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {}
