package com.ticketapp.controller;

import com.ticketapp.entity.Event;
import com.ticketapp.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "Favorites", description = "Favori ekleme ve listeleme işlemleri")
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService service;

    public FavoriteController(FavoriteService service) {
        this.service = service;
    }

    @Operation(summary = "Favorilere ekle/çıkar (Toggle)")
    @PostMapping("/{eventId}")
    public ResponseEntity<String> toggleFavorite(@PathVariable Long eventId, Principal principal) {
        String message = service.toggleFavorite(principal.getName(), eventId);
        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Favorilerimi getir")
    @GetMapping
    public ResponseEntity<List<Event>> getMyFavorites(Principal principal) {
        return ResponseEntity.ok(service.getMyFavorites(principal.getName()));
    }
}