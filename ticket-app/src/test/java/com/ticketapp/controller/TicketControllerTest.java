package com.ticketapp.controller;

import com.ticketapp.dto.PurchaseRequest;
import com.ticketapp.entity.Event;
import com.ticketapp.entity.Ticket;
import com.ticketapp.service.TicketService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TicketController.class)
@AutoConfigureMockMvc(addFilters = false) // Testte security filtrelerini kapatıyoruz
@ActiveProfiles("test")
class TicketControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    TicketService ticketService;

    @MockBean
    com.ticketapp.repository.SeatRepository seatRepository;

    @MockBean
    org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    // SecurityConfig içindeki bean'ler için mock'lar
    @MockBean
    com.ticketapp.security.JwtAuthFilter jwtAuthFilter;

    @MockBean
    com.ticketapp.security.JwtService jwtService;
    @MockBean
    com.ticketapp.repository.TicketRepository ticketRepository;

    @MockBean
    com.ticketapp.service.PdfService pdfService;

    /**
     * 1) Mutlu senaryo:
     *    POST /api/tickets/purchase
     *    JSON body: { "eventId": 1, "quantity": 2 }
     *    principal.getName() = "ahmet123"
     *    → service.purchase(PurchaseRequest, "ahmet123") çağrılıyor
     *    → 200 ve Ticket JSON dönüyor mu?
     */
    @Test
    void purchase_validRequest_returnsTicketAnd200() throws Exception {
        // GIVEN - sahte Event ve Ticket
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Rock Konseri");
        event.setCity("Ankara");
        event.setType("music");
        event.setVenue("Arena");
        event.setDateTime(LocalDateTime.of(2025, 12, 10, 20, 0));
        event.setTotalSeats(100);
        event.setPrice(BigDecimal.valueOf(150));

        Ticket ticket = new Ticket();
        ticket.setId(10L);
        ticket.setEvent(event);
        ticket.setUsername("ahmet123");
        ticket.setQuantity(2);
        ticket.setCreatedAt(LocalDateTime.of(2025, 11, 1, 18, 0));

        // TicketService.purchase(PurchaseRequest, username) çağrıldığında bu Ticket dönsün
        Mockito.when(ticketService.purchase(any(PurchaseRequest.class), eq("ahmet123")))
                .thenReturn(ticket);

        // Göndereceğimiz JSON request body
        String body = """
                {
                  "eventId": 1,
                  "quantity": 2
                }
                """;

        // Principal'i testte elle veriyoruz
        Principal principal = () -> "ahmet123";

        // WHEN + THEN
        mvc.perform(post("/api/tickets/purchase")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.event.id").value(1L))
                .andExpect(jsonPath("$.username").value("ahmet123"))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    /**
     * 2) Kapasite dolu / iş kuralı hatası:
     *    Service RuntimeException fırlatırsa,
     *    GlobalExceptionHandler devreye girip 400 + "runtime" error dönmeli.
     */
    @Test
    void purchase_capacityExceeded_returns400WithRuntimeError() throws Exception {
        // GIVEN - servis kapasite hatası fırlatıyor
        Mockito.when(ticketService.purchase(any(PurchaseRequest.class), eq("ahmet123")))
                .thenThrow(new RuntimeException("Yeterli koltuk yok"));

        String body = """
                {
                  "eventId": 1,
                  "quantity": 5
                }
                """;

        Principal principal = () -> "ahmet123";

        mvc.perform(post("/api/tickets/purchase")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("runtime"))
                .andExpect(jsonPath("$.message").value("Yeterli koltuk yok"));
    }
}
