package com.ticketapp.repository;

import com.ticketapp.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    // Bir etkinliğe ait tüm koltukları getir (Sıralı gelsin ki harita düzgün çıksın)
    List<Seat> findAllByHoldExpiresAtBefore(LocalDateTime now);
    List<Seat> findByEventIdOrderByIdAsc(Long eventId);
}