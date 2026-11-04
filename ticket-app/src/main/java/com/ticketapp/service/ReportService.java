// src/main/java/com/ticketapp/service/ReportService.java
package com.ticketapp.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.ticketapp.dto.FullSalesReport;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.dto.SalesSummaryItem;
import com.ticketapp.entity.Event;
import com.ticketapp.exception.ErrorMessages;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.TicketRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


@Service
public class ReportService {
    // TL formatlayıcı
    private static final DecimalFormatSymbols TR_SYM = new DecimalFormatSymbols(new Locale("tr", "TR"));
    private static final DecimalFormat TL = new DecimalFormat("#,##0.00", TR_SYM);

    private final EventRepository eventRepo;
    private final TicketRepository ticketRepo;

    public ReportService(EventRepository eventRepo, TicketRepository ticketRepo) {
        this.eventRepo = eventRepo;
        this.ticketRepo = ticketRepo;
    }

    // A) Purchase time

    public List<SalesSummaryItem> summaryByPurchase(LocalDateTime from, LocalDateTime to) {
        return eventRepo.findAll().stream().map(e -> {
            int soldInRange = ticketRepo.sumQuantityByEventIdBetweenPurchase(e.getId(), from, to);
            int allTimeSold = ticketRepo.sumQuantityByEventId(e.getId());
            int remaining = Math.max(0, e.getTotalSeats() - allTimeSold);

            var price = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
            var revenue = price.multiply(BigDecimal.valueOf(soldInRange));

            return new SalesSummaryItem(
                    e.getId(), e.getTitle(), e.getTotalSeats(),
                    soldInRange, remaining, price, revenue
            );
        }).toList();
    }

    // B) Event date
    public List<SalesSummaryItem> summaryByEvent(LocalDateTime from, LocalDateTime to) {
        return eventRepo.findAll().stream().map(e -> {
            int soldInRange = ticketRepo.sumQuantityByEventIdBetweenEventDate(e.getId(), from, to);
            int allTimeSold = ticketRepo.sumQuantityByEventId(e.getId());
            int remaining = Math.max(0, e.getTotalSeats() - allTimeSold);

            var price = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
            var revenue = price.multiply(BigDecimal.valueOf(soldInRange));

            return new SalesSummaryItem(
                    e.getId(), e.getTitle(), e.getTotalSeats(),
                    soldInRange, remaining, price, revenue
            );
        }).toList();
    }

    // C) All time
    @Cacheable(value = "sales_reports", key = "#eventId")
    public SalesReport allTimeSales(Long eventId) {
        /*System.out.println("--> (ReportService) Rapor veritabanından hesaplanıyor... ");*/
        var e = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.EVENT_NOT_FOUND));

        int soldAllTime = ticketRepo.sumQuantityByEventId(e.getId());
        int remaining = Math.max(0, e.getTotalSeats() - soldAllTime);

        var price = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
        var revenue = price.multiply(BigDecimal.valueOf(soldAllTime));

        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        int sold24h = ticketRepo.sumQuantityByEventIdBetweenPurchase(e.getId(), yesterday, LocalDateTime.now());

        return new SalesReport(
                e.getId(), e.getTitle(), e.getCity(), e.getVenue(),
                e.getDateTime(), e.getTotalSeats(),
                soldAllTime, remaining, price, revenue, sold24h
        );


    }

    public List<FullSalesReport> full(LocalDateTime from, LocalDateTime to) {
        // Güvenlik: from > to geldiyse takas et
        LocalDateTime f = from;
        LocalDateTime t = to;
        if (f != null && t != null && f.isAfter(t)) {
            LocalDateTime tmp = f;
            f = t;
            t = tmp;
        }
        // 2) tüm etkinlikleri çek
        List<Event> events = eventRepo.findAll();
        List<FullSalesReport> out = new ArrayList<>(events.size());
        // 3) her etkinlik için metrikleri hesapla
        for (Event e : events) {
            Long eventId = e.getId();

            // all-time satış
            int soldAllTime = ticketRepo.sumQuantityByEventId(eventId);
            if (soldAllTime < 0) soldAllTime = 0;

            // tarih aralığı satışı (from/to ikisi de verilmişse)
            int soldInRange = 0;
            if (f != null && t != null) {
                soldInRange = ticketRepo.sumQuantityByEventIdBetweenPurchase(eventId, f, t);
                if (soldInRange < 0) soldInRange = 0;
            }
            // kalan koltuk
            int totalSeats = e.getTotalSeats();
            int remaining = totalSeats - soldAllTime;
            if (remaining < 0) remaining = 0;

            // fiyat ve gelirler
            BigDecimal price = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
            BigDecimal revenueRange = price.multiply(BigDecimal.valueOf(soldInRange));
            BigDecimal revenueAllTime = price.multiply(BigDecimal.valueOf(soldAllTime));
            // DTO
            out.add(new FullSalesReport(
                    eventId,
                    e.getTitle(),
                    totalSeats,
                    soldInRange,
                    soldAllTime,
                    remaining,
                    price,
                    revenueRange,
                    revenueAllTime
            ));
        }

        return out;
    }


    public byte[] fullCsv(LocalDateTime from, LocalDateTime to) {
        // 1) Veriyi hazırla
        List<FullSalesReport> list = full(from, to);

        StringBuilder sb = new StringBuilder();
        // Başlık satırıS
        sb.append("eventId,title,totalSeats,soldInRange,soldAllTime,remaining,price,revenueRange,revenueAllTime\n");
        // 3) Satırlar
        for (FullSalesReport r : list) {
            // başlıkta/isimde virgül vb. varsa Excel için tırnakla ve iç tırnakları kaçır
            String title = r.title == null ? "" : r.title.replace("\"", "\"\"");
            String price = r.price == null ? "0" : r.price.toPlainString();
            String revenueRange = r.revenueRange == null ? "0" : r.revenueRange.toPlainString();
            String revenueAllTime = r.revenueAllTime == null ? "0" : r.revenueAllTime.toPlainString();

            sb.append(r.eventId).append(",");
            // CSV güvenliği: başlıkta virgül olursa tırnakla
            sb.append("\"").append(r.title == null ? "" : r.title.replace("\"", "\"\"")).append("\",");
            sb.append(r.totalSeats).append(",");
            sb.append(r.soldInRange).append(",");
            sb.append(r.soldAllTime).append(",");
            sb.append(r.remaining).append(",");
            sb.append(TL.format(r.price == null ? BigDecimal.ZERO : r.price)).append(",");
            sb.append(TL.format(r.revenueRange == null ? BigDecimal.ZERO : r.revenueRange)).append(",");
            sb.append(TL.format(r.revenueAllTime == null ? BigDecimal.ZERO : r.revenueAllTime)).append("\n");
        }

        // 4) UTF-8 BOM ile byte dizisi hazırla (Excel Türkçe karakterleri sorunsuz görür)
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream out = new ByteArrayOutputStream(bom.length + body.length);
        out.write(bom, 0, bom.length);
        out.write(body, 0, body.length);
        return out.toByteArray();

    }


    public String fullCsvFilename(LocalDateTime from, LocalDateTime to) {
        DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
        if (from != null && to != null) {
            // from > to takasını full(...) zaten yapıyor; burada isim için min/max alabiliriz
            LocalDateTime a = from.isAfter(to) ? to : from;
            LocalDateTime b = from.isAfter(to) ? from : to;
            return "full-sales-report_" + F.format(a) + "_to_" + F.format(b) + ".csv";
        }
        return "full-sales-report.csv";
    }
    //SAYFALI FULL RAPOR

    public Page<FullSalesReport> fullPaged(LocalDateTime from, LocalDateTime to,
                                           int page, int size, String sort, String dir) {
        // 1) Sıralama ve sayfalama ayarları
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

        // 2) Event'leri sayfalı çek
        var eventPage = eventRepo.findAll(pageable);

        // 3) from>to ise takas (null'lar sorun değil)
        LocalDateTime f = from, t = to;
        if (f != null && t != null && f.isAfter(t)) {
            LocalDateTime tmp = f;
            f = t;
            t = tmp;
        }

        // 4) Her event için metrikleri hesapla (full(...) ile aynı mantık)
        List<FullSalesReport> content = new ArrayList<>(eventPage.getContent().size());
        for (Event e : eventPage.getContent()) {
            Long eventId = e.getId();

            int soldAllTime = ticketRepo.sumQuantityByEventId(eventId);
            if (soldAllTime < 0) soldAllTime = 0;

            int soldInRange = 0;
            if (f != null && t != null) {
                soldInRange = ticketRepo.sumQuantityByEventIdBetweenPurchase(eventId, f, t);
                if (soldInRange < 0) soldInRange = 0;
            }

            int totalSeats = e.getTotalSeats();
            int remaining = Math.max(0, totalSeats - soldAllTime);

            BigDecimal price = (e.getPrice() == null) ? BigDecimal.ZERO : e.getPrice();
            BigDecimal revenueRange = price.multiply(BigDecimal.valueOf(soldInRange));
            BigDecimal revenueAllTime = price.multiply(BigDecimal.valueOf(soldAllTime));

            content.add(new FullSalesReport(
                    eventId, e.getTitle(), totalSeats,
                    soldInRange, soldAllTime, remaining,
                    price, revenueRange, revenueAllTime
            ));
        }

        // 5) Page metadata ile dön
        return new PageImpl<>(content, pageable, eventPage.getTotalElements());
    }

    // PDF dosya adı
    public String fullPdfFilename(LocalDateTime from, LocalDateTime to) {
        DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
        if (from != null && to != null) {
            LocalDateTime a = from.isAfter(to) ? to : from;
            LocalDateTime b = from.isAfter(to) ? from : to;
            return "full-sales-report_" + F.format(a) + "_to_" + F.format(b) + ".pdf";
        }
        return "full-sales-report.pdf";
    }



    /** Full raporu PDF olarak üretir  */
    public byte[] fullPdf(LocalDateTime from, LocalDateTime to) {
        List<FullSalesReport> list = full(from, to);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate(), 36, 36, 28, 28);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Türkçe karakterler için CP1254 kodlamalı standart font
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, "Cp1254", BaseFont.EMBEDDED);
            Font h1 = new Font(bf, 16, Font.BOLD);
            Font h2 = new Font(bf, 10, Font.NORMAL, new GrayColor(0.20f));
            Font th = new Font(bf, 11, Font.BOLD);
            Font td = new Font(bf, 10, Font.NORMAL);

            // --- Başlık ve tarih aralığı ---
            Paragraph title = new Paragraph("Birleşik Satış Raporu", h1);
            title.setAlignment(Element.ALIGN_LEFT);
            title.setSpacingAfter(4f);
            doc.add(title);

            Paragraph sub = new Paragraph(dateRangeLine(from, to), h2);
            sub.setSpacingAfter(10f);
            doc.add(sub);

            // Tablo (9 sütun)
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{8f, 28f, 9f, 12f, 12f, 10f, 10f, 12f, 12f});

            // Header hücreleri, Başlıklar
            addHeader(table, "ID", th, Element.ALIGN_CENTER);
            addHeader(table, "Etkinlik Adı", th, Element.ALIGN_LEFT);
            addHeader(table, "Toplam", th, Element.ALIGN_RIGHT);
            addHeader(table, "Aralık Satış", th, Element.ALIGN_RIGHT);
            addHeader(table, "Tüm Zaman", th, Element.ALIGN_RIGHT);
            addHeader(table, "Kalan", th, Element.ALIGN_RIGHT);
            addHeader(table, "Fiyat (₺)", th, Element.ALIGN_RIGHT);
            addHeader(table, "Aralık Geliri (₺)", th, Element.ALIGN_RIGHT);
            addHeader(table, "Toplam Gelir (₺)", th, Element.ALIGN_RIGHT);

            // Satırlar, Toplamlar
            int totSoldRange = 0, totSoldAll = 0, totRemaining = 0, totSeats = 0;
            BigDecimal totRevRange = BigDecimal.ZERO, totRevAll = BigDecimal.ZERO;
            boolean zebra = false;
            for (FullSalesReport r : list) {
                zebra = !zebra;
                GrayColor rowBg = zebra ? new GrayColor(0.96f) : GrayColor.GRAYWHITE;

                totSoldRange += r.soldInRange;
                totSoldAll += r.soldAllTime;
                totRemaining += r.remaining;
                totSeats += r.totalSeats;
                totRevRange = totRevRange.add(nvl(r.revenueRange));
                totRevAll = totRevAll.add(nvl(r.revenueAllTime));

                addCell(table, String.valueOf(r.eventId), td, Element.ALIGN_CENTER, rowBg);
                addCell(table, text(r.title), td, Element.ALIGN_LEFT, rowBg);
                addCell(table, String.valueOf(r.totalSeats), td, Element.ALIGN_RIGHT, rowBg);
                addCell(table, String.valueOf(r.soldInRange), td, Element.ALIGN_RIGHT, rowBg);
                addCell(table, String.valueOf(r.soldAllTime), td, Element.ALIGN_RIGHT, rowBg);
                addCell(table, String.valueOf(r.remaining), td, Element.ALIGN_RIGHT, rowBg);
                addCell(table, TL.format(nvl(r.price)), td, Element.ALIGN_RIGHT, rowBg);
                addCell(table, TL.format(nvl(r.revenueRange)), td, Element.ALIGN_RIGHT, rowBg);
                addCell(table, TL.format(nvl(r.revenueAllTime)), td, Element.ALIGN_RIGHT, rowBg);
            }
        //  TOPLAM SATIRI
            Font totalFont = new Font(bf, 11, Font.BOLD);
            GrayColor totalBg = new GrayColor(0.90f);
            addSpanCell(table, "TOPLAM", totalFont, Element.ALIGN_RIGHT, totalBg, 2);
            addCell(table, String.valueOf(totSeats), totalFont, Element.ALIGN_RIGHT, totalBg);
            addCell(table, String.valueOf(totSoldRange), totalFont, Element.ALIGN_RIGHT, totalBg);
            addCell(table, String.valueOf(totSoldAll), totalFont, Element.ALIGN_RIGHT, totalBg);
            addCell(table, String.valueOf(totRemaining), totalFont, Element.ALIGN_RIGHT, totalBg);
            addCell(table, "", totalFont, Element.ALIGN_RIGHT, totalBg);
            addCell(table, TL.format(totRevRange), totalFont, Element.ALIGN_RIGHT, totalBg);
            addCell(table, TL.format(totRevAll), totalFont, Element.ALIGN_RIGHT, totalBg);

            doc.add(table);
            doc.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("PDF oluşturulamadı", ex);
        }
    }

    //yardımcılar
    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String text(String s) {
        return s == null ? "" : s;
    }

    private String dateRangeLine(LocalDateTime from, LocalDateTime to) {
        DateTimeFormatter F = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        if (from != null && to != null) {
            LocalDateTime a = from.isAfter(to) ? to : from;
            LocalDateTime b = from.isAfter(to) ? from : to;
            return "Tarih aralığı: " + F.format(a) + "  –  " + F.format(b);
        }
        return "Tarih aralığı: (tüm kayıtlar)";
    }

    private void addHeader(PdfPTable t, String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Paragraph(text, font));
        c.setHorizontalAlignment(align);
        c.setPadding(6f);
        c.setBackgroundColor(new GrayColor(0.90f));
        c.setBorder(Rectangle.BOTTOM);
        t.addCell(c);
    }

    private void addCell(PdfPTable t, String text, Font font, int align, GrayColor bg) {
        PdfPCell c = new PdfPCell(new Paragraph(text, font));
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(5f);
        c.setBackgroundColor(bg);
        t.addCell(c);
    }

    private void addSpanCell(PdfPTable t, String text, Font font, int align, GrayColor bg, int colspan) {
        PdfPCell c = new PdfPCell(new Paragraph(text, font));
        c.setColspan(colspan);
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(6f);
        c.setBackgroundColor(bg);
        t.addCell(c);
    }
}

