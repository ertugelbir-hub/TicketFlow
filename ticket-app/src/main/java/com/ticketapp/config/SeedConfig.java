package com.ticketapp.config;

import com.ticketapp.dto.EventRequest;
import com.ticketapp.entity.Event;
import com.ticketapp.entity.Seat;
import com.ticketapp.entity.Ticket;
import com.ticketapp.entity.User;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.SeatRepository;
import com.ticketapp.repository.TicketRepository;
import com.ticketapp.repository.UserRepository;
import com.ticketapp.service.EventService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
@Configuration
public class SeedConfig {
    private final Random random = new Random();

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepo,
                                PasswordEncoder encoder,
                                EventRepository eventRepo,
                                EventService eventService,
                                SeatRepository seatRepo,
                                TicketRepository ticketRepo) {
        return args -> {

            // ADMIN yoksa oluştur
            if (userRepo.findByUsername("admin").isEmpty()) {
                User u = new User();
                u.setUsername("admin");
                u.setPassword(encoder.encode("admin123")); // giriş şifresi
                u.setEmail("admin@example.com");
                u.setRole("ADMIN");                       // kritik: token’da ROLE_ADMIN olsun
                userRepo.save(u);
                /*System.out.println("Seed -> admin/admin123 oluşturuldu.");*/

            }
            // USER yoksa oluştur
            if (userRepo.findByUsername("ayse").isEmpty()) {
                User u1 = new User();
                u1.setUsername("ayse");
                u1.setPassword(encoder.encode("ayse123")); // giriş şifresi
                u1.setEmail("ayse@example.com");
                u1.setRole("USER");                       // kritik: token’da ROLE_USER olsun
                userRepo.save(u1);
                /*System.out.println("Seed -> ayse/ayse123 oluşturuldu.");*/

            }
            // USER2 yoksa oluştur
            if (userRepo.findByUsername("ahmet").isEmpty()) {
                User u2 = new User();
                u2.setUsername("ahmet");
                u2.setPassword(encoder.encode("ahmet123")); // giriş şifresi
                u2.setEmail("ahmet@example.com");
                u2.setRole("USER");                       // kritik: token’da ROLE_USER olsun
                userRepo.save(u2);
                /*System.out.println("Seed -> ahmet/ahmet123 oluşturuldu.");*/

            }
            // ETKİNLİKLER yoksa oluştur
            if (eventRepo.count() == 0) {
                eventService.create(createReq(
                        "Duman Konseri",
                        "Ankara",
                        "IF Performance Hall",
                        "Konser",
                        "Duman canlı sahne performansı",
                        LocalDateTime.now().plusDays(3).withHour(21).withMinute(0),
                        50,
                        new BigDecimal("450.00"),
                        "/default1.jpg"));
                eventService.create(createReq(
                        "Bir Delinin Hatıra Defteri",
                        "İstanbul",
                        "Zorlu PSM",
                        "Tiyatro",
                        "Tek perdelik klasik tiyatro oyunu",
                        LocalDateTime.now().plusDays(6).withHour(19).withMinute(30),
                        50,
                        new BigDecimal("250.00"),
                        "/default2.jpg"));
                eventService.create(createReq(
                        "Stand-Up Gecesi",
                        "İzmir",
                        "Bostanlı Suat Taşer",
                        "Stand-up",
                        "Açık mikrofon ve komedi gösterisi",
                        LocalDateTime.now().plusDays(8).withHour(20).withMinute(30),
                        50,
                        new BigDecimal("300.00"),
                        "/default3.jpg"));
                eventService.create(createReq(
                        "Mor ve Ötesi",
                        "Ankara",
                        "Jolly Joker",
                        "Konser",
                        "Sevilen şarkıları ve yeni albümü",
                        LocalDateTime.now().plusDays(12).withHour(21).withMinute(0),
                        50,
                        new BigDecimal("500.00"),
                        "/default4.jpg"));
                eventService.create(createReq("Cimri - Moliere",
                        "Ankara",
                        "Şinasi Sahnesi",
                        "Tiyatro",
                        "Moliere'in ünlü tiyatro eseri",
                        LocalDateTime.now().plusDays(15).withHour(20).withMinute(0),
                        50,
                        new BigDecimal("200.00"),
                        "/default5.jpg"));
                eventService.create(createReq(
                        "Doğu Demirkol Stand-Up",
                        "İstanbul",
                        "BKM Beşiktaş",
                        "Stand-up",
                        "Doğu Demirkol tek kişilik gösteri",
                        LocalDateTime.now().plusDays(18).withHour(20).withMinute(0),
                        50,
                        new BigDecimal("400.00"),
                        "/default1.jpg"));
                eventService.create(createReq(
                        "Hayko Cepkin Konseri",
                        "Ankara",
                        "ODTÜ Vişnelik",
                        "Konser",
                        "Hayko Cepkin açık hava konseri",
                        LocalDateTime.now().plusDays(18).withHour(20).withMinute(0),
                        800,
                        new BigDecimal("600.00"),
                        "/default1.jpg"));

                // RASTGELE BİLET VE KOLTUK DAĞITIMI
                List<Event> allEvents = eventRepo.findAll();
                List<String> mockUsers = List.of("ayse", "ahmet");

                for (Event event : allEvents) {
                    // 500'den büyükse %20 gişe satışı yap
                    if (event.getTotalSeats() > 500) {
                        assignBoxOfficeSeats(event, seatRepo, ticketRepo);
                    } else {
                        // 500 ve altıysa mevcut Ayşe ve Ahmet bilet dağıtımı aynen çalışsın
                        int purchaseCount = 2 + random.nextInt(3);
                        for (int i = 0; i < purchaseCount; i++) {
                            String randomUser = mockUsers.get(random.nextInt(mockUsers.size()));
                            int randomSeatCount = 1 + random.nextInt(5);
                            assignRandomTicket(event, randomUser, randomSeatCount, seatRepo, ticketRepo);
                        }
                    }
                }
            }

        };
    }
    private EventRequest createReq(String title, String city, String venue, String type,
                                   String desc, LocalDateTime dateTime, int seats,
                                   BigDecimal price, String img) {
        EventRequest req = new EventRequest();
        req.setTitle(title);
        req.setCity(city);
        req.setVenue(venue);
        req.setType(type);
        req.setDescription(desc);
        req.setDateTime(dateTime);
        req.setTotalSeats(seats);
        req.setPrice(price);
        req.setImageUrl(img);
        return req;
    }
    // Rastgele Konumlardan Koltuk Seçip Bilet Oluşturan Yardımcı Metot
    private void assignRandomTicket(Event event, String username, int count,
                                    SeatRepository seatRepo, TicketRepository ticketRepo) {
        // Etkinliğin boş koltuklarını bul
        List<Seat> emptySeats = new ArrayList<>(
                seatRepo.findByEventIdOrderByIdAsc(event.getId()).stream()
                        .filter(s -> !s.isSold())
                        .toList()
        );

        if (emptySeats.isEmpty()) return;

        // Koltukları rastgele karıştır
        Collections.shuffle(emptySeats, random);

        int takeCount = Math.min(count, emptySeats.size());
        List<Seat> selectedSeats = emptySeats.subList(0, takeCount);

        // Seçilen koltukları dolu yap
        selectedSeats.forEach(s -> s.setSold(true));
        seatRepo.saveAll(selectedSeats);

        // Bileti oluştur (Son 1-36 saat içinde alınmış gibi gerçekçi tarih ver)
        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setUsername(username);
        ticket.setQuantity(takeCount);
        ticket.setPrice(event.getPrice());
        ticket.setCreatedAt(LocalDateTime.now().minusHours(1 + random.nextInt(36)));
        ticketRepo.save(ticket);
    }
    // 500'den büyük etkinliklerde %20 koltuğu gişe satışı olarak kapatan metot
    private void assignBoxOfficeSeats(Event event, SeatRepository seatRepo, TicketRepository ticketRepo) {
        List<Seat> emptySeats = new ArrayList<>(
                seatRepo.findByEventIdOrderByIdAsc(event.getId()).stream()
                        .filter(s -> !s.isSold())
                        .toList()
        );

        if (emptySeats.isEmpty()) return;

        Collections.shuffle(emptySeats, random);

        // Kapasitenin tam %20'si (800 koltukta 160 koltuk seçilir)
        int targetSeats = (int) Math.round(event.getTotalSeats() * 0.20);
        targetSeats = Math.min(targetSeats, emptySeats.size());
        if (targetSeats <= 0) return;

        // Seçilen koltukları dolu yap ve tek seferde kaydet
        List<Seat> seatsToSell = emptySeats.subList(0, targetSeats);
        seatsToSell.forEach(s -> s.setSold(true));
        seatRepo.saveAll(seatsToSell);

        // Gişe satışı olarak tek bilet kaydet (Ayşe/Ahmet'in ekranına düşmez, admin raporları tam tutar)
        Ticket ticket = new Ticket();
        ticket.setEvent(event);
        ticket.setUsername("gise_satisi");
        ticket.setQuantity(targetSeats);
        ticket.setPrice(event.getPrice());
        ticket.setCreatedAt(LocalDateTime.now().minusHours(12));
        ticketRepo.save(ticket);
    }
}