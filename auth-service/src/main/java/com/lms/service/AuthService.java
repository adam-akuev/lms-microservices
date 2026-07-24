package com.lms.service;

import com.lms.common.exception.InvalidCredentialsException;
import com.lms.config.RabbitMQConfig;
import com.lms.dto.LoginRequest;
import com.lms.dto.RegisterRequest;
import com.lms.dto.event.StudentRegistrationEvent;
import com.lms.dto.event.TeacherRegistrationEvent;
import com.lms.model.Role;
import com.lms.model.User;
import com.lms.repository.UserRepository;
import com.lms.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final RabbitTemplate rabbitTemplate;

    public void register(RegisterRequest request) {
        User user = userService.saveNewUser(request);

        if (request.role() == Role.ROLE_STUDENT) {
            StudentRegistrationEvent event = new StudentRegistrationEvent(
                    user.getId(),
                    request.fullName(),
                    request.phoneNumber()
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.LMS_EXCHANGE,
                    RabbitMQConfig.STUDENT_ROUTING_KEY,
                    event
            );
        }

        if (request.role() == Role.ROLE_TEACHER) {
            TeacherRegistrationEvent event = new TeacherRegistrationEvent(
                    user.getId(),
                    request.fullName(),
                    request.phoneNumber()
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.LMS_EXCHANGE,
                    RabbitMQConfig.TEACHER_ROUTING_KEY,
                    event
            );
        }
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Неверный email или пароль"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return jwtProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
    }
}
