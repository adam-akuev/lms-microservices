package com.lms.service;

import com.lms.client.EnrollmentClient;
import com.lms.client.TeacherClient;
import com.lms.common.exception.BaseException;
import com.lms.dto.course.CourseRequest;
import com.lms.dto.course.CourseResponse;
import com.lms.dto.internal.teacher.TeacherResponseInternal;
import com.lms.model.Course;
import com.lms.repository.CourseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private TeacherClient teacherClient;
    @Mock private EnrollmentClient enrollmentClient;
    @Mock private LessonProgressService lessonProgressService;

    @InjectMocks
    private CourseService courseService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthentication(String role, Object principal) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getPrincipal()).thenReturn(principal);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        lenient().doReturn(authorities).when(authentication).getAuthorities();
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getStudentCourses_Success() {
        Long studentId = 1L;
        Long teacherId = 5L;
        Long courseId = 10L;

        when(enrollmentClient.getStudentCourseIds(studentId)).thenReturn(List.of(courseId));

        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Java Advanced");
        course.setTeacherId(teacherId);
        when(courseRepository.findAllById(List.of(courseId))).thenReturn(List.of(course));

        TeacherResponseInternal teacherMock = new TeacherResponseInternal(teacherId, "Иван Иванов", null, null);
        when(teacherClient.getProfilesByIds(List.of(teacherId))).thenReturn(List.of(teacherMock));
        when(lessonProgressService.calculateCourseProgress(studentId, courseId)).thenReturn(75);
        List<CourseResponse> result = courseService.getStudentCourses(studentId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java Advanced", result.get(0).title());
        assertEquals(75, result.get(0).progressPercentage());
        assertEquals("Иван Иванов", result.get(0).teacher().fullName());
    }

    @Test
    void getStudentCourses_ReturnEmptyList_WhenNoEnrollments() {
        // Arrange
        Long studentId = 1L;
        when(enrollmentClient.getStudentCourseIds(studentId)).thenReturn(Collections.emptyList());

        // Act
        List<CourseResponse> result = courseService.getStudentCourses(studentId);

        // Assert
        assertTrue(result.isEmpty());
        verifyNoInteractions(courseRepository, teacherClient, lessonProgressService); // Проверяем, что дальше код не шел
    }

    @Test
    void create_Success_ByTeacher() {
        // Arrange
        Long currentUserId = 5L; // ID Преподавателя
        mockAuthentication("ROLE_TEACHER", currentUserId);

        CourseRequest request = new CourseRequest("Spring Boot 3", "Desc", BigDecimal.TEN, null);

        when(teacherClient.existsById(currentUserId)).thenReturn(true);

        Course savedCourse = new Course();
        savedCourse.setId(100L);
        savedCourse.setTitle(request.title());
        savedCourse.setTeacherId(currentUserId);

        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

        TeacherResponseInternal teacherMock = new TeacherResponseInternal(currentUserId, "Профессор", null, null);
        when(teacherClient.getProfileById(currentUserId)).thenReturn(teacherMock);

        // Act
        CourseResponse response = courseService.create(request, currentUserId);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals(currentUserId, response.teacher().id());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void create_ThrowsException_ByAdmin_WhenTeacherIdIsNull() {
        // Arrange
        Long adminId = 1L;
        mockAuthentication("ROLE_ADMIN", adminId);
        // Админ передает null вместо ID учителя
        CourseRequest request = new CourseRequest("New Course", "Desc", BigDecimal.TEN, null);

        // Act & Assert
        assertThrows(BaseException.class, () -> {
            courseService.create(request, adminId);
        });
    }

    @Test
    void delete_Success() {
        // Arrange
        Long courseId = 99L;
        Course course = new Course();
        course.setId(courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // Act
        courseService.delete(courseId);

        // Assert
        verify(courseRepository, times(1)).delete(course);

        // Проверяем, что сообщение улетело в RabbitMQ с правильными параметрами
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("lms-exchange"),
                eq("course.deleted.key"),
                eq(courseId)
        );
    }
}
