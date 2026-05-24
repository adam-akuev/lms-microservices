package com.lms.service;

import com.lms.common.exception.StudentAlreadyEnrolledException;
import com.lms.dto.enrollment.EnrollmentResponse;
import com.lms.model.Enrollment;
import com.lms.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final StudentService studentService;
    private final EnrollmentRepository enrollmentRepository;

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

        return EnrollmentResponse.fromEntity(savedEnrollment);
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
}
