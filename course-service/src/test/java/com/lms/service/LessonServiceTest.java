package com.lms.service;

import com.lms.client.EnrollmentClient;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.lesson.LessonRequest;
import com.lms.dto.lesson.LessonResponse;
import com.lms.model.Course;
import com.lms.model.Lesson;
import com.lms.repository.CourseRepository;
import com.lms.repository.LessonRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock private LessonRepository lessonRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentClient enrollmentClient;

    @InjectMocks
    private LessonService lessonService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthentication(String role) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        
        lenient().doReturn(authorities).when(authentication).getAuthorities();
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        
        SecurityContextHolder.setContext(securityContext);
    }

    // --- Тесты для метода getAllLessonsForStudentByCourseId ---

    @Test
    void getAllLessons_Success_WhenUserIsTeacherOrAdmin() {
        // Arrange
        Long courseId = 10L;
        Long studentId = 1L;
        mockAuthentication("ROLE_TEACHER");

        Lesson lesson = Lesson.builder().id(100L).title("Введение").build();
        when(lessonRepository.findByCourseId(courseId)).thenReturn(List.of(lesson));

        // Act
        List<LessonResponse> result = lessonService.getAllLessonsForStudentByCourseId(studentId, courseId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Введение", result.get(0).title());
        // Проверяем, что к Feign-клиенту подписок даже не обращались, так как это учитель
        verifyNoInteractions(enrollmentClient); 
    }

    @Test
    void getAllLessons_Success_WhenStudentIsEnrolled() {
        // Arrange
        Long courseId = 10L;
        Long studentId = 1L;
        mockAuthentication("ROLE_STUDENT");

        when(enrollmentClient.checkEnrollment(studentId, courseId)).thenReturn(true);

        Lesson lesson = Lesson.builder().id(100L).title("Введение").build();
        when(lessonRepository.findByCourseId(courseId)).thenReturn(List.of(lesson));

        // Act
        List<LessonResponse> result = lessonService.getAllLessonsForStudentByCourseId(studentId, courseId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(enrollmentClient, times(1)).checkEnrollment(studentId, courseId);
    }

    @Test
    void getAllLessons_ThrowsAccessDeniedException_WhenStudentNotEnrolled() {
        // Arrange
        Long courseId = 10L;
        Long studentId = 1L;
        mockAuthentication("ROLE_STUDENT");

        // Симулируем, что студент НЕ записан на курс
        when(enrollmentClient.checkEnrollment(studentId, courseId)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            lessonService.getAllLessonsForStudentByCourseId(studentId, courseId);
        });

        // Уроки не должны запрашиваться из базы данных при отсутствии доступа
        verifyNoInteractions(lessonRepository);
    }

    // --- Тесты для метода create ---

    @Test
    void create_Success() {
        // Arrange
        Long courseId = 10L;
        LessonRequest request = new LessonRequest("Новый урок", "Контент", courseId);
        Course course = new Course();
        course.setId(courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        
        Lesson savedLesson = Lesson.builder()
                .id(500L)
                .title(request.title())
                .content(request.content())
                .course(course)
                .build();
        when(lessonRepository.save(any(Lesson.class))).thenReturn(savedLesson);

        // Act
        LessonResponse response = lessonService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(500L, response.id());
        assertEquals("Новый урок", response.title());
        verify(lessonRepository, times(1)).save(any(Lesson.class));
    }

    @Test
    void create_ThrowsResourceNotFoundException_WhenCourseNotExists() {
        // Arrange
        Long courseId = 10L;
        LessonRequest request = new LessonRequest("Урок", "Контент", courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            lessonService.create(request);
        });

        verifyNoInteractions(lessonRepository);
    }
}