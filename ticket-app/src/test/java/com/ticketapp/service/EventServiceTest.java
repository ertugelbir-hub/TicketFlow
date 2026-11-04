package com.ticketapp.service;

import com.ticketapp.dto.EventRequest;
import com.ticketapp.entity.Event;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.SeatRepository;
import com.ticketapp.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private SeatRepository seatRepository;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, ticketRepository, seatRepository);
    }

    @Test
    void create_shouldSaveEvent() {
        // Hazırlık
        EventRequest req = new EventRequest();
        req.title = "Test Event";
        req.city = "Istanbul";
        req.type = "Concert";
        req.venue = "Venue";
        req.setDateTime(LocalDateTime.now().plusDays(1));
        req.totalSeats = 100;
        req.price = BigDecimal.TEN;

        Event eventToSave = new Event();
        eventToSave.setId(1L);
        eventToSave.setTitle("Test Event");
        eventToSave.setTotalSeats(100);

        // Mock davranışı
        when(eventRepository.save(any(Event.class))).thenReturn(eventToSave);

        // Çalıştırma
        Event result = eventService.create(req);

        // Doğrulama
        assertNotNull(result);
        assertEquals("Test Event", result.getTitle());
        verify(eventRepository).save(any(Event.class));
        verify(seatRepository, times(100)).save(any());
    }

    @Test
    void list_shouldReturnAll() {
        when(eventRepository.findAll()).thenReturn(List.of(new Event()));

        List<Event> result = eventService.list();

        assertEquals(1, result.size());
        verify(eventRepository).findAll();
    }

    @Test
    void listPaged_delegatesToRepository() {
        Page<Event> page = new PageImpl<>(List.of(new Event()));
        Pageable pageable = PageRequest.of(0, 10);

        when(eventRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Event> result = eventService.listPaged(pageable);

        assertEquals(1, result.getTotalElements());
        verify(eventRepository).findAll(pageable);
    }

    @Test
    void search_delegatesToRepository() {
        // Hazırlık
        Page<Event> page = new PageImpl<>(List.of(new Event()));
        Pageable pageable = PageRequest.of(0, 10);

        // DÜZELTME BURADA YAPILDI:
        // Eskiden: eventRepository.search(...) çağrılıyordu.
        // Şimdi: eventRepository.findAll(Specification, Pageable) çağrılıyor.
        // Bu yüzden testi buna göre güncelledik.

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // Çalıştırma
        Page<Event> result = eventService.search("Ankara", "music", "konser", null, null, pageable);

        // Doğrulama
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        // Repository'nin findAll metodunun doğru parametrelerle çağrıldığını kontrol et
        verify(eventRepository).findAll(any(Specification.class), eq(pageable));
    }
}