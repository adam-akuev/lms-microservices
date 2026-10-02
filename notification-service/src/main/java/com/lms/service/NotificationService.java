package com.lms.service;

import com.lms.model.NotificationLog;
import com.lms.model.NotificationStatus;
import com.lms.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository logRepository;
    private final EmailService emailService;

    /**
     * Идемпотентная отправка: если событие с таким eventId уже успешно обработано — просто выходим.
     * Иначе отправляем письмо и логируем результат (успех/ошибка).
     */
    @Transactional
    public void sendOnce(String eventId, String eventType, String recipient,
                          String subject, String template, Map<String, Object> variables) {

        if (logRepository.existsByEventIdAndStatus(eventId, NotificationStatus.SENT)) {
            log.info("Событие {} ({}) уже было обработано, повторно не отправляем", eventId, eventType);
            return;
        }

        try {
            emailService.send(recipient, subject, template, variables);
            saveLog(eventId, eventType, recipient, NotificationStatus.SENT, null);
        } catch (Exception e) {
            saveLog(eventId, eventType, recipient, NotificationStatus.FAILED, e.getMessage());
            throw e; // пробрасываем дальше — RabbitListener решит про retry/DLQ
        }
    }

    private void saveLog(String eventId, String eventType, String recipient,
                          NotificationStatus status, String errorMessage) {
        NotificationLog logEntry = NotificationLog.builder()
                .eventId(eventId)
                .eventType(eventType)
                .recipient(recipient)
                .status(status)
                .errorMessage(errorMessage)
                .build();
        logRepository.save(logEntry);
    }
}