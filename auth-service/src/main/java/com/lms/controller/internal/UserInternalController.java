package com.lms.controller.internal;

import com.lms.dto.internal.UserInternalResponse;
import com.lms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserInternalResponse> getUserById(@PathVariable("userId") Long userId) {
        var user = userService.getById(userId);
        return ResponseEntity.ok(new UserInternalResponse(user.id(), user.fullName(), user.email()));
    }
}