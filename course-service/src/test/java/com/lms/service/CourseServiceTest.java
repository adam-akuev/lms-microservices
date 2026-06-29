package com.lms.service;

import com.lms.client.EnrollmentClient;
import com.lms.client.TeacherClient;
import com.lms.common.exception.BaseException;
import com.lms.dto.course.CourseRequest;
import com.lms.dto.course.CourseResponse;
import com.lms.dto.internal.teacher.TeacherResponseInternal;
import com.lms.mapper.CourseMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private TeacherClient teacherClient;
    @Mock private EnrollmentClient enrollmentClient;
    @Mock private LessonProgressService lessonProgressService;
    @Mock private CourseMapper courseMapper;

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
        // Arrange
        Long studentId = 1L;
        Long teacherId = 5L;
        Long courseId = 10L;

        when(enrollmentClient.getStudentCourseIds(studentId)).thenReturn(List.of(courseId));

        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Java Advanced");
        course.setDescription("Advanced Java course");
        course.setPrice(BigDecimal.valueOf(100));
        course.setTeacherId(teacherId);
        when(courseRepository.findAllById(List.of(courseId))).thenReturn(List.of(course));

        TeacherResponseInternal teacherMock = new TeacherResponseInternal(teacherId, "Иван Иванов", null, null);
        when(teacherClient.getProfilesByIds(List.of(teacherId))).thenReturn(List.of(teacherMock));
        when(lessonProgressService.calculateCourseProgress(studentId, courseId)).thenReturn(75);

        // Мокаем маппер с ТРЕМЯ параметрами (как в реальном коде на строке 96)
        CourseResponse expectedResponse = new CourseResponse(
                courseId,
                "Java Advanced",
                "Advanced Java course",
                BigDecimal.valueOf(100),
                teacherMock,
                75
        );
        when(courseMapper.toResponse(any(Course.class), any(TeacherResponseInternal.class), anyInt()))
                .thenReturn(expectedResponse);

        // Act
        List<CourseResponse> result = courseService.getStudentCourses(studentId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java Advanced", result.get(0).title());
        assertEquals(75, result.get(0).progressPercentage());
        assertEquals("Иван Иванов", result.get(0).teacher().fullName());

        verify(lessonProgressService).calculateCourseProgress(studentId, courseId);
        verify(courseMapper).toResponse(course, teacherMock, 75);
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
        verifyNoInteractions(courseRepository, teacherClient, lessonProgressService, courseMapper);
    }

    @Test
    void create_Success_ByTeacher() {
        // Arrange
        Long currentUserId = 5L;
        mockAuthentication("ROLE_TEACHER", currentUserId);

        CourseRequest request = new CourseRequest("Spring Boot 3", "Desc", BigDecimal.TEN, null);

        when(teacherClient.existsById(currentUserId)).thenReturn(true);

        // Создаем сущность курса для маппера
        Course courseEntity = new Course();
        courseEntity.setTitle(request.title());
        courseEntity.setDescription(request.description());
        courseEntity.setPrice(request.price());
        courseEntity.setTeacherId(currentUserId);

        Course savedCourse = new Course();
        savedCourse.setId(100L);
        savedCourse.setTitle(request.title());
        savedCourse.setDescription(request.description());
        savedCourse.setPrice(request.price());
        savedCourse.setTeacherId(currentUserId);

        // Мокаем маппер: from request to entity
        when(courseMapper.toEntity(eq(request), eq(currentUserId))).thenReturn(courseEntity);
        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

        TeacherResponseInternal teacherMock = new TeacherResponseInternal(currentUserId, "Профессор", null, null);
        when(teacherClient.getProfileById(currentUserId)).thenReturn(teacherMock);

        // Мокаем маппер: from entity to response (в create() метод вызывается с 2 параметрами на строке 129)
        CourseResponse expectedResponse = new CourseResponse(
                100L,
                "Spring Boot 3",
                "Desc",
                BigDecimal.TEN,
                teacherMock,
                0
        );
        when(courseMapper.toResponse(any(Course.class), any(TeacherResponseInternal.class)))
                .thenReturn(expectedResponse);

        // Act
        CourseResponse response = courseService.create(request, currentUserId);

        // Assert
        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Spring Boot 3", response.title());
        assertEquals("Desc", response.description());
        assertEquals(BigDecimal.TEN, response.price());
        assertEquals(currentUserId, response.teacher().id());
        assertEquals("Профессор", response.teacher().fullName());

        verify(courseRepository).save(any(Course.class));
        verify(courseMapper).toEntity(request, currentUserId);
        verify(courseMapper).toResponse(savedCourse, teacherMock);
        verify(teacherClient).getProfileById(currentUserId);
    }

    @Test
    void create_ThrowsException_ByAdmin_WhenTeacherIdIsNull() {
        // Arrange
        Long adminId = 1L;
        mockAuthentication("ROLE_ADMIN", adminId);
        CourseRequest request = new CourseRequest("New Course", "Desc", BigDecimal.TEN, null);

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            courseService.create(request, adminId);
        });

        assertNotNull(exception);
        assertEquals("Администратор должен указать ID преподавателя при создании курса!", exception.getMessage());
        verifyNoInteractions(courseMapper, courseRepository, teacherClient);
    }

    @Test
    void delete_Success() {
        // Arrange
        Long courseId = 99L;
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Test Course");
        course.setDescription("Test Description");
        course.setPrice(BigDecimal.valueOf(50));
        course.setTeacherId(10L);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // Act
        courseService.delete(courseId);

        // Assert
        verify(courseRepository, times(1)).delete(course);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("lms-exchange"),
                eq("course.deleted.key"),
                eq(courseId)
        );
    }
}