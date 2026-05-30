package com.lms.dto;

import java.time.LocalDate;

public record TeacherRequest(
        String fullName,
        String phone,
        LocalDate birthDate,
        String bio,
        String qualification,
        Integer experienceYears
) {}
