package com.ticketapp.controller;

import com.ticketapp.dto.PurchaseRequest;
import com.ticketapp.entity.Seat;
import com.ticketapp.entity.Ticket;
import com.ticketapp.repository.SeatRepository;
import com.ticketapp.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static com.ticketapp.config.SwaggerExamples.ERROR_RES;
import static com.ticketapp.config.SwaggerExamples.TICKET_PURCHASE_REQ;

@Tag(name = "Tickets", description = "Bilet satın alma, iptal etme ve kendi biletlerini görme")
@RestController
@RequestMapping("/api/tickets")

public class TicketController {
    private final TicketService service;
    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private com.ticketapp.repository.TicketRepository ticketRepository;

    @Autowired
    private com.ticketapp.service.PdfService pdfService;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @Operation(summary = "Bilet satın al (USER)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Satın alım başarılı",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{ \"status\":\"OK\" }")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Validasyon",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ERROR_RES)
                    )
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name="Purchase body", value = TICKET_PURCHASE_REQ)
            )
    )
    @PostMapping("/purchase")
    public ResponseEntity<Ticket> purchase(@RequestBody @Valid PurchaseRequest req,
                                           Principal principal) {
        // principal.getName() = token’daki username
        Ticket ticket = service.purchase(req, principal.getName());
        return ResponseEntity.ok(ticket);
    }
    @GetMapping
    public ResponseEntity<List<Ticket>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<Ticket>> listByUsername(@PathVariable String username) {
        return ResponseEntity.ok(service.listByUsername(username));
    }
    @Operation(
            summary = "Bilet iptal et",
            description = "Verilen ID'ye ait bileti iptal eder. Yalnızca biletin sahibi veya admin iptal edebilir."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Bilet başarıyla iptal edildi"),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama başarısız"),
            @ApiResponse(responseCode = "403", description = "Bu bileti iptal etmeye yetkiniz yok"),
            @ApiResponse(responseCode = "404", description = "Verilen ID ile bilet bulunamadı")
    })
    
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<?> cancel(@PathVariable Long ticketId, Principal principal) {
        service.cancel(ticketId, principal.getName());
        return ResponseEntity.ok("İptal edildi");
    }
    @GetMapping("/{ticketId}/pdf")
    public ResponseEntity<byte[]> downloadTicketPdf(
            @PathVariable Long ticketId,
            Principal principal,
            org.springframework.security.core.Authentication authentication) {

        // 1. Kullanıcı Admin mi kontrol et
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        // 2. Bileti bul
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new com.ticketapp.exception.ResourceNotFoundException(com.ticketapp.exception.ErrorMessages.TICKET_NOT_FOUND));

        // 3. Admin değilse ve bilet başkasınınsa erişimi engelle (403)
        if (!isAdmin && !ticket.getUsername().equalsIgnoreCase(principal.getName())) {
            throw new org.springframework.security.access.AccessDeniedException("Bu bilet için indirme yetkiniz yok.");
        }

        // 4. PDF üret
        byte[] pdfBytes = pdfService.createTicketPdf(ticket);

        // 5. Tarayıcıya indirilebilir dosya olarak dön
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bilet-" + ticketId + ".pdf\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
    @Operation(
            summary = "Kullanıcının biletlerini listele",
            description = "JWT içindeki kullanıcı adına göre, kullanıcının satın aldığı tüm biletleri döner."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bilet listesi başarıyla döndü"),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama başarısız")
    })
    @GetMapping("/my")
    public ResponseEntity<List<Ticket>> myTickets(Principal principal) {
        List<Ticket> list = service.myTickets(principal.getName());
        return ResponseEntity.ok(list);
    }
    @PostMapping("/events/{eventId}/seats/{seatId}/hold")
    public ResponseEntity<?> holdSeat(@PathVariable Long eventId, @PathVariable Long seatId) {

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Koltuk bulunamadı"));

        // 1. Kontrol: Koltuk zaten satılmış mı?
        if (seat.isSold()) {
            return ResponseEntity.badRequest().body("Bu koltuk zaten satılmış!");
        }

        // 2. Kontrol: Koltuk başkası tarafından rezerve edilmiş mi? (Süresi dolmamışsa)
        if (seat.getHoldExpiresAt() != null && seat.getHoldExpiresAt().isAfter(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Bu koltuk şu an başkası tarafından tutuluyor!");
        }

        // 3. İşlem: 10 Dakika Rezerve Et
        seat.setHoldExpiresAt(LocalDateTime.now().plusMinutes(10));
        seatRepository.save(seat);

        // 4. Haber Ver: WebSocket ile herkese duyur (Rengi Sarıya dönsün)
        messagingTemplate.convertAndSend("/topic/events/" + eventId + "/seats", seat);

        return ResponseEntity.ok("Koltuk 10 dakikalığına sizin!");
    }
    @PostMapping("/events/{eventId}/seats/{seatId}/unhold")
    public ResponseEntity<?> unholdSeat(@PathVariable Long eventId, @PathVariable Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Koltuk bulunamadı"));

        // Eğer koltuk satılmamışsa, rezervasyon süresini temizle
        if (!seat.isSold()) {
            seat.setHoldExpiresAt(null);
            seatRepository.save(seat);

            // Herkese koltuğun boşa çıktığını duyur
            messagingTemplate.convertAndSend("/topic/events/" + eventId + "/seats", seat);
        }

        return ResponseEntity.ok("Rezervasyon iptal edildi.");
    }


}
