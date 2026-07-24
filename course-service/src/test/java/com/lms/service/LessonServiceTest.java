package com.lms.service;

import com.lms.client.EnrollmentClient;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.lesson.LessonRequest;
import com.lms.dto.lesson.LessonResponse;
import com.lms.mapper.LessonMapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock private LessonRepository lessonRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentClient enrollmentClient;
    @Mock private LessonMapper lessonMapper;

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

        Lesson lesson = Lesson.builder().id(100L).title("Введение").content("Контент урока").build();
        when(lessonRepository.findByCourseId(courseId)).thenReturn(List.of(lesson));

        // Мокаем маппер
        LessonResponse expectedResponse = new LessonResponse(100L, "Введение", "Контент урока");
        when(lessonMapper.toResponse(lesson)).thenReturn(expectedResponse);

        // Act
        List<LessonResponse> result = lessonService.getAllLessonsForStudentByCourseId(studentId, courseId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).id());
        assertEquals("Введение", result.get(0).title());
        assertEquals("Контент урока", result.get(0).content());

        verifyNoInteractions(enrollmentClient);
        verify(lessonMapper, times(1)).toResponse(lesson);
    }

    @Test
    void getAllLessons_Success_WhenStudentIsEnrolled() {
        // Arrange
        Long courseId = 10L;
        Long studentId = 1L;
        mockAuthentication("ROLE_STUDENT");

        when(enrollmentClient.checkEnrollment(studentId, courseId)).thenReturn(true);

        Lesson lesson = Lesson.builder().id(100L).title("Введение").content("Контент урока").build();
        when(lessonRepository.findByCourseId(courseId)).thenReturn(List.of(lesson));

        // Мокаем маппер
        LessonResponse expectedResponse = new LessonResponse(100L, "Введение", "Контент урока");
        when(lessonMapper.toResponse(lesson)).thenReturn(expectedResponse);

        // Act
        List<LessonResponse> result = lessonService.getAllLessonsForStudentByCourseId(studentId, courseId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Введение", result.get(0).title());

        verify(enrollmentClient, times(1)).checkEnrollment(studentId, courseId);
        verify(lessonMapper, times(1)).toResponse(lesson);
    }

    @Test
    void getAllLessons_ThrowsAccessDeniedException_WhenStudentNotEnrolled() {
        // Arrange
        Long courseId = 10L;
        Long studentId = 1L;
        mockAuthentication("ROLE_STUDENT");

        when(enrollmentClient.checkEnrollment(studentId, courseId)).thenReturn(false);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            lessonService.getAllLessonsForStudentByCourseId(studentId, courseId);
        });

        verifyNoInteractions(lessonRepository);
        verifyNoInteractions(lessonMapper);
    }

    // --- Тесты для метода getById ---

    @Test
    void getById_Success() {
        // Arrange
        Long lessonId = 100L;
        Lesson lesson = Lesson.builder()
                .id(lessonId)
                .title("Урок 1")
                .content("Содержание урока")
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        LessonResponse expectedResponse = new LessonResponse(lessonId, "Урок 1", "Содержание урока");
        when(lessonMapper.toResponse(lesson)).thenReturn(expectedResponse);

        // Act
        LessonResponse response = lessonService.getById(lessonId);

        // Assert
        assertNotNull(response);
        assertEquals(lessonId, response.id());
        assertEquals("Урок 1", response.title());
        assertEquals("Содержание урока", response.content());

        verify(lessonMapper).toResponse(lesson);
    }

    @Test
    void getById_ThrowsResourceNotFoundException() {
        // Arrange
        Long lessonId = 999L;
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            lessonService.getById(lessonId);
        });

        verifyNoInteractions(lessonMapper);
    }

    // --- Тесты для метода create ---

    @Test
    void create_Success() {
        // Arrange
        Long courseId = 10L;
        LessonRequest request = new LessonRequest("Новый урок", "Контент", courseId);

        // Создаем сущность, которую вернет маппер
        Lesson lessonEntity = new Lesson();
        lessonEntity.setTitle(request.title());
        lessonEntity.setContent(request.content());

        Lesson savedLesson = Lesson.builder()
                .id(500L)
                .title(request.title())
                .content(request.content())
                .build();

        // Мокаем маппер и репозиторий
        when(lessonMapper.toEntity(request)).thenReturn(lessonEntity);
        when(lessonRepository.save(lessonEntity)).thenReturn(savedLesson);

        LessonResponse expectedResponse = new LessonResponse(500L, "Новый урок", "Контент");
        when(lessonMapper.toResponse(savedLesson)).thenReturn(expectedResponse);

        // Act
        LessonResponse response = lessonService.create(request);

        // Assert
        assertNotNull(response);
        assertEquals(500L, response.id());
        assertEquals("Новый урок", response.title());
        assertEquals("Контент", response.content());

        verify(lessonMapper).toEntity(request);
        verify(lessonRepository).save(lessonEntity);
        verify(lessonMapper).toResponse(savedLesson);
    }

    // --- Тесты для метода update ---

    @Test
    void update_Success() {
        // Arrange
        Long lessonId = 100L;
        Long courseId = 10L;
        LessonRequest request = new LessonRequest("Обновленный урок", "Новый контент", courseId);

        Lesson existingLesson = Lesson.builder()
                .id(lessonId)
                .title("Старый заголовок")
                .content("Старый контент")
                .build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(existingLesson));

        Lesson updatedLesson = Lesson.builder()
                .id(lessonId)
                .title("Обновленный урок")
                .content("Новый контент")
                .build();

        when(lessonRepository.save(existingLesson)).thenReturn(updatedLesson);

        LessonResponse expectedResponse = new LessonResponse(lessonId, "Обновленный урок", "Новый контент");
        when(lessonMapper.toResponse(updatedLesson)).thenReturn(expectedResponse);

        // Act
        LessonResponse response = lessonService.update(lessonId, request);

        // Assert
        assertNotNull(response);
        assertEquals(lessonId, response.id());
        assertEquals("Обновленный урок", response.title());
        assertEquals("Новый контент", response.content());

        verify(lessonMapper).updateEntityFromDto(request, existingLesson);
        verify(lessonRepository).save(existingLesson);
        verify(lessonMapper).toResponse(updatedLesson);
    }

    @Test
    void update_ThrowsResourceNotFoundException() {
        // Arrange
        Long lessonId = 999L;
        LessonRequest request = new LessonRequest("Урок", "Контент", 10L);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            lessonService.update(lessonId, request);
        });

        verifyNoInteractions(lessonMapper);
        verify(lessonRepository, never()).save(any());
    }

    // --- Тесты для метода delete ---

    @Test
    void delete_Success() {
        // Arrange
        Long lessonId = 100L;
        Lesson lesson = Lesson.builder().id(lessonId).title("Урок").build();

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        // Act
        lessonService.delete(lessonId);

        // Assert
        verify(lessonRepository).delete(lesson);
    }

    @Test
    void delete_ThrowsResourceNotFoundException() {
        // Arrange
        Long lessonId = 999L;

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            lessonService.delete(lessonId);
        });

        verify(lessonRepository, never()).delete(any());
    }
}