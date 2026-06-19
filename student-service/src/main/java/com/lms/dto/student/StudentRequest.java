package com.lms.dto.student;

import java.time.LocalDate;

public record StudentRequest(
        String fullName,
        String phone,
        LocalDate birthDate
) {}
