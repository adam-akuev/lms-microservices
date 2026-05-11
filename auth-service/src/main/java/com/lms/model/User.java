package com.lms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "Некорректный формат почты")
    @NotBlank(message = "Почта не может быть пустой")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "ФИО не может быть пустым")
    @Pattern(
            regexp = "^[а-яА-ЯёЁa-zA-Z\\s\\-]+$",
            message = "ФИО может содержать только буквы, пробелы и дефис"
    )
    @Column(nullable = false)
    private String fullName;

    @Column(unique = true)
    @Pattern(
            regexp = "^(\\+7|7|8)?[\\s\\-]?\\(?[489][0-9]{2}\\)?[\\s\\-]?[0-9]{3}[\\s\\-]?[0-9]{2}[\\s\\-]?[0-9]{2}$",
            message = "Неверный формат номера телефона"
    )
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
