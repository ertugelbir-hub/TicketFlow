package com.ticketapp.repository;

import com.ticketapp.entity.Ticket;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends org.springframework.data.jpa.repository.JpaRepository<Ticket, Long> {

    // Tüm zamanlar için toplam satılan adet (remaining hesaplamakta kullanacağız)
    @Query("select coalesce(sum(t.quantity), 0) from Ticket t where t.event.id = :eventId")
    int sumQuantityByEventId(@Param("eventId") Long eventId);

    // Belirli tarih aralığında satılan adet (rapordaki 'sold')
    @Query("""
           select coalesce(sum(t.quantity), 0)
           from Ticket t
           where t.event.id = :eventId
             and t.createdAt between :from and :to
           """)
    int sumQuantityByEventIdBetweenPurchase(@Param("eventId") Long eventId,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);
    //Kullanıcıya göre biletler
    @Query("select t from Ticket t where t.username = :username")
    List<Ticket> findByUsername(@Param("username") String username);

    // Etkinlik tarihine göre
    @Query("""
         select coalesce(sum(t.quantity),0)
         from Ticket t
         where t.event.id = :eventId
           and t.event.dateTime between :from and :to
         """)
    int sumQuantityByEventIdBetweenEventDate(
            @Param("eventId") Long eventId,
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

}




