package com.ticketapp.repository;

import com.ticketapp.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    // 1. Yeniye Göre (Etkinlik tarihine göre en yakın olanları getirir)
    List<Event> findAllByOrderByDateTimeAsc();

    // 2. Çok Satanlara Göre (Koltuk tablosuyla birleştirip, satılan koltuk sayısına göre çoktan aza sıralar)
    @Query("SELECT e FROM Event e LEFT JOIN Seat s ON s.event.id = e.id AND s.isSold = true GROUP BY e.id ORDER BY COUNT(s.id) DESC")
    List<Event> findAllByOrderBySoldTicketsDesc();
}