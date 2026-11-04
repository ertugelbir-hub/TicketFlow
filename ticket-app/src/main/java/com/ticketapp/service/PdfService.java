package com.ticketapp.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;
import com.ticketapp.dto.TicketNotificationEvent;
import org.springframework.stereotype.Service;
import com.lowagie.text.pdf.BaseFont;

import java.awt.*;
import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] createTicketPdf(TicketNotificationEvent event) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 1. PDF Dokümanı Oluştur
            Document document = new Document(PageSize.A5);
            PdfWriter.getInstance(document, baos);
            document.open();

            // 2. Fontlar
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1254", BaseFont.EMBEDDED);
            Font titleFont = new Font(bf, 18, Font.BOLD, Color.BLACK);
            Font labelFont = new Font(bf, 12, Font.BOLD, Color.DARK_GRAY);
            Font valueFont = new Font(bf, 12, Font.NORMAL, Color.BLACK);
            Font footerFont = new Font(bf, 10, Font.ITALIC, Color.GRAY);

            // 3. Başlık
            Paragraph title = new Paragraph("TicketFlow - Dijital Bilet", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 4. Bilet Bilgileri
            addInfo(document, "Etkinlik:", event.getEventTitle(), labelFont, valueFont);
            addInfo(document, "Kullanıcı:", event.getUsername(), labelFont, valueFont);
            addInfo(document, "Bilet No:", "#" + event.getTicketId(), labelFont, valueFont);
            addInfo(document, "Adet:", String.valueOf(event.getQuantity()), labelFont, valueFont);
            addInfo(document, "Toplam Tutar:", event.getTotalPrice() + " TL", labelFont, valueFont);

            // 5. QR Kod Oluştur ve Ekle
            String qrContent = "TICKET_ID:" + event.getTicketId() + "|USER:" + event.getUsername();
            Image qrImage = generateQrCodeImage(qrContent);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrImage.setSpacingBefore(30);
            qrImage.scaleAbsolute(150, 150); // Boyut ayarı
            document.add(qrImage);

            // Alt not
            Paragraph footer = new Paragraph("Kapıda bu QR kodu okutunuz.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(10);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF oluşturulamadı: " + e.getMessage());
        }
    }
    public byte[] createTicketPdf(com.ticketapp.entity.Ticket ticket) {
        TicketNotificationEvent event = new TicketNotificationEvent(
                ticket.getId(),
                ticket.getEvent() != null ? ticket.getEvent().getId() : null,
                ticket.getUsername(),
                ticket.getEvent() != null ? ticket.getEvent().getTitle() : "Etkinlik",
                ticket.getQuantity(),
                ticket.getPrice() != null ? ticket.getPrice().multiply(java.math.BigDecimal.valueOf(ticket.getQuantity())) : java.math.BigDecimal.ZERO,
                "",
                0,
                0
        );
        return createTicketPdf(event);
    }

    // Yardımcı: QR Kod Resmini Üretir
    private Image generateQrCodeImage(String text) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        return Image.getInstance(pngOutputStream.toByteArray());
    }

    // Yardımcı: Satır Ekleme
    private void addInfo(Document doc, String label, String value, Font labFont, Font valFont) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", labFont));
        p.add(new Chunk(value != null ? value : "", valFont));
        p.setSpacingAfter(5);
        doc.add(p);
    }
}