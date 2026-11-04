package com.ticketapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EventRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "Etkinlik başlığı zorunludur") public String title;
    @NotBlank public String city;
    @NotBlank public String type;
    @NotBlank public String venue; //boş olamaz @notblank
    // JSON’dan tarih okurken formatı belirtelim:
    @NotNull(message = "Datetime yıl ay gün saat dakika şeklinde girilmelidir") // @boş olamaz @notnull
    @Schema(example = "2025-12-10T20:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dateTime;
    @Positive public int totalSeats; // pozitif olucak @positive
    @NotNull
    @DecimalMin(value = "0.01",message = "Fiyat 0'dan büyük olmalıdır")
    public BigDecimal price; //boş olamaz min 0.01 olucak
    private String imageUrl;
    public String description;
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Boş constructor (JSON -> DTO map’lemek için gerekli)
    public EventRequest() {}

    public EventRequest(String title, String city, String type, String venue,
                        LocalDateTime dateTime, int totalSeats, BigDecimal price) {
        this.title = title;
        this.city = city;
        this.type = type;
        this.venue = venue;
        this.dateTime = dateTime;
        this.totalSeats = totalSeats;
        this.price = price;
    }
    // ---- GETTER / SETTER’LAR ----
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    @Schema(example = "2025-12-10T20:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}


