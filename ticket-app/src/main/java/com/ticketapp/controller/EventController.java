package com.ticketapp.controller;

import com.ticketapp.dto.EventRequest;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.entity.Event;
import com.ticketapp.entity.Seat;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.SeatRepository;
import com.ticketapp.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.ticketapp.config.SwaggerExamples.*;

@Tag(name = "Events", description = "Etkinlik oluşturma, listeleme ve yönetim işlemleri")
@RestController
@RequestMapping("/api/events")
@Validated
public class EventController {

    private final EventService service;
    private final SeatRepository seatRepo;
    private final EventRepository eventRepository;
    public EventController(EventService service, SeatRepository seatRepo, EventRepository eventRepository) {
        this.service = service;
        this.seatRepo = seatRepo;
        this.eventRepository = eventRepository;
    }
    //
    @Operation(summary = "Event oluştur (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Oluşturuldu",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name="Event", value = EVENT_RES)
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
                    examples = @ExampleObject(name="Create body", value = EVENT_CREATE_REQ)
            )
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping //admin
    public ResponseEntity<Event> create(@RequestBody @Valid EventRequest req) {
        return ResponseEntity.ok(service.create(req));
    }
    @Operation(summary = "Event listesini getir (herkese açık)")
    @ApiResponse(responseCode = "200", description = "Başarılı",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name="List", value = "[" + EVENT_RES + "]")
            ))
    @GetMapping
    public ResponseEntity<List<Event>> list(@RequestParam(required = false, defaultValue = "newest") String sort) {
        List<Event> events;

        if ("best-selling".equalsIgnoreCase(sort)) {
            // "best-selling" istenirse çok satanları getir
            events = eventRepository.findAllByOrderBySoldTicketsDesc();
        } else {
            // Bir şey belirtilmezse veya "newest" denirse yeniye/tarihe göre getir
            events = eventRepository.findAllByOrderByDateTimeAsc();
        }

        return ResponseEntity.ok(events);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")      // ADMIN
    public ResponseEntity<Event> update(@PathVariable Long id, @RequestBody @Valid EventRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")   // ADMIN
    public ResponseEntity<String> delete(@Valid @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok("Silindi");
    }
    @GetMapping("/reports/sales")
    public ResponseEntity<SalesReport> sales(@Valid @RequestParam Long eventId) {
        return ResponseEntity.ok(service.salesReport(eventId));
    } // LIST paged (parametre doğrulaması)
    @GetMapping("/paged")   // <--- ÖNEMLİ: /api/events/paged olur
    public ResponseEntity<Page<Event>> listPaged(

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        String[] parts = sort.split(",");
        Sort s = Sort.by(Sort.Direction.fromString(parts.length > 1 ? parts[1] : "asc"), parts[0]);
        Page<Event> result = service.listPaged(PageRequest.of(page, size, s));
        return ResponseEntity.ok(result);

    }

    @Operation(
            summary = "Event arama/sıralama/sayfalama",
            description = "Şehre, türe, başlığa ve tarih aralığına göre filtreler. "
                    + "page/size/sort/dir ile sayfalama ve sıralama yapar."
    )
    @ApiResponse(responseCode = "200", description = "Başarılı")
    @GetMapping("/search")
    public Page<Event> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String q,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateTime") String sort,
            @RequestParam(defaultValue = "asc") String dir
    ) {
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        return service.search(city, type, q, from, to, pageable);
    }
    @GetMapping("/{id}/seats")
    public ResponseEntity<List<Seat>> getEventSeats(@PathVariable Long id) {
        return ResponseEntity.ok(seatRepo.findByEventIdOrderByIdAsc(id));
    }



}


