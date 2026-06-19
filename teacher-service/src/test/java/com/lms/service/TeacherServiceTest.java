package com.lms.service;

import com.lms.common.exception.BaseException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.TeacherRequest;
import com.lms.dto.TeacherResponse;
import com.lms.dto.event.TeacherRegistrationEvent;
import com.lms.dto.internal.TeacherResponseInternal;
import com.lms.model.Teacher;
import com.lms.repository.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private TeacherService teacherService;

    // --- Тесты для getTeachersWithFilter ---

    @Test
    void getTeachersWithFilter_ReturnsFilteredPage_WhenQualificationIsProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        String qualification = "Java";
        Teacher teacher = Teacher.builder().id(1L).fullName("Сергей Петров").qualification("Java Senior").build();
        Page<Teacher> teacherPage = new PageImpl<>(List.of(teacher));

        when(teacherRepository.findByQualificationContainingIgnoreCase(eq(qualification), eq(pageable)))
                .thenReturn(teacherPage);

        Page<TeacherResponse> result = teacherService.getTeachersWithFilter(qualification, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(teacherRepository, times(1)).findByQualificationContainingIgnoreCase(qualification, pageable);
        verify(teacherRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getTeachersWithFilter_ReturnsAllPage_WhenQualificationIsEmptyOrNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Teacher teacher = Teacher.builder().id(1L).fullName("Сергей Петров").build();
        Page<Teacher> teacherPage = new PageImpl<>(List.of(teacher));

        when(teacherRepository.findAll(pageable)).thenReturn(teacherPage);

        Page<TeacherResponse> resultNull = teacherService.getTeachersWithFilter(null, pageable);
        Page<TeacherResponse> resultBlank = teacherService.getTeachersWithFilter("   ", pageable);

        assertNotNull(resultNull);
        assertNotNull(resultBlank);
        verify(teacherRepository, times(2)).findAll(pageable);
    }

    // --- Тесты для getProfilesByIds ---

    @Test
    void getProfilesByIds_ReturnsList_WhenIdsAreValid() {
        List<Long> ids = List.of(1L, 2L);
        Teacher t1 = Teacher.builder().id(1L).fullName("Преподаватель 1").build();
        Teacher t2 = Teacher.builder().id(2L).fullName("Преподаватель 2").build();

        when(teacherRepository.findAllById(ids)).thenReturn(List.of(t1, t2));

        List<TeacherResponseInternal> result = teacherService.getProfilesByIds(ids);

        assertEquals(2, result.size());
        verify(teacherRepository, times(1)).findAllById(ids);
    }

    @Test
    void getProfilesByIds_ReturnsEmptyList_WhenIdsNullOrEmpty() {
        assertTrue(teacherService.getProfilesByIds(null).isEmpty());
        assertTrue(teacherService.getProfilesByIds(List.of()).isEmpty());
        verifyNoInteractions(teacherRepository);
    }

    // --- Тесты для getProfileById и getProfileByIdInternal ---

    @Test
    void getProfileById_ReturnsResponse_WhenTeacherExists() {
        Long id = 1L;
        Teacher teacher = Teacher.builder().id(id).fullName("Иван Иванов").build();
        when(teacherRepository.findById(id)).thenReturn(Optional.of(teacher));

        TeacherResponse response = teacherService.getProfileById(id);

        assertNotNull(response);
        verify(teacherRepository, times(1)).findById(id);
    }

    @Test
    void getProfileById_ThrowsException_WhenTeacherDoesNotExist() {
        Long id = 1L;
        when(teacherRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teacherService.getProfileById(id));
    }

    // --- Тесты для createProfileFromEvent ---

    @Test
    void createProfileFromEvent_SavesTeacher_WhenIdIsNew() {
        TeacherRegistrationEvent event = new TeacherRegistrationEvent(1L, "Ольга Сидорова", "+79991112233");
        when(teacherRepository.existsById(event.id())).thenReturn(false);

        teacherService.createProfileFromEvent(event);

        verify(teacherRepository, times(1)).save(any(Teacher.class));
    }

    @Test
    void createProfileFromEvent_ThrowsException_WhenIdAlreadyExists() {
        TeacherRegistrationEvent event = new TeacherRegistrationEvent(1L, "Ольга Сидорова", "+79991112233");
        when(teacherRepository.existsById(event.id())).thenReturn(true);

        BaseException ex = assertThrows(BaseException.class, () -> teacherService.createProfileFromEvent(event));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    // --- Тесты для updateProfile ---

    @Test
    void updateProfile_UpdatesFields_WhenTeacherExists() {
        Long id = 1L;
        Teacher existingTeacher = Teacher.builder()
                .id(id)
                .fullName("Старое Имя")
                .qualification("Middle")
                .experienceYears(2)
                .build();

        TeacherRequest request = new TeacherRequest(
                "Новое Имя", "+71112223344", LocalDate.of(1985, 3, 15), 
                "Краткая био", "Senior", 10
        );

        when(teacherRepository.findById(id)).thenReturn(Optional.of(existingTeacher));

        teacherService.updateProfile(id, request);

        assertEquals("Новое Имя", existingTeacher.getFullName());
        assertEquals("+71112223344", existingTeacher.getPhone());
        assertEquals("Краткая био", existingTeacher.getBio());
        assertEquals("Senior", existingTeacher.getQualification());
        assertEquals(10, existingTeacher.getExperienceYears());
        verify(teacherRepository, times(1)).save(existingTeacher);
    }

    // --- Тесты для deactivateProfile ---

    @Test
    void deactivateProfile_SetsActiveFalse_WhenTeacherExists() {
        Long id = 1L;
        Teacher teacher = Teacher.builder().id(id).fullName("Учитель").active(true).build();
        when(teacherRepository.findById(id)).thenReturn(Optional.of(teacher));

        teacherService.deactivateProfile(id);

        assertFalse(teacher.isActive());
        verify(teacherRepository, times(1)).save(teacher);
    }

    // --- Тесты для exists ---

    @Test
    void exists_ReturnsTrue_WhenTeacherExists() {
        Long id = 1L;
        when(teacherRepository.existsById(id)).thenReturn(true);

        assertTrue(teacherService.exists(id));
    }
}