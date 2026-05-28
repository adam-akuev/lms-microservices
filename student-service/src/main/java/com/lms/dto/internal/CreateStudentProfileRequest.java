package com.lms.dto.internal;

public record CreateStudentProfileRequest(
        Long id,
        String fullName,
        String phone
) {}
