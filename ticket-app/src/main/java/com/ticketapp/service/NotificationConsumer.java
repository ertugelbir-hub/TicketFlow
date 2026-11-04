package com.ticketapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketapp.dto.TicketNotificationEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket için
    private final EmailService emailService;               // Mail için
    private final PdfService pdfService;

    public NotificationConsumer(ObjectMapper objectMapper,
                                SimpMessagingTemplate messagingTemplate,
                                EmailService emailService,PdfService pdfService) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
        this.pdfService = pdfService;
    }

    @KafkaListener(topics = "notification-topic", groupId = "ticket-notification-group")
    public void listen(String message) {
        try {
            // 1. Gelen JSON'ı Java Nesnesine çevir
            TicketNotificationEvent event = objectMapper.readValue(message, TicketNotificationEvent.class);

           /* System.out.println("=========================================");
            System.out.println("KAFKA RAPORU GELDİ");
            System.out.println("-----------------------------------------");
            System.out.println("Alan Kişi : " + event.getUsername());
            System.out.println("Etkinlik  : " + event.getEventTitle());
            System.out.println("Adet      : " + event.getQuantity());
            System.out.println("Tutar     : " + event.getTotalPrice() + " ₺");
            System.out.println("Kalan Stok: " + event.getRemainingSeats());
            System.out.println("Son 24s   : " + event.getSoldLast24Hours() + " bilet satıldı!");
            System.out.println("=========================================");
*/
            // GÖREV 2: WEBSOCKET İLE CANLI YAYIN 📡
            // Frontend'e "Stok güncellendi" haberi uçur
            messagingTemplate.convertAndSend("/topic/sales", event);
            // 3. PDF OLUŞTURMA VE GÖNDERME

            // A) PDF'i oluştur (Byte dizisi olarak döner)
            byte[] pdfBytes = pdfService.createTicketPdf(event);

            // B) Mail içeriğini hazırla
            String mailBaslik = "Biletiniz Hazır! 🎟️ - " + event.getEventTitle();
            String mailIcerigi = "Merhaba " + event.getUsername() + ",\n\n" +
                    "Satın alma işleminiz başarıyla gerçekleşti.\n" +
                    "Dijital biletiniz EKTE yer almaktadır.\n\n" +
                    "İyi eğlenceler!\nTicketApp Ekibi";

            // C) Maili PDF ekiyle gönder
            // Eğer kullanıcının maili yoksa test maili kullan
            String emailTo = (event.getEmail() != null && !event.getEmail().isEmpty()) ? event.getEmail() : "test@example.com";

            emailService.sendTicketWithPdf(emailTo, mailBaslik, mailIcerigi, "bilet.pdf", pdfBytes);
        } catch (Exception e) {
            System.err.println("❌ Mesaj işleme hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
}