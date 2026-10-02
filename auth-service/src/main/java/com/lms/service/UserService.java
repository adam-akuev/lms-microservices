package com.lms.service;

import com.lms.common.exception.ResourceNotFoundException;
import com.lms.common.exception.UserAlreadyExistsException;
import com.lms.dto.RegisterRequest;
import com.lms.dto.UserResponse;
import com.lms.model.User;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден!"));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public User saveNewUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(request.role());

        user.setPassword(passwordEncoder.encode(request.password()));

        return userRepository.save(user);
    }
}
