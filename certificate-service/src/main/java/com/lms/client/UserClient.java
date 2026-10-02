package com.lms.client;

import com.lms.dto.internal.UserInternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface UserClient {

    @GetMapping("/api/users/internal/{userId}")
    UserInternalResponse getUserById(@PathVariable("userId") Long userId);
}