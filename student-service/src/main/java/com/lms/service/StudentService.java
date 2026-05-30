package com.lms.service;

import com.lms.common.exception.BaseException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.student.StudentRequest;
import com.lms.dto.student.StudentResponse;
import com.lms.dto.event.StudentRegistrationEvent;
import com.lms.model.Student;
import com.lms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentResponse getProfileById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Профиль студента не найден!"));

        return StudentResponse.fromEntity(student);
    }

    @Transactional
    public void createProfileFromEvent(StudentRegistrationEvent request) {
        if (studentRepository.existsById(request.id())) {
            throw new BaseException("Профиль студента с таким ID уже существует", HttpStatus.CONFLICT);
        }

        Student student = Student.builder()
                .id(request.id())
                .fullName(request.fullName())
                .phone(request.phone())
                .build();

         studentRepository.save(student);
    }

    @Transactional
    public StudentResponse updateProfile(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Профиль студента не найден!"));

        if (request.fullName() != null) {
            student.setFullName(request.fullName());
        }

        if (request.phone() != null) {
            student.setPhone(request.phone());
        }

        if (request.birthDate() != null) {
            student.setBirthDate(request.birthDate());
        }

        Student updatedStudent = studentRepository.save(student);
        return StudentResponse.fromEntity(updatedStudent);
    }

    public void validateStudentExists(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Студент с ID " + studentId + " не найден");
        }
    }
}
