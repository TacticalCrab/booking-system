package com.example.bookingsystem.employee.workinghours;

import com.example.bookingsystem.employee.dto.WorkingHoursResponse;

public class EmployeeWorkingHoursMapper {
    public static WorkingHoursResponse toResponse(
            EmployeeWorkingHours workingHours
    ) {
        return new WorkingHoursResponse(
                workingHours.getDayOfWeek(),
                workingHours.getStartTime(),
                workingHours.getEndTime()
        );
    }
}
