package com.example.bookingsystem.support.builder;

import com.example.bookingsystem.service.ServiceEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

public class ServiceTestBuilder {

    private String name = "Shave";
    private String description = "Shave your head and body, just everything now!";
    private Integer durationMinutes = 20;
    private BigDecimal price = BigDecimal.valueOf(999.0);
    private Long id = 1L;

    public ServiceTestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ServiceTestBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public ServiceTestBuilder withDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
        return this;
    }

    public ServiceTestBuilder withPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    public ServiceTestBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ServiceTestBuilder withoutId() {
        this.id = null;
        return this;
    }

    public ServiceEntity build() {
        ServiceEntity service = new ServiceEntity(
                name,
                description,
                durationMinutes,
                price
        );

        ReflectionTestUtils.setField(service, "id", id);

        return service;
    }
}
