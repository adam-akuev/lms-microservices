package com.lms.service;

import com.lms.client.CourseClient;
import com.lms.client.UserClient;
import com.lms.common.exception.CertificateAccessDeniedException;
import com.lms.common.exception.ResourceNotFoundException;
import com.lms.dto.certificate.CertificateResponse;
import com.lms.dto.certificate.CertificateVerificationResponse;
import com.lms.dto.event.CertificateIssuedEvent;
import com.lms.dto.internal.CourseInternalResponse;
import com.lms.dto.internal.UserInternalResponse;
import com.lms.model.Certificate;
import com.lms.pdf.CertificatePdfGenerator;
import com.lms.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateService {

    @Value("${app.rabbit.notification-exchange}")
    private String notificationExchange;

    private final CertificateRepository certificateRepository;
    private final CourseClient courseClient;
    private final UserClient userClient;
    private final CertificatePdfGenerator pdfGenerator;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Идемпотентная выдача сертификата.
     * Обогащает данные снэпшотом названия курса и имени студента через Feign (GET),
     * чтобы дальнейшие операции (скачивание, верификация) не зависели от других сервисов.
     */
    @Transactional
    public void issueIfNotExists(Long userId, Long courseId, int score) {
        if (certificateRepository.findByUserIdAndCourseId(userId, courseId).isPresent()) {
            return;
        }

        CourseInternalResponse course = courseClient.getCourseById(courseId);
        UserInternalResponse user = userClient.getUserById(userId);

        Certificate certificate = Certificate.builder()
                .userId(userId)
                .courseId(courseId)
                .courseTitle(course.title())
                .studentName(user.fullName())
                .score(score)
                .build();

        certificateRepository.save(certificate);

        publishCertificateIssuedEvent(certificate, user.email());
    }

    private void publishCertificateIssuedEvent(Certificate certificate, String email) {
        try {
            CertificateIssuedEvent event = new CertificateIssuedEvent(
                    UUID.randomUUID().toString(),
                    certificate.getUserId(),
                    email,
                    certificate.getStudentName(),
                    certificate.getCourseTitle(),
                    certificate.getCertificateNumber(),
                    certificate.getScore()
            );
            rabbitTemplate.convertAndSend(notificationExchange, "certificate.issued", event);
        } catch (Exception e) {
            log.error("Не удалось опубликовать CertificateIssuedEvent для certificateId={}", certificate.getId(), e);
        }
    }

    public List<CertificateResponse> getMyCertificates(Long userId) {
        return certificateRepository.findByUserIdOrderByIssuedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public byte[] downloadCertificatePdf(Long certificateId, Long requesterUserId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Сертификат не найден"));

        if (!certificate.getUserId().equals(requesterUserId)) {
            throw new CertificateAccessDeniedException("Этот сертификат принадлежит другому пользователю");
        }

        return pdfGenerator.generate(certificate);
    }

    public CertificateVerificationResponse verify(String certificateNumber) {
        Certificate certificate = certificateRepository.findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Сертификат с таким номером не найден"));

        return new CertificateVerificationResponse(
                certificate.getCertificateNumber(),
                certificate.getStudentName(),
                certificate.getCourseTitle(),
                certificate.getScore(),
                certificate.getIssuedAt(),
                true
        );
    }

    private CertificateResponse toResponse(Certificate certificate) {
        return new CertificateResponse(
                certificate.getId(),
                certificate.getCourseId(),
                certificate.getCourseTitle(),
                certificate.getCertificateNumber(),
                certificate.getScore(),
                certificate.getIssuedAt()
        );
    }
}