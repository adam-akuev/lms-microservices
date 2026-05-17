package com.lms.service;

import com.lms.client.StudentClient;
import com.lms.common.exception.InvalidCredentialsException;
import com.lms.common.exception.UserAlreadyExistsException;
import com.lms.dto.LoginRequest;
import com.lms.dto.RegisterRequest;
import com.lms.dto.internal.CreateStudentProfileRequest;
import com.lms.model.Role;
import com.lms.model.User;
import com.lms.repository.UserRepository;
import com.lms.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StudentClient studentClient;
    private final UserService userService;

    public void register(RegisterRequest request) {
        User user = userService.saveNewUser(request);

        if (request.role() == Role.ROLE_STUDENT) {
            CreateStudentProfileRequest studentProfileRequest = new CreateStudentProfileRequest(
                    user.getId(),
                    request.fullName(),
                    request.phoneNumber()
            );

            studentClient.createProfile(studentProfileRequest);
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
