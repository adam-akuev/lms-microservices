package com.lms.service;

import com.lms.common.exception.BaseException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.event.StudentRegistrationEvent;
import com.lms.dto.student.StudentRequest;
import com.lms.dto.student.StudentResponse;
import com.lms.model.Student;
import com.lms.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    // --- Тесты для getProfileById ---

    @Test
    void getProfileById_ReturnsResponse_WhenStudentExists() {
        Long studentId = 1L;
        Student student = Student.builder()
                .id(studentId)
                .fullName("Иван Иванов")
                .phone("+79991112233")
                .build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        StudentResponse response = studentService.getProfileById(studentId);

        assertNotNull(response);
        assertEquals("Иван Иванов", response.fullName());
        assertEquals("+79991112233", response.phone());
        verify(studentRepository, times(1)).findById(studentId);
    }

    @Test
    void getProfileById_ThrowsResourceNotFoundException_WhenStudentDoesNotExist() {
        Long studentId = 1L;
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> 
                studentService.getProfileById(studentId)
        );

        assertEquals("Профиль студента не найден!", exception.getMessage());
    }

    // --- Тесты для createProfileFromEvent ---

    @Test
    void createProfileFromEvent_SavesStudent_WhenStudentDoesNotExist() {
        StudentRegistrationEvent event = new StudentRegistrationEvent(1L, "Петр Петров", "+79995554433");
        
        when(studentRepository.existsById(event.id())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> studentService.createProfileFromEvent(event));

        verify(studentRepository, times(1)).existsById(event.id());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void createProfileFromEvent_ThrowsBaseException_WhenStudentAlreadyExists() {
        StudentRegistrationEvent event = new StudentRegistrationEvent(1L, "Петр Петров", "+79995554433");

        when(studentRepository.existsById(event.id())).thenReturn(true);

        BaseException exception = assertThrows(BaseException.class, () -> 
                studentService.createProfileFromEvent(event)
        );

        assertEquals("Профиль студента с таким ID уже существует", exception.getMessage());
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(studentRepository, never()).save(any());
    }

    // --- Тесты для updateProfile ---

    @Test
    void updateProfile_UpdatesFields_WhenStudentExists() {
        Long studentId = 1L;
        Student existingStudent = Student.builder()
                .id(studentId)
                .fullName("Старое Имя")
                .phone("+70000000000")
                .build();

        StudentRequest request = new StudentRequest("Новое Имя", "+71111111111", LocalDate.of(2000, 1, 1));

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(existingStudent));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentResponse response = studentService.updateProfile(studentId, request);

        assertNotNull(response);
        assertEquals("Новое Имя", response.fullName());
        assertEquals("+71111111111", response.phone());
        // Здесь проверяем, что в саму сущность проставилась дата рождения (зависит от маппинга в StudentResponse)
        assertEquals(LocalDate.of(2000, 1, 1), existingStudent.getBirthDate()); 
        verify(studentRepository, times(1)).save(existingStudent);
    }

    // --- Тесты для validateStudentExists ---

    @Test
    void validateStudentExists_DoesNotThrow_WhenStudentExists() {
        Long studentId = 1L;
        when(studentRepository.existsById(studentId)).thenReturn(true);

        assertDoesNotThrow(() -> studentService.validateStudentExists(studentId));
    }

    @Test
    void validateStudentExists_ThrowsResourceNotFoundException_WhenStudentDoesNotExist() {
        Long studentId = 1L;
        when(studentRepository.existsById(studentId)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> 
                studentService.validateStudentExists(studentId)
        );

        assertEquals("Студент с ID 1 не найден", exception.getMessage());
    }
}