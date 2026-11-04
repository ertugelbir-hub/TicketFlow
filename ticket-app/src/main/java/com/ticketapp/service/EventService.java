package com.ticketapp.service;

import com.ticketapp.dto.EventRequest;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.entity.Event;
import com.ticketapp.entity.Seat;
import com.ticketapp.exception.ErrorMessages;
import com.ticketapp.exception.ResourceNotFoundException;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.EventSpecifications;
import com.ticketapp.repository.SeatRepository;
import com.ticketapp.repository.TicketRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class EventService {

    // DÜZELTME: 'String' yanına '[]' ekledik. Artık bu bir dizi.
    private static final String[] DEFAULT_IMAGES = {
            "https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?auto=format&fit=crop&w=800&q=80",
            "https://cdn.bubilet.com.tr/cdn-cgi/image/format=auto,width=828/https://cdn.bubilet.com.tr/files/Blog/resim-adi-37993.jpg",
            "https://blog.biletino.com/wp-content/uploads/2022/03/Konser-nedir-biletino-11245.jpg",
            "https://fs.galataport.com/cdn/uploads/000003402_1.png"
    };

    private final EventRepository repo;
    private final TicketRepository ticketRepo;
    private final SeatRepository seatRepo;

    public EventService(EventRepository repo, TicketRepository ticketRepo, SeatRepository seatRepo) {
        this.repo = repo;
        this.ticketRepo = ticketRepo;
        this.seatRepo = seatRepo;
    }

    /**
     *   RESİM DÜZELTİCİ
     * - Unsplash linkiyse -> Kendi parametrelerini ayarlar.
     * - Başka bir linkse (Google vb.) -> wsrv.nl servisini kullanarak Full HD yapar.
     */
    private String enhanceImageUrl(String url) {
        if (url == null || url.isBlank()) return null;

        // 1. Durum: Unsplash Linki (Kendi parametreleri var, onları kullanalım)
        if (url.contains("images.unsplash.com")) {
            if (url.contains("w=")) url = url.replaceAll("w=\\d+", "w=1920"); else url += "&w=1920";
            if (url.contains("h=")) url = url.replaceAll("h=\\d+", "h=1080"); else url += "&h=1080";
            if (url.contains("q=")) url = url.replaceAll("q=\\d+", "q=90");   else url += "&q=90";
            if (!url.contains("fit=")) url += "&fit=crop";
            return url;
        }

        // 2. Durum: Diğer Herhangi Bir Link (Google, random site vb.)
        // Eğer zaten wsrv.nl linki değilse, onu wsrv.nl içine paketle
        else if (!url.contains("wsrv.nl")) {
            // Mantık şu: https://wsrv.nl/?url=SENIN_RESMIN&w=1920&h=1080&fit=cover&output=webp
            return "https://wsrv.nl/?url=" + url + "&w=1920&h=1080&fit=cover&output=webp&q=85";
        }

        return url;
    }
    @CacheEvict(value = "events", allEntries = true)
    public Event create(EventRequest r) {
        Event e = new Event();
        e.setTitle(r.title);
        e.setCity(r.city);
        e.setType(r.type);
        e.setVenue(r.venue);
        e.setDateTime(r.getDateTime());
        e.setTotalSeats(r.totalSeats);
        e.setPrice(r.price);
        e.setDescription(r.getDescription());
        if (r.getImageUrl() != null && !r.getImageUrl().isBlank()) {
            e.setImageUrl(r.getImageUrl());
        } else {
            int randomIndex = new Random().nextInt(DEFAULT_IMAGES.length);
            e.setImageUrl(DEFAULT_IMAGES[randomIndex]);
        }
        Event savedEvent = repo.save(e);
        generateSeatsForEvent(savedEvent);
        return savedEvent;
    }
    @Cacheable(value = "events")
    public List<Event> list() {
        /*System.out.println("--> Veritabanından çekiliyor...");*/
        return repo.findAll();
    }
    @Caching(evict = {
            @CacheEvict(value = "events", allEntries = true),
            @CacheEvict(value = "sales_reports", key = "#id")
    })
    public Event update(Long id, EventRequest r) {
        Event e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.EVENT_NOT_FOUND));
        e.setTitle(r.getTitle());
        e.setCity(r.getCity());
        e.setType(r.getType());
        e.setVenue(r.getVenue());
        e.setDateTime(r.getDateTime());
        e.setTotalSeats(r.getTotalSeats());
        e.setPrice(r.getPrice());
        e.setDescription(r.getDescription());
        if (r.getImageUrl() != null && !r.getImageUrl().isBlank()) {
            e.setImageUrl(enhanceImageUrl(r.getImageUrl()));
        }
        return repo.save(e);
    }
    @CacheEvict(value = "events", allEntries = true)
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException(ErrorMessages.EVENT_NOT_FOUND);
        }
        repo.deleteById(id);
    }
    @Cacheable(value = "sales_reports", key = "#eventId")
    public SalesReport salesReport(Long eventId) {
        /*System.out.println("--> Rapor veritabanından hesaplanıyor... (Cache Yok)");*/
        Event e = repo.findById(eventId)
                .orElseThrow(() -> new RuntimeException(ErrorMessages.EVENT_NOT_FOUND));

        int sold = ticketRepo.sumQuantityByEventId(e.getId());
        int remaining = e.getTotalSeats() - sold;
        if (remaining < 0) remaining = 0;
        BigDecimal price = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();

        BigDecimal revenue = price.multiply(BigDecimal.valueOf(sold));

        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        int sold24h = ticketRepo.sumQuantityByEventIdBetweenPurchase(e.getId(), yesterday, LocalDateTime.now());

        return new SalesReport(
                e.getId(), e.getTitle(), e.getCity(), e.getVenue(),
                e.getDateTime(), e.getTotalSeats(), sold, remaining, price, revenue, sold24h
        );
    }

    public Page<Event> listPaged(Pageable pageable) {
        return repo.findAll(pageable);
    }

    // Specification kullanarak arama yapıyoruz.
    public Page<Event> search(String city, String type, String q,
                              LocalDateTime from, LocalDateTime to,
                              Pageable pageable) {

        Specification<Event> spec = EventSpecifications.withFilters(city, type, q, from, to);
        return repo.findAll(spec, pageable);
    }

    public List<SalesReport> salesSummary(LocalDateTime from, LocalDateTime to) {
        Specification<Event> spec = EventSpecifications.withFilters(null, null, null, from, to);
        Page<Event> page = repo.findAll(spec, PageRequest.of(0, 1000));

        List<Event> events = page.getContent();
        List<SalesReport> list = new ArrayList<>();

        for (Event e : events) {
            int sold = ticketRepo.sumQuantityByEventId(e.getId());
            int remaining = Math.max(0, e.getTotalSeats() - sold);
            BigDecimal price = e.getPrice() == null ? BigDecimal.ZERO : e.getPrice();
            BigDecimal revenue = price.multiply(BigDecimal.valueOf(sold));

            LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
            int sold24h = ticketRepo.sumQuantityByEventIdBetweenPurchase(e.getId(), yesterday, LocalDateTime.now());

            list.add(new SalesReport(
                    e.getId(), e.getTitle(), e.getCity(), e.getVenue(),
                    e.getDateTime(), e.getTotalSeats(), sold, remaining, price, revenue, sold24h
            ));
        }
        return list;
    }
    //Koltuk üretici
    public void generateSeatsForEvent(Event event) {
        int totalSeats = event.getTotalSeats();
        int seatsPerRow = 10;
        int seatsCreated = 0;
        int rowIndex = 0;
        // Kapasite dolana kadar sıra üret
        while (seatsCreated < totalSeats) {
            // Sıra adını hesaplama: A..Z bittiğinde 2A..2Z, 3A..3Z formatına geçer
            int tier = (rowIndex / 26) + 1;
            char letter = (char) ('A' + (rowIndex % 26));
            String rowName = (tier == 1) ? String.valueOf(letter) : (tier + String.valueOf(letter));

            for (int number = 1; number <= seatsPerRow; number++) {
                if (seatsCreated >= totalSeats) break;

                Seat seat = new Seat(rowName, number, event);
                seatRepo.save(seat);
                seatsCreated++;
            }
            rowIndex++;
        }
    }
}