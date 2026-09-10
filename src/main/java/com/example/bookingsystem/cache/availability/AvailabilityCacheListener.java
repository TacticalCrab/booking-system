package com.example.bookingsystem.cache.availability;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
class AvailabilityCacheListener {
    private final AvailabilityCacheService availabilityCacheService;

    public AvailabilityCacheListener(
            AvailabilityCacheService availabilityCacheService
    ) {
        this.availabilityCacheService = availabilityCacheService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handle(
            EmployeeAvailabilityChangedEvent event
    ) {
        availabilityCacheService.evictForEmployee(
                event.employeeId()
        );
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handle(
            EmployeeDateAvailabilityChangedEvent event
    ) {
        availabilityCacheService.evictForEmployeeAndDate(
                event.employeeId(),
                event.serviceIds(),
                event.date()
        );
    }
}
