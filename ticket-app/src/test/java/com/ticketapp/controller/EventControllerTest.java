package com.ticketapp.controller;

import com.ticketapp.dto.SalesReport;
import com.ticketapp.entity.Event;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.SeatRepository;
import com.ticketapp.service.EventService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EventController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class EventControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    EventService eventService;
    @MockBean
    private SeatRepository seatRepo;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    com.ticketapp.security.JwtAuthFilter jwtAuthFilter;

    @MockBean
    com.ticketapp.security.JwtService jwtService;


    @Test
    void create_validRequest_returnsEventAnd200() throws Exception {
        Event e = new Event();
        e.setId(1L);
        e.setTitle("Rock Konseri");
        e.setCity("Ankara");
        e.setType("music");
        e.setVenue("Arena");
        e.setDateTime(LocalDateTime.of(2025, 12, 10, 20, 0));
        e.setTotalSeats(100);
        e.setPrice(BigDecimal.valueOf(150));

        Mockito.when(eventService.create(any()))
                .thenReturn(e);

        String body = """
                {
                  "title": "Rock Konseri",
                  "city": "Ankara",
                  "type": "music",
                  "venue": "Arena",
                  "dateTime": "2025-12-10T20:00",
                  "totalSeats": 100,
                  "price": 150.0
                }
                """;

        mvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Rock Konseri"))
                .andExpect(jsonPath("$.city").value("Ankara"));
    }

    @Test
    void create_invalidRequest_titleBlank_returns400WithValidationError() throws Exception {
        Mockito.when(eventService.create(any())).thenAnswer(invocation -> {
            throw new IllegalStateException("Service should not be called when validation fails");
        });

        String body = """
                {
                  "title": "",
                  "city": "Ankara",
                  "type": "music",
                  "venue": "Arena",
                  "dateTime": "2025-12-10T20:00",
                  "totalSeats": 100,
                  "price": 150.0
                }
                """;

        mvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"))
                .andExpect(jsonPath("$.details[0].field").value("title"))
                .andExpect(jsonPath("$.details[0].message").value("Etkinlik başlığı zorunludur"));
    }

    @Test
    void list_returnsEvents() throws Exception {
        Event e = new Event();
        e.setId(1L);
        e.setTitle("Tiyatro");
        e.setCity("Istanbul");
        e.setType("theatre");
        e.setVenue("Sahne 1");
        e.setDateTime(LocalDateTime.of(2025, 11, 1, 19, 30));
        e.setTotalSeats(50);
        e.setPrice(BigDecimal.valueOf(120));

        Mockito.when(eventRepository.findAllByOrderByDateTimeAsc())
                .thenReturn(List.of(e));

        mvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Tiyatro"))
                .andExpect(jsonPath("$[0].city").value("Istanbul"));
    }

    @Test
    void listPaged_returnsPagedEvents() throws Exception {
        Event e = new Event();
        e.setId(1L);
        e.setTitle("Konferans");
        e.setCity("Ankara");
        e.setType("conference");
        e.setVenue("Salon 1");
        e.setDateTime(LocalDateTime.of(2025, 10, 1, 9, 0));
        e.setTotalSeats(200);
        e.setPrice(BigDecimal.valueOf(300));

        Page<Event> page = new PageImpl<>(
                List.of(e),
                PageRequest.of(0, 10),
                1
        );

        Mockito.when(eventService.listPaged(Mockito.any(Pageable.class)))
                .thenReturn(page);

        mvc.perform(get("/api/events/paged")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Konferans"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void search_withCityTypeAndQuery_returnsPagedEvents() throws Exception {
        Event e = new Event();
        e.setId(5L);
        e.setTitle("Rock Festivali");
        e.setCity("Ankara");
        e.setType("music");
        e.setVenue("Açık Hava");
        e.setDateTime(LocalDateTime.of(2025, 8, 15, 21, 0));
        e.setTotalSeats(500);
        e.setPrice(BigDecimal.valueOf(400));

        Page<Event> page = new PageImpl<>(
                List.of(e),
                PageRequest.of(0, 5),
                1
        );

        Mockito.when(eventService.search(
                        eq("Ankara"),
                        eq("music"),
                        eq("rock"),
                        isNull(),
                        isNull(),
                        any(Pageable.class)))
                .thenReturn(page);

        mvc.perform(get("/api/events/search")
                        .param("city", "Ankara")
                        .param("type", "music")
                        .param("q", "rock")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "dateTime")
                        .param("dir", "desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(5L))
                .andExpect(jsonPath("$.content[0].title").value("Rock Festivali"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void sales_returnsSalesReportForEvent() throws Exception {
        // BURASI GÜNCELLENDİ: 11. Parametre Eklendi (50)
        SalesReport report = new SalesReport(
                10L,
                "Rock Gecesi",
                "Istanbul",
                "Stadyum",
                LocalDateTime.of(2025, 9, 1, 20, 0),
                1000,
                600,
                400,
                BigDecimal.valueOf(250),
                BigDecimal.valueOf(150000),
                50 // soldLast24Hours
        );

        Mockito.when(eventService.salesReport(10L))
                .thenReturn(report);

        mvc.perform(get("/api/events/reports/sales")
                        .param("eventId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventId").value(10L))
                .andExpect(jsonPath("$.title").value("Rock Gecesi"))
                .andExpect(jsonPath("$.sold").value(600))
                .andExpect(jsonPath("$.remaining").value(400));
    }
}