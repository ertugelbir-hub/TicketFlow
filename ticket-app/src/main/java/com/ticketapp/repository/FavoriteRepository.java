package com.ticketapp.repository;

import com.ticketapp.entity.Event;
import com.ticketapp.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // Kullanıcının favorilerini getir
    @Query("SELECT f.event FROM Favorite f WHERE f.user.username = :username")
    List<Event> findFavoriteEventsByUsername(@Param("username") String username);

    // Kullanıcı bu etkinliği favorilemiş mi?
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Favorite f WHERE f.user.username = :username AND f.event.id = :eventId")
    boolean isFavorite(@Param("username") String username, @Param("eventId") Long eventId);
    // Favoriyi silmek için bul
    @Query("SELECT f FROM Favorite f WHERE f.user.username = :username AND f.event.id = :eventId")
    Optional<Favorite> findFavoriteEntry(@Param("username") String username, @Param("eventId") Long eventId);
}