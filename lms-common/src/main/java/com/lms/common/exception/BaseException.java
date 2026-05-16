package com.lms.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {

    @Getter
    private final HttpStatus status;

    protected BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
