package com.ticketapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class PurchaseRequest {
    @NotNull(message = "Etkinlik id girilmelidir") @Positive(message = "eventId 1 veya daha büyük olmalı") public Long eventId;
    @Positive(message = "Bilet sayısı pozitif olmalıdır")public int quantity;
    public List<Long> seatIds;

    public String couponCode;

}
