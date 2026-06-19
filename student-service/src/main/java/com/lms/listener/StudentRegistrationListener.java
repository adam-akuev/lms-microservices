package com.lms.listener;

import com.lms.dto.event.StudentRegistrationEvent;
import com.lms.service.StudentService;
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
public class StudentRegistrationListener {

    private final StudentService studentService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "student-registration-queue", durable = "true"),
            exchange = @Exchange(value = "lms-exchange", type = "topic"),
            key = "user.registration.student"
    ))
    public void handleStudentRegistration(StudentRegistrationEvent event) {
        log.info("Получено событие регистрации студента из RabbitMQ c ID: {}", event.id());

        try {
            studentService.createProfileFromEvent(event);
            log.info("Профиль студента c ID: {} успешно создан", event.id());
        } catch (Exception e) {
            log.error("Ошибка при обработке регистрации студента с ID: {}", event.id());
            throw e;
        }
    }
}
