package com.healthcare.hospitalmanagementapi.common.exception.custom;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
