package com.lms.common.exception;

import org.springframework.http.HttpStatus;

public class CertificateAccessDeniedException extends BaseException {
    public CertificateAccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}