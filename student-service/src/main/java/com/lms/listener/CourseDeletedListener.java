package com.lms.listener;

import com.lms.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseDeletedListener {

    private final EnrollmentService enrollmentService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "course-delete-queue", durable = "true"),
            exchange = @Exchange(value = "lms-exchange", type = "topic"),
            key = "course.deleted.key"
    ))
    public void handleCourseDeleted(Long courseId) {
        log.info("Получено событие удаление курса из RabbitMQ. ID курса: {}", courseId);
        try {
            enrollmentService.cascadeDeleteEnrollments(courseId);
            log.info("Каскадное удаление записей для курса ID: {} успешно завершено", courseId);
        } catch (Exception e) {
            log.error("Ошибка при каскадном удалении записей для курса ID: {}", courseId);
            throw e;
        }
    }
}
