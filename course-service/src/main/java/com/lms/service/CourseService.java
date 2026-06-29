package com.lms.service;

import com.lms.client.EnrollmentClient;
import com.lms.client.TeacherClient;
import com.lms.common.exception.BaseException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.course.CourseRequest;
import com.lms.dto.course.CourseResponse;
import com.lms.dto.internal.teacher.TeacherResponseInternal;
import com.lms.mapper.CourseMapper;
import com.lms.model.Course;
import com.lms.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final RabbitTemplate rabbitTemplate;
    private final TeacherClient teacherClient;
    private final EnrollmentClient enrollmentClient;
    private final LessonProgressService lessonProgressService;
    private final CourseMapper courseMapper;

    public List<CourseResponse> getAll() {
        List<Course> courses = courseRepository.findAll();

        List<Long> teacherIds = courses.stream()
                .map(Course::getTeacherId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<TeacherResponseInternal> teachers = teacherClient.getProfilesByIds(teacherIds);

        Map<Long, TeacherResponseInternal> teacherMap = teachers.stream()
                .collect(Collectors.toMap(TeacherResponseInternal::id, t -> t));

        return courses.stream()
                .map(course -> courseMapper.toResponse(course, teacherMap.get(course.getTeacherId())))
                .toList();
    }

    public CourseResponse getById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Курс не найден!"));

        TeacherResponseInternal teacher = null;
        if (course.getTeacherId() != null) {
            try {
                teacher = teacherClient.getProfileById(course.getTeacherId());
            } catch (Exception ignored) {
            }
        }

        Integer progressPercentage = getProgressIfCurrentUserIsStudent(id);

        return courseMapper.toResponse(course, teacher, progressPercentage);
    }

    public List<CourseResponse> getStudentCourses(Long studentId) {
        List<Long> myCourseIds = enrollmentClient.getStudentCourseIds(studentId);

        if (myCourseIds.isEmpty()) {
            return List.of();
        }

        List<Course> courses = courseRepository.findAllById(myCourseIds);

        List<Long> teachers = courses.stream()
                .map(Course::getTeacherId)
                .distinct()
                .toList();

        Map<Long, TeacherResponseInternal> teacherMap = teacherClient.getProfilesByIds(teachers)
                .stream()
                .collect(Collectors.toMap(TeacherResponseInternal::id, t -> t));

        return courses.stream()
                .map(course -> {
                    int progressPercentage = lessonProgressService.calculateCourseProgress(studentId, course.getId());
                    return courseMapper.toResponse(course, teacherMap.get(course.getTeacherId()), progressPercentage);
                })
                .toList();
    }

    @Transactional
    public CourseResponse create(CourseRequest request, Long currentUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isTeacher = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));

        Long targetTeacherId;

        if (isTeacher) {
            targetTeacherId = currentUserId;
        } else {
            if (request.teacherId() == null) {
                throw new BaseException("Администратор должен указать ID преподавателя при создании курса!", HttpStatus.BAD_REQUEST);
            }
            targetTeacherId = request.teacherId();
        }

        boolean teacherExists = teacherClient.existsById(targetTeacherId);
        if (!teacherExists) {
            throw new ResourceNotFoundException("Преподаватель с id " + targetTeacherId + " не найден!");
        }

        Course course = courseMapper.toEntity(request, targetTeacherId);

        Course saved = courseRepository.save(course);
        TeacherResponseInternal teacher = teacherClient.getProfileById(saved.getTeacherId());

        return courseMapper.toResponse(saved, teacher);
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Курс не найден!"));

        if (!course.getTeacherId().equals(request.teacherId())) {
            boolean teacherExists = teacherClient.existsById(request.teacherId());
            if (!teacherExists) {
                throw new ResourceNotFoundException("Преподаватель с id " + request.teacherId() + " не найден!");
            }
            course.setTeacherId(request.teacherId());
        }

        courseMapper.updateEntityFromDto(request, course);

        Course saved = courseRepository.save(course);
        TeacherResponseInternal teacher = teacherClient.getProfileById(saved.getTeacherId());

        return courseMapper.toResponse(saved, teacher);
    }

    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Курс с id " + id + " не найден!"));

        courseRepository.delete(course);

        rabbitTemplate.convertAndSend(
                "lms-exchange",
                "course.deleted.key",
                id
        );
    }

    private Integer getProgressIfCurrentUserIsStudent(Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        boolean isStudent = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_STUDENT"));

        if (isStudent) {
            try {
                Long studentId = (Long) authentication.getPrincipal();
                return lessonProgressService.calculateCourseProgress(studentId, courseId);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
