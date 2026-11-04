package com.ticketapp.service;

import com.ticketapp.entity.Event;
import com.ticketapp.entity.Favorite;
import com.ticketapp.entity.User;
import com.ticketapp.exception.ErrorMessages;
import com.ticketapp.exception.ResourceNotFoundException;
import com.ticketapp.repository.EventRepository;
import com.ticketapp.repository.FavoriteRepository;
import com.ticketapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepo;
    private final UserRepository userRepo;
    private final EventRepository eventRepo;

    public FavoriteService(FavoriteRepository favoriteRepo, UserRepository userRepo, EventRepository eventRepo) {
        this.favoriteRepo = favoriteRepo;
        this.userRepo = userRepo;
        this.eventRepo = eventRepo;
    }

    @Transactional
    public String toggleFavorite(String username, Long eventId) {
        // 1. Kullanıcı ve Etkinliği bul
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.EVENT_NOT_FOUND));

        // 2. Kontrol et: Zaten favoride mi?
        Optional<Favorite> existingFav = favoriteRepo.findFavoriteEntry(username, eventId);

        if (existingFav.isPresent()) {
            // Varsa sil (Unfavorite)
            favoriteRepo.delete(existingFav.get());
            return "Favorilerden çıkarıldı 💔";
        } else {
            // Yoksa ekle (Favorite)
            Favorite fav = new Favorite(user, event);
            favoriteRepo.save(fav);
            return "Favorilere eklendi ❤️";
        }
    }
    @Transactional(readOnly = true)
    public List<Event> getMyFavorites(String username) {
        // Favori tablosundan sadece Event objelerini çekip liste olarak dönüyoruz
        return favoriteRepo.findFavoriteEventsByUsername(username);
    }
}