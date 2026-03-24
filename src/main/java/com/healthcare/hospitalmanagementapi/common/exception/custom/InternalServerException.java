package com.healthcare.hospitalmanagementapi.common.exception.custom;

public class InternalServerException extends RuntimeException {
    public InternalServerException(String message) {
        super(message);
    }
}
