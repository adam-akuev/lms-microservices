package com.lms.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException() {
        super("Неверный email или пароль", HttpStatus.UNAUTHORIZED);
    }
}
