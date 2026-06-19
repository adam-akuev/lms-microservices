package com.lms.service;

import com.lms.common.exception.ResourceNotFoundException;
import com.lms.common.exception.StudentAlreadyEnrolledException;
import com.lms.dto.enrollment.EnrollmentResponse;
import com.lms.model.Enrollment;
import com.lms.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private StudentService studentService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    // --- Тесты для метода enroll ---

    @Test
    void enroll_SavesEnrollment_WhenDataIsValid() {
        Long studentId = 1L;
        Long courseId = 10L;
        
        Enrollment savedEnrollment = Enrollment.builder()
                .id(100L)
                .studentId(studentId)
                .courseId(courseId)
                .enrolledAt(LocalDateTime.now())
                .build();

        // Симулируем, что студент существует (метод void ничего не выбрасывает)
        doNothing().when(studentService).validateStudentExists(studentId);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(savedEnrollment);

        // Предполагается, что в EnrollmentResponse есть статический фабричный метод (аналогично прошлым DTO)
        EnrollmentResponse response = enrollmentService.enroll(studentId, courseId);

        assertNotNull(response);
        verify(studentService, times(1)).validateStudentExists(studentId);
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void enroll_ThrowsStudentAlreadyEnrolledException_WhenAlreadyEnrolled() {
        Long studentId = 1L;
        Long courseId = 10L;

        doNothing().when(studentService).validateStudentExists(studentId);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);

        assertThrows(StudentAlreadyEnrolledException.class, () -> 
                enrollmentService.enroll(studentId, courseId)
        );

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enroll_ThrowsResourceNotFoundException_WhenStudentDoesNotExist() {
        Long studentId = 999L;
        Long courseId = 10L;

        // Симулируем, что метод валидации выбросил исключение
        doThrow(new ResourceNotFoundException("Студент не найден"))
                .when(studentService).validateStudentExists(studentId);

        assertThrows(ResourceNotFoundException.class, () -> 
                enrollmentService.enroll(studentId, courseId)
        );

        verifyNoInteractions(enrollmentRepository);
    }

    // --- Тесты для метода isStudentEnrolled ---

    @Test
    void isStudentEnrolled_ReturnsTrue_WhenEnrollmentExists() {
        Long studentId = 1L;
        Long courseId = 10L;

        doNothing().when(studentService).validateStudentExists(studentId);
        when(enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)).thenReturn(true);

        boolean result = enrollmentService.isStudentEnrolled(studentId, courseId);

        assertTrue(result);
    }

    // --- Тесты для метода getStudentCourseIds ---

    @Test
    void getStudentCourseIds_ReturnsListOfCourseIds() {
        Long studentId = 1L;
        List<Enrollment> mockEnrollments = List.of(
                Enrollment.builder().courseId(10L).build(),
                Enrollment.builder().courseId(20L).build()
        );

        doNothing().when(studentService).validateStudentExists(studentId);
        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(mockEnrollments);

        List<Long> courseIds = enrollmentService.getStudentCourseIds(studentId);

        assertNotNull(courseIds);
        assertEquals(2, courseIds.size());
        assertTrue(courseIds.containsAll(List.of(10L, 20L)));
    }

    // --- Тесты для метода cascadeDeleteEnrollments ---

    @Test
    void cascadeDeleteEnrollments_CallsRepositoryDelete() {
        Long courseId = 50L;
        doNothing().when(enrollmentRepository).deleteByCourseId(courseId);

        assertDoesNotThrow(() -> enrollmentService.cascadeDeleteEnrollments(courseId));

        verify(enrollmentRepository, times(1)).deleteByCourseId(courseId);
    }
}