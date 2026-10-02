package com.lms.pdf;

import com.lms.model.Certificate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Component
public class CertificatePdfGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public byte[] generate(Certificate certificate) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDFont font = PDType0Font.load(document, new ClassPathResource("fonts/DejaVuSans.ttf").getInputStream());

            float pageWidth = page.getMediaBox().getWidth();
            float centerY = page.getMediaBox().getHeight() / 2;

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                drawCentered(cs, font, 28, pageWidth, centerY + 160, "СЕРТИФИКАТ");
                drawCentered(cs, font, 12, pageWidth, centerY + 120, "о прохождении курса");
                drawCentered(cs, font, 20, pageWidth, centerY + 70, certificate.getStudentName());
                drawCentered(cs, font, 14, pageWidth, centerY + 30, "успешно завершил(а) курс");
                drawCentered(cs, font, 16, pageWidth, centerY, "«" + certificate.getCourseTitle() + "»");
                drawCentered(cs, font, 12, pageWidth, centerY - 40, "Результат теста: " + certificate.getScore() + "%");
                drawCentered(cs, font, 12, pageWidth, centerY - 60,
                        "Дата выдачи: " + certificate.getIssuedAt().format(DATE_FORMAT));
                drawCentered(cs, font, 10, pageWidth, centerY - 100,
                        "Номер сертификата: " + certificate.getCertificateNumber());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Не удалось сгенерировать PDF сертификата", e);
        }
    }

    private void drawCentered(PDPageContentStream cs, PDFont font, float fontSize,
                               float pageWidth, float y, String text) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float x = (pageWidth - textWidth) / 2;

        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }
}