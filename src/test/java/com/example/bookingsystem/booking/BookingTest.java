package com.example.bookingsystem.booking;

import com.example.bookingsystem.support.TestDataFactory;
import com.example.bookingsystem.user.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BookingTest {

    @Test
    void shouldReturnTrueWhenBookingIsOwnedByUser() {
        Booking booking = TestDataFactory.booking();
        User user = booking.getUser();

        assertNotNull(user);
        assertTrue(booking.isOwnedBy(user.getId()));
    }

    @Test
    void shouldReturnFalseWhenBookingIsOwnedByUser() {
        Booking booking = TestDataFactory.booking();

        assertFalse(booking.isOwnedBy(9999L));
    }

    @Test
    void shouldChangeStatusToCanceledWhenCancelled() {
        Booking booking = TestDataFactory.booking();

        booking.cancel();

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }
}
