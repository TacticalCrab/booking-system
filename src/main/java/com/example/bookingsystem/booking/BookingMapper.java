package com.example.bookingsystem.booking;

import com.example.bookingsystem.booking.dto.BookingResponse;
import com.example.bookingsystem.employee.EmployeeMapper;
import com.example.bookingsystem.employee.dto.EmployeeResponse;
import com.example.bookingsystem.service.ServiceMapper;
import com.example.bookingsystem.service.dto.ServiceResponse;
import com.example.bookingsystem.user.UserMapper;
import com.example.bookingsystem.user.dto.UserResponse;

public final class BookingMapper {
    private BookingMapper() {}

    public static BookingResponse toResponse(Booking booking) {
        UserResponse user = UserMapper.toResponse(booking.getUser());
        EmployeeResponse employee = EmployeeMapper.toResponse(booking.getEmployee());
        ServiceResponse service = ServiceMapper.toResponse(booking.getService());

        return new BookingResponse(
                booking.getId(),
                user,
                employee,
                service,
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}
