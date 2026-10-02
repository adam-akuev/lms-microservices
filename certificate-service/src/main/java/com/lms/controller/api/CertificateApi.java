package com.lms.controller.api;

import com.lms.dto.certificate.CertificateResponse;
import com.lms.dto.certificate.CertificateVerificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Сертификаты", description = "Выдача, скачивание и верификация сертификатов о прохождении курса")
public interface CertificateApi {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Мои сертификаты", description = "Возвращает список сертификатов текущего студента")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список сертификатов получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    ResponseEntity<List<CertificateResponse>> getMyCertificates(Long userId);

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Скачать сертификат в PDF", description = "Возвращает PDF-файл сертификата")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF успешно сгенерирован"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Сертификат принадлежит другому пользователю"),
            @ApiResponse(responseCode = "404", description = "Сертификат не найден")
    })
    ResponseEntity<ByteArrayResource> downloadCertificate(Long certificateId, Long userId);

    @Operation(
            summary = "Проверить подлинность сертификата",
            description = "Публичный эндпоинт без авторизации — для проверки сертификата работодателем по номеру"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Сертификат найден и подлинность подтверждена"),
            @ApiResponse(responseCode = "404", description = "Сертификат с таким номером не найден")
    })
    ResponseEntity<CertificateVerificationResponse> verifyCertificate(String certificateNumber);
}