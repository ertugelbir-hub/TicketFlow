package com.ticketapp.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTicketInfo(String to, String title, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@ticketapp.com");
            message.setTo(to);
            message.setSubject(title);
            message.setText(body);

            mailSender.send(message);
            /*System.out.println("Mailtrap'e mail gönderildi: " + to);*/
        } catch (Exception e) {
            System.err.println("❌ Mail gönderme hatası: " + e.getMessage());
        }
    }
    public void sendTicketWithPdf(String to, String title, String body, String pdfName, byte[] pdfData) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true = multipart (dosya eki var demek)
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("noreply@ticketapp.com");
            helper.setTo(to);
            helper.setSubject(title);
            helper.setText(body);

            // PDF'i eklenti olarak koy
            helper.addAttachment(pdfName, new ByteArrayResource(pdfData));

            mailSender.send(message);
            /*System.out.println("PDF'li Mail gönderildi: " + to);*/
        } catch (Exception e) {
            System.err.println("❌ PDF Mail gönderme hatası: " + e.getMessage());
        }
    }
}