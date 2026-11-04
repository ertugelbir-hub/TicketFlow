package com.ticketapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
@Schema(name = "FullSalesReport", description = "Bir etkinlik için all-time ve tarih aralığı satış/metrikleri")
public class FullSalesReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(example = "1", description = "Etkinlik ID")
    public Long eventId;
    @Schema(example = "Swagger Test Konseri", description = "Etkinlik adı")
    public String title;
    @Schema(example = "10", description = "Toplam koltuk")
    public int totalSeats;
    @Schema(example = "0", description = "[from,to] aralığında satılan bilet adedi")
    public int soldInRange;     // from-to aralığı satışı
    @Schema(example = "3", description = "Tüm zamanlarda satılan bilet adedi")
    public int soldAllTime;     // tüm zaman satışı
    @Schema(example = "7", description = "Kalan koltuk")
    public int remaining;       // totalSeats - allTime(sold)
    @Schema(example = "150", description = "Bilet fiyatı")
    public BigDecimal price;
    @Schema(example = "0", description = "Aralık içi gelir")
    public BigDecimal revenueRange;    // soldInRange * price
    @Schema(example = "450", description = "Tüm zaman gelir")
    public BigDecimal revenueAllTime;  // soldAllTime * price

    public FullSalesReport() { }

    public FullSalesReport(Long eventId, String title, int totalSeats,
                           int soldInRange, int soldAllTime, int remaining,
                           BigDecimal price, BigDecimal revenueRange, BigDecimal revenueAllTime) {
        this.eventId = eventId;
        this.title = title;
        this.totalSeats = totalSeats;
        this.soldInRange = soldInRange;
        this.soldAllTime = soldAllTime;
        this.remaining = remaining;
        this.price = price;
        this.revenueRange = revenueRange;
        this.revenueAllTime = revenueAllTime;
    }
}
