package com.ticketapp.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class SalesSummaryItem implements Serializable {
    private static final long serialVersionUID = 1L;

    public Long eventId;
    public String title;
    public int totalSeats;
    public int sold;
    public int remaining;
    public BigDecimal price;
    public BigDecimal revenue;

    // Esnek kurucu: JPQL'den Number gelebilir, null gelebilir.
    public SalesSummaryItem(Long eventId,
                            String title,
                            Number totalSeats,
                            Number sold,
                            Number remaining,
                            BigDecimal price,
                            BigDecimal revenue) {

        this.eventId = eventId;
        this.title = title;
        this.totalSeats = totalSeats == null ? 0 : totalSeats.intValue();
        this.sold = sold == null ? 0 : sold.intValue();
        this.remaining = remaining == null ? 0 : remaining.intValue();
        this.price = price;
        // Eğer JPQL revenue'yu null gönderirse burada sold * price hesapla
        this.revenue = (revenue != null)
                ? revenue
                : (price == null ? BigDecimal.ZERO
                : price.multiply(BigDecimal.valueOf(this.sold)));
    }
}
