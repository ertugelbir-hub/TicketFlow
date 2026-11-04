package com.ticketapp.dto;

import java.io.Serializable;
import java.math.BigDecimal;

// Kafka ile taşınacak veri paketi
public class TicketNotificationEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ticketId;
    private Long eventId;
    private String username;
    private String eventTitle;
    private int quantity;
    private BigDecimal totalPrice;
    private String email;
    private int remainingSeats;
    private int soldLast24Hours;

    public TicketNotificationEvent() {}

    public TicketNotificationEvent(Long ticketId, Long eventId, String username, String eventTitle,
                                   int quantity, BigDecimal totalPrice, String email,
                                   int remainingSeats, int soldLast24Hours) {
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.username = username;
        this.eventTitle = eventTitle;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.email = email;
        this.remainingSeats = remainingSeats;
        this.soldLast24Hours = soldLast24Hours;
    }
    public Long getTicketId() { return ticketId; }
    public Long getEventId() { return eventId; }
    public String getUsername() { return username; }
    public String getEventTitle() { return eventTitle; }
    public int getQuantity() { return quantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getEmail() { return email; } //
    public int getRemainingSeats() { return remainingSeats; }
    public int getSoldLast24Hours() { return soldLast24Hours; }
    @Override
    public String toString() {
        return "Bilet Detayı { " +
                "Kullanıcı='" + username + '\'' +
                ", Etkinlik='" + eventTitle + '\'' +
                ", Adet=" + quantity +
                ", Kalan=" + remainingSeats +
                ", Son 24s Satış=" + soldLast24Hours +
                " }";
    }
}