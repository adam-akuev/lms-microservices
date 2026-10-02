package com.lms.controller;

import com.lms.controller.api.CertificateApi;
import com.lms.dto.certificate.CertificateResponse;
import com.lms.dto.certificate.CertificateVerificationResponse;
import com.lms.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController implements CertificateApi {

    private final CertificateService certificateService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CertificateResponse>> getMyCertificates(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(certificateService.getMyCertificates(userId));
    }

    @GetMapping("/{certificateId}/download")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ByteArrayResource> downloadCertificate(
            @PathVariable("certificateId") Long certificateId,
            @AuthenticationPrincipal Long userId
    ) {
        byte[] pdfBytes = certificateService.downloadCertificatePdf(certificateId, userId);
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate.pdf")
                .contentLength(pdfBytes.length)
                .body(resource);
    }

    @GetMapping("/verify/{certificateNumber}")
    public ResponseEntity<CertificateVerificationResponse> verifyCertificate(
            @PathVariable("certificateNumber") String certificateNumber
    ) {
        return ResponseEntity.ok(certificateService.verify(certificateNumber));
    }
}