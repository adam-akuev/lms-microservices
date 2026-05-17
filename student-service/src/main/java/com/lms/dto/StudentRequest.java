package com.lms.dto;

import java.time.LocalDate;

public record StudentRequest(
        String fullName,
        String phone,
        LocalDate birthDate
) {}
