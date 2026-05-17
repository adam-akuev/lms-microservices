package com.lms.service;

import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.StudentRequest;
import com.lms.dto.StudentResponse;
import com.lms.model.Student;
import com.lms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
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

    public StudentResponse updateProfile(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Профиль студента не найден!"));

        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setPhone(request.phone());
        student.setBirthDate(request.birthDate());

        Student updatedStudent = studentRepository.save(student);
        return StudentResponse.fromEntity(updatedStudent);
    }
}
