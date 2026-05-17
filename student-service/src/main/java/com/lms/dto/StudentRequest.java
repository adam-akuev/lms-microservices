package com.lms.dto;

import java.time.LocalDate;

public record StudentRequest(
        String firstName,
        String lastName,
        String phone,
        LocalDate birthDate
) {}
