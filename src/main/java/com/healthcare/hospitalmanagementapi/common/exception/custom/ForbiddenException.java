package com.healthcare.hospitalmanagementapi.common.exception.custom;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
