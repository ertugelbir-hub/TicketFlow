package com.ticketapp.service;

import com.ticketapp.entity.Seat;
import com.ticketapp.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SeatCleanupService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Her 60 saniyede bir çalışır (60000 ms)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredSeats() {
        LocalDateTime now = LocalDateTime.now();

        // Süresi dolmuş rezervasyonları bul
        List<Seat> expiredSeats = seatRepository.findAllByHoldExpiresAtBefore(now);

        if (!expiredSeats.isEmpty()) {
           /*System.out.println("Temizlik Zamanı: " + expiredSeats.size() + " koltuk boşa düşürülüyor...");*/

            for (Seat seat : expiredSeats) {
                // Eğer koltuk SATILMAMIŞSA ama süresi dolmuşsa serbest bırak
                if (!seat.isSold()) {
                    seat.setHoldExpiresAt(null); // Süreyi sil
                    seatRepository.save(seat);

                    // Frontend'e haber ver: "Bu koltuk artık boş!"
                    messagingTemplate.convertAndSend("/topic/events/" + seat.getEvent().getId() + "/seats", seat);
                }
            }
        }
    }
}