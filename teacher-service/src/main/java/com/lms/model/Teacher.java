package com.lms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    @Id
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String phone;
    private LocalDate birthDate;

    @Column(length = 1000)
    private String bio;

    private String qualification;
    private Integer experienceYears;

    private boolean active = true;
}
