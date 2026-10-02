package com.lms.service;

import com.lms.client.CourseClient;
import com.lms.client.UserClient;
import com.lms.common.exception.StudentAlreadyEnrolledException;
import com.lms.dto.enrollment.EnrollmentResponse;
import com.lms.dto.event.EnrollmentCreatedEvent;
import com.lms.dto.internal.CourseInternalResponse;
import com.lms.dto.internal.UserInternalResponse;
import com.lms.model.Enrollment;
import com.lms.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final StudentService studentService;
    private final EnrollmentRepository enrollmentRepository;
    private final UserClient userClient;
    private final CourseClient courseClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbit.notification-exchange}")
    private String notificationExchange;

    @Transactional
    public EnrollmentResponse enroll(Long studentId, Long courseId) {
        studentService.validateStudentExists(studentId);

        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new StudentAlreadyEnrolledException("Студент уже записан на этот курс");
        }

        Enrollment enrollment = Enrollment.builder()
                .studentId(studentId)
                .courseId(courseId)
                .enrolledAt(LocalDateTime.now())
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        publishEnrollmentCreatedEvent(studentId, courseId);

        return EnrollmentResponse.fromEntity(savedEnrollment);
    }

    private void publishEnrollmentCreatedEvent(Long studentId, Long courseId) {
        try {
            UserInternalResponse user = userClient.getUserById(studentId);
            CourseInternalResponse course = courseClient.getCourseById(courseId);

            EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(
                    UUID.randomUUID().toString(),
                    studentId,
                    user.email(),
                    user.fullName(),
                    courseId,
                    course.title()
            );

            rabbitTemplate.convertAndSend(notificationExchange, "enrollment.created", event);
        } catch (Exception e) {
            // Публикация события не должна ронять запись на курс —
            // студент обязан быть зачислен, даже если Rabbit/auth-service/course-service недоступны
            log.error("Не удалось опубликовать EnrollmentCreatedEvent для studentId={}, courseId={}", studentId, courseId, e);
        }
    }

    public boolean isStudentEnrolled(Long studentId, Long courseId) {
        studentService.validateStudentExists(studentId);
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    public List<Long> getStudentCourseIds(Long studentId) {
        studentService.validateStudentExists(studentId);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return enrollments.stream().map(Enrollment::getCourseId).toList();
    }

    @Transactional
    public void cascadeDeleteEnrollments(Long courseId) {
        enrollmentRepository.deleteByCourseId(courseId);
    }
}