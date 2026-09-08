package com.example.bookingsystem.common.exception;

import java.util.Collection;

public class NotFoundException extends RuntimeException {

    private final String resource;
    private final String matcher;
    private final Object value;

    public NotFoundException(String resource, Object id) {
        super(resource + " with id " + id + " was not found");

        this.resource = resource;
        this.matcher = "id";
        this.value = id;
    }

    public NotFoundException(String resource, Collection<?> ids) {
        super(resource + " with ids " + ids + " were not found");

        this.resource = resource;
        this.matcher = "ids";
        this.value = ids;
    }

    public NotFoundException(
            String resource,
            String matcher,
            Object value
    ) {
        super(resource + " with " + matcher + " " + value + " was not found");

        this.resource = resource;
        this.matcher = matcher;
        this.value = value;
    }

    public String getResource() {
        return resource;
    }

    public String getMatcher() {
        return matcher;
    }

    public Object getValue() {
        return value;
    }
}
