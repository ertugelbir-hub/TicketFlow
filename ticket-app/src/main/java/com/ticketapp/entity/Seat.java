package com.ticketapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rowName; // A, B, C...
    private int seatNumber; // 1, 2, 3...

    private boolean isSold = false; // Dolu mu boş mu?
    private LocalDateTime holdExpiresAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @JsonIgnore // Sonsuz döngü olmasın diye
    private Event event;
    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;


    public Seat() {}

    public Seat(String rowName, int seatNumber, Event event) {
        this.rowName = rowName;
        this.seatNumber = seatNumber;
        this.event = event;
        this.isSold = false;
        this.version = 0;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public String getRowName() { return rowName; }
    public void setRowName(String rowName) { this.rowName = rowName; }
    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    public boolean isSold() { return isSold; }
    public void setSold(boolean sold) { isSold = sold; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public LocalDateTime getHoldExpiresAt() {return holdExpiresAt;}
    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) {this.holdExpiresAt = holdExpiresAt;}
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}