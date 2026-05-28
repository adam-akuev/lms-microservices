package com.lms.common.exception;

import org.springframework.http.HttpStatus;

public class StudentAlreadyEnrolledException extends BaseException {
    public StudentAlreadyEnrolledException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
