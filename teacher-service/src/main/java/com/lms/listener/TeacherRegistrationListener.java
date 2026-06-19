package com.lms.listener;

import com.lms.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.lms.dto.event.TeacherRegistrationEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeacherRegistrationListener {

    private final TeacherService teacherService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "teacher-registration-queue", durable = "true"),
            exchange = @Exchange(value = "lms-exchange", type = "topic"),
            key = "user.registration.teacher"
    ))
    public void handleStudentRegistration(TeacherRegistrationEvent event) {
        log.info("Получено событие регистрации учителя из RabbitMQ c ID: {}", event.id());

        try {
            teacherService.createProfileFromEvent(event);
            log.info("Профиль учителя c ID: {} успешно создан", event.id());
        } catch (Exception e) {
            log.error("Ошибка при обработке регистрации учителя с ID: {}", event.id());
            throw e;
        }
    }
}
