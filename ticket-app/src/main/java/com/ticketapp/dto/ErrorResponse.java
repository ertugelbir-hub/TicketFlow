package com.ticketapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {
    @Schema(example = "validation")
    public String error;

    @Schema(example = "[{\"field\":\"dateTime\",\"message\":\"Tarih formatı yyyy-MM-dd'T'HH:mm olmalı\"}]")
    public List<Detail> details = new ArrayList<>();
    public ErrorResponse() {}
    public ErrorResponse(String error) { this.error = error; }

    public ErrorResponse add(String field, String message) {
        Detail d = new Detail(); d.field = field; d.message = message;
        this.details.add(d);
        return this;
    }
    public static class Detail {
        public String field;
        public String message;
    }
}
