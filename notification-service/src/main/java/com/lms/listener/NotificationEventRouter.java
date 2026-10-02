package com.lms.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.event.CertificateIssuedEvent;
import com.lms.dto.event.EnrollmentCreatedEvent;
import com.lms.dto.event.UserRegisteredEvent;
import com.lms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventRouter {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${app.rabbit.notification-queue}")
    public void route(Message message) throws Exception {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        byte[] body = message.getBody();

        log.info("Получено событие с routing key: {}", routingKey);

        switch (routingKey) {
            case "user.registered" -> handleUserRegistered(objectMapper.readValue(body, UserRegisteredEvent.class));
            case "enrollment.created" -> handleEnrollmentCreated(objectMapper.readValue(body, EnrollmentCreatedEvent.class));
            case "certificate.issued" -> handleCertificateIssued(objectMapper.readValue(body, CertificateIssuedEvent.class));
            default -> log.warn("Неизвестный routing key: {}, сообщение пропущено", routingKey);
        }
    }

    private void handleUserRegistered(UserRegisteredEvent event) {
        notificationService.sendOnce(
                event.eventId(),
                "USER_REGISTERED",
                event.email(),
                "Добро пожаловать в Lumina LMS!",
                "welcome",
                Map.of("fullName", event.fullName())
        );
    }

    private void handleEnrollmentCreated(EnrollmentCreatedEvent event) {
        notificationService.sendOnce(
                event.eventId(),
                "ENROLLMENT_CREATED",
                event.email(),
                "Вы записаны на курс «" + event.courseTitle() + "»",
                "enrollment-confirmed",
                Map.of(
                        "fullName", event.fullName(),
                        "courseTitle", event.courseTitle(),
                        "courseId", event.courseId()
                )
        );
    }

    private void handleCertificateIssued(CertificateIssuedEvent event) {
        notificationService.sendOnce(
                event.eventId(),
                "CERTIFICATE_ISSUED",
                event.email(),
                "Ваш сертификат готов! 🎓",
                "certificate-issued",
                Map.of(
                        "studentName", event.studentName(),
                        "courseTitle", event.courseTitle(),
                        "certificateNumber", event.certificateNumber(),
                        "score", event.score()
                )
        );
    }
}