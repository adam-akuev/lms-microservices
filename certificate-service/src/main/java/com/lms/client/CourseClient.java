package com.lms.client;

import com.lms.dto.internal.CourseInternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service")
public interface CourseClient {

    @GetMapping("/api/courses/internal/{courseId}")
    CourseInternalResponse getCourseById(@PathVariable("courseId") Long courseId);
}