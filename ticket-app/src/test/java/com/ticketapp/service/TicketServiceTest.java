package com.ticketapp.service;

import com.ticketapp.dto.PurchaseRequest;
import com.ticketapp.entity.Event;
import com.ticketapp.entity.Seat;
import com.ticketapp.entity.Ticket;
import com.ticketapp.entity.User;
import com.ticketapp.exception.ResourceNotFoundException;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.SeatRepository;
import com.ticketapp.repository.TicketRepository;
import com.ticketapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    TicketRepository ticketRepository;

    @Mock
    EventRepository eventRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    TicketService ticketService;

    @Mock
    NotificationProducer notificationProducer;

    @Mock
    SeatRepository seatRepository;

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @Test
    void purchase_whenCapacityEnough_savesTicketAndReturns() {
        // GIVEN
        PurchaseRequest request = new PurchaseRequest();
        request.eventId = 1L;
        request.quantity = 2;
        request.seatIds = List.of(10L, 11L);

        // ✅ Kullanıcıyı da mock'la
        User user = new User();
        user.setId(1L);
        user.setUsername("ahmet123");
        user.setPassword("secret");         // ne olduğunun önemi yok
        when(userRepository.findByUsername("ahmet123"))
                .thenReturn(Optional.of(user));

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Konser");
        event.setCity("Ankara");
        event.setType("music");
        event.setVenue("Arena");
        event.setDateTime(LocalDateTime.of(2025, 12, 10, 20, 0));
        event.setTotalSeats(100);
        event.setPrice(BigDecimal.valueOf(200));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(ticketRepository.sumQuantityByEventId(1L)).thenReturn(10);
        Seat s1 = new Seat("A", 1, event);
        Seat s2 = new Seat("A", 2, event);
        when(seatRepository.findAllById(request.seatIds)).thenReturn(List.of(s1, s2));

        // Kaydedilen ticket'a id verelim
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        // WHEN
        Ticket result = ticketService.purchase(request, "ahmet123");

        // THEN
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, times(1)).save(captor.capture());
        Ticket saved = captor.getValue();

        assertEquals(event, saved.getEvent());
        assertEquals("ahmet123", saved.getUsername());
        assertEquals(2, saved.getQuantity());
        assertNotNull(saved.getCreatedAt());
        assertEquals(100L, result.getId());
    }

    @Test
    void purchase_whenCapacityNotEnough_throwsExceptionAndDoesNotSave() {
        // GIVEN
        PurchaseRequest request = new PurchaseRequest();
        request.eventId = 1L;
        request.quantity = 5;
        request.seatIds = List.of(10L, 11L);


        User user = new User();
        user.setId(1L);
        user.setUsername("ahmet123");
        user.setPassword("secret");
        when(userRepository.findByUsername("ahmet123"))
                .thenReturn(Optional.of(user));

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Konser");
        event.setTotalSeats(10);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        Seat s1 = new Seat("A", 1, event);
        s1.setSold(true); // Koltuk dolu!
        Seat s2 = new Seat("A", 2, event);
        when(seatRepository.findAllById(request.seatIds)).thenReturn(List.of(s1, s2));

        // WHEN
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ticketService.purchase(request, "ahmet123"));

        // THEN
        assertTrue(ex.getMessage().contains("maalesef dolu"));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }
    /**
     * quantity <= 0 ise servis hemen IllegalArgumentException fırlatmalı
     * ve hiçbir repository ile konuşmamalı.
     */
    @Test
    void purchase_whenQuantityZero_throwsRuntimeException() {
        // GIVEN
        PurchaseRequest request = new PurchaseRequest();
        request.eventId = 1L;
        request.quantity = 0; // geçersiz adet

        // WHEN
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> ticketService.purchase(request, "ahmet123")
        );

        // THEN
        assertEquals("Adet 1 veya daha fazla olmalı", ex.getMessage());

        // Hiç repository çağrılmamalı
        verifyNoInteractions(userRepository, eventRepository, ticketRepository);
    }



    /**
     * Event bulunamazsa ResourceNotFoundException fırlatılmalı
     * ve ticket kaydı yapılmamalı.
     */
    @Test
    void purchase_whenEventNotFound_throwsResourceNotFoundException() {
        // GIVEN
        PurchaseRequest request = new PurchaseRequest();
        request.eventId = 999L; // olmayan id
        request.quantity = 1;

        User user = new User();
        user.setId(1L);
        user.setUsername("ahmet123");

        when(userRepository.findByUsername("ahmet123"))
                .thenReturn(java.util.Optional.of(user));

        when(eventRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        // WHEN
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.purchase(request, "ahmet123")
        );

        // THEN
        assertEquals("Etkinlik bulunamadı", ex.getMessage());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }


    @Test
    void purchase_whenQuantityInvalid_throwsRuntimeException() {
        // GIVEN
        PurchaseRequest request = new PurchaseRequest();
        request.eventId = 1L;
        request.quantity = 0;   // veya -5 gibi

        // WHEN
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> ticketService.purchase(request, "ahmet123")
        );

        // THEN
        assertEquals("Adet 1 veya daha fazla olmalı", ex.getMessage());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }



}
