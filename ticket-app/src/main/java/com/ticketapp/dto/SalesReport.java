package com.ticketapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

public class SalesReport implements Serializable {
    private static final long serialVersionUID = 1L;

    public Long eventId;
    public String title;
    public String city;
    public String venue;
    public LocalDateTime dateTime;
    public int totalSeats;
    public int sold;
    public int remaining;
    public BigDecimal price;
    public BigDecimal revenue;
    public int soldLast24Hours;

    public SalesReport(Long eventId, String title,String city,String venue,LocalDateTime dateTime, int totalSeats, int sold, int remaining,  BigDecimal price, BigDecimal revenue, int soldLast24Hours) {
        this.eventId = eventId;
        this.title = title;
        this.city = city;
        this.venue = venue;
        this.dateTime = dateTime;
        this.totalSeats = totalSeats;
        this.sold = sold;
        this.remaining = remaining;
        this.price = price;
        this.revenue = revenue;
        this.soldLast24Hours = soldLast24Hours;
    }
}
