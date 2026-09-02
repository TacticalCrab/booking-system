package com.example.bookingsystem.common.exception;

import java.util.Collection;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String resource, Object id) {
        super(resource + " with id " + id + " was not found");
    }

    public NotFoundException(String resource, Collection<?> id) {
        super(resource + " with ids " + id + " were not found");
    }
}
