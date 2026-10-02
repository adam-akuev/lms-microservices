package com.lms.listener;

import com.lms.dto.event.QuizPassedEvent;
import com.lms.service.CertificateService;
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
public class QuizPassedListener {

    private final CertificateService certificateService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "certificate-issue-queue", durable = "true"),
            exchange = @Exchange(value = "lms-exchange", type = "topic"),
            key = "quiz.passed.key"
    ))
    public void handle(QuizPassedEvent event) {
        log.info("Получено событие QuizPassedEvent: userId={}, courseId={}, score={}",
                event.userId(), event.courseId(), event.score());
        try {
            certificateService.issueIfNotExists(event.userId(), event.courseId(), event.score());
            log.info("Обработка выдачи сертификата для userId={}, courseId={} успешно завершена",
                    event.userId(), event.courseId());
        } catch (Exception e) {
            log.error("Не удалось выдать сертификат для userId={}, courseId={}",
                    event.userId(), event.courseId(), e);
            throw e;
        }
    }
}