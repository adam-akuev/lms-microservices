package com.lms.service;

import com.lms.common.exception.BaseException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.StudentRequest;
import com.lms.dto.StudentResponse;
import com.lms.dto.internal.CreateStudentProfileRequest;
import com.lms.model.Student;
import com.lms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentResponse getProfileById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Профиль студента не найден!"));

        return StudentResponse.fromEntity(student);
    }

    public void createProfile(CreateStudentProfileRequest request) {
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

    public StudentResponse updateProfile(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Профиль студента не найден!"));

        student.setFullName(request.fullName());
        student.setPhone(request.phone());
        student.setBirthDate(request.birthDate());

        Student updatedStudent = studentRepository.save(student);
        return StudentResponse.fromEntity(updatedStudent);
    }
}
