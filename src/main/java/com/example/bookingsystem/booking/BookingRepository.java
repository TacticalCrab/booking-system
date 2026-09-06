package com.example.bookingsystem.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findAllByUserEmail(
            String email,
            Pageable pageable
    );

    boolean existsByEmployee_IdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long employeeId,
            LocalDateTime requestedStart,
            LocalDateTime requestedEnd
    );

    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM Booking b
        WHERE b.employee.id = :emplyeeId
            AND b.startTime < :requestedEnd
            AND b.endTime > :requestedStart
            AND b.status <> com.example.bookingsystem.booking.BookingStatus.CANCELLED
        """)
    boolean existsOverlappingBooking(
            @Param("employeeId") Long employeeId,
            @Param("requestedStart") LocalDateTime requestedStart,
            @Param("requestedEnd") LocalDateTime requestedEnd
    );
}
