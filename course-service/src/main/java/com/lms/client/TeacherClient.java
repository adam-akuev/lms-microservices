package com.lms.client;

import com.lms.dto.internal.teacher.TeacherResponseInternal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "teacher-service", path = "/api/teachers/internal")
public interface TeacherClient {

    @GetMapping("/{id}/exists")
    boolean existsById(@PathVariable("id") Long id);

    @GetMapping("/{id}")
    TeacherResponseInternal getProfileById(@PathVariable("id") Long id);

    @GetMapping("/bulk")
    List<TeacherResponseInternal> getProfilesByIds(@RequestParam("ids") List<Long> ids);
}
