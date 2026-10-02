package com.lms.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.thymeleaf.context.Context;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void send(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            Context context = new Context();
            context.setVariables(variables);
            String htmlBody = templateEngine.process(templateName, context);

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Письмо '{}' успешно отправлено на {}", templateName, to);
        } catch (Exception e) {
            log.error("Не удалось отправить письмо '{}' на {}", templateName, to, e);
            throw new RuntimeException("Ошибка отправки email", e);
        }
    }
}