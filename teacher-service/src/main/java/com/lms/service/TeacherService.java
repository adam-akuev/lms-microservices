package com.lms.service;

import com.lms.common.exception.BaseException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.TeacherRequest;
import com.lms.dto.TeacherResponse;
import com.lms.dto.event.TeacherRegistrationEvent;
import com.lms.model.Teacher;
import com.lms.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public Page<TeacherResponse> getTeachersWithFilter(String qualification, Pageable pageable) {
        if (qualification != null && !qualification.isBlank()) {
            return teacherRepository.findByQualificationContainingIgnoreCase(qualification, pageable)
                    .map(TeacherResponse::fromEntity);
        }

        return teacherRepository.findAll(pageable).map(TeacherResponse::fromEntity);
    }

    public List<TeacherResponse> getProfilesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return teacherRepository.findAllById(ids).stream()
                .map(TeacherResponse::fromEntity)
                .toList();
    }

    public TeacherResponse getProfileById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Профиль учителя не найден"));
        return TeacherResponse.fromEntity(teacher);
    }

    @Transactional
    public void createProfileFromEvent(TeacherRegistrationEvent request) {
        if (teacherRepository.existsById(request.id())) {
            throw new BaseException("Профиль учителя с таким ID уже существует", HttpStatus.CONFLICT);
        }

        Teacher teacher = Teacher.builder()
                .id(request.id())
                .fullName(request.fullName())
                .phone(request.phone())
                .build();

        teacherRepository.save(teacher);
    }

    @Transactional
    public TeacherResponse updateProfile(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Профиль учителя не найден"));

        teacher.setFullName(request.fullName());
        teacher.setPhone(request.phone());
        if (request.bio() != null) {
            teacher.setBio(request.bio());
        }

        if (request.qualification() != null) {
            teacher.setQualification(request.qualification());
        }

        if (request.experienceYears() != null) {
            teacher.setExperienceYears(request.experienceYears());
        }

        teacherRepository.save(teacher);
        return TeacherResponse.fromEntity(teacher);
    }

    @Transactional
    public void deactivateProfile(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Профиль учителя не найден"));

        teacher.setActive(false);
        teacherRepository.save(teacher);
    }

    public boolean exists(Long id) {
        return teacherRepository.existsById(id);
    }
}
