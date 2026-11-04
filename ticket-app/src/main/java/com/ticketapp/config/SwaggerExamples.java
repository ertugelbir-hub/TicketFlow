package com.ticketapp.config;

public final class SwaggerExamples {
    private SwaggerExamples() {}
    // Ortak parametre örnekleri
    public static final String DATE_FROM = "2025-12-01T00:00";
    public static final String DATE_TO   = "2025-12-31T23:59";
    // Response örnekleri


    // Auth
    public static final String LOGIN_REQ = "{\n" +
            "  \"username\": \"admin\",\n" +
            "  \"password\": \"admin123\"\n" +
            "}";
    public static final String LOGIN_RES = "{\n" +
            "  \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n" +
            "}";

    // Event
    public static final String EVENT_CREATE_REQ = "{\n" +
            "  \"title\": \"Swagger Test Konseri\",\n" +
            "  \"city\": \"Ankara\",\n" +
            "  \"type\": \"Concert\",\n" +
            "  \"venue\": \"Arena\",\n" +
            "  \"dateTime\": \"2025-12-10T20:00\",\n" +
            "  \"totalSeats\": 10,\n" +
            "  \"price\": 150\n" +
            "}";
    public static final String EVENT_RES = "{\n" +
            "  \"id\": 1,\n" +
            "  \"title\": \"Swagger Test Konseri\",\n" +
            "  \"city\": \"Ankara\",\n" +
            "  \"venue\": \"Arena\",\n" +
            "  \"dateTime\": \"2025-12-10T20:00\",\n" +
            "  \"totalSeats\": 10,\n" +
            "  \"price\": 150\n" +
            "}";

    // Ticket
    public static final String TICKET_PURCHASE_REQ = "{\n" +
            "  \"eventId\": 1,\n" +
            "  \"quantity\": 3\n" +
            "}";

    // Report
    public static final String REPORT_FULL_LIST = "[{\n" +
            "  \"eventId\": 1,\n" +
            "  \"title\": \"Swagger Test Konseri\",\n" +
            "  \"totalSeats\": 10,\n" +
            "  \"soldInRange\": 0,\n" +
            "  \"soldAllTime\": 3,\n" +
            "  \"remaining\": 7,\n" +
            "  \"price\": 150,\n" +
            "  \"revenueRange\": 0,\n" +
            "  \"revenueAllTime\": 450\n" +
            "}]";
    public static final String REPORT_FULL_PAGE = "{\n" +
            "  \"content\": " + REPORT_FULL_LIST + ",\n" +
            "  \"pageable\": {\"pageNumber\": 0, \"pageSize\": 10, \"offset\": 0, \"paged\": true},\n" +
            "  \"last\": true, \"totalElements\": 1, \"totalPages\": 1,\n" +
            "  \"size\": 10, \"number\": 0, \"first\": true, \"numberOfElements\": 1, \"empty\": false\n" +
            "}";
    // Hata modeli örneği
    public static final String ERROR_RES = "{\n" +
            "  \"error\": \"validation\",\n" +
            "  \"details\": [ { \"field\": \"dateTime\", \"message\": \"Tarih formatı yyyy-MM-dd'T'HH:mm olmalı\" } ]\n" +
            "}";
    public static final String EVENTS_SEARCH_EXAMPLE_QUERY =
            "city=Ankara&type=Concert&q=Test&from=2025-12-01T00:00&to=2025-12-31T23:59&page=0&size=5&sort=dateTime&dir=asc";
    // RAPOR: Özet endpoint’leri için örnekler
    public static final String REPORT_SUMMARY_BY_PURCHASE = """
[
  {
    "eventId": 1,
    "title": "Swagger Test Konseri",
    "totalSeats": 10,
    "soldInRange": 3,
    "remaining": 7,
    "price": 150.00,
    "revenue": 450.00
  }
]
""";

    public static final String REPORT_SUMMARY_BY_EVENT = """
[
  {
    "eventId": 2,
    "title": "Swagger Test Konseri 2",
    "totalSeats": 20,
    "soldInRange": 5,
    "remaining": 15,
    "price": 200.00,
    "revenue": 1000.00
  }
]
""";

    // CSV örneği (text/csv)
    public static final String REPORT_FULL_CSV = """
eventId,title,totalSeats,soldInRange,soldAllTime,remaining,price,revenueRange,revenueAllTime
1,"Swagger Test Konseri",10,0,3,7,150.00,0.00,450.00
""";

    // Hata örnekleri (Swagger'da 403/404/400 göstermek için)
    public static final String ERROR_FORBIDDEN = """
{ "error": "forbidden", "message": "Bu işlem için yetkiniz yok (ADMIN rolü gerekli)." }
""";

    public static final String ERROR_NOT_FOUND = """
{ "error": "not_found", "message": "Etkinlik bulunamadı." }
""";

// PDF örneği (application/pdf)
public static final String REPORT_FULL_PDF = """
Bu endpoint PDF dosyası döndürür (application/pdf).
İçerik tablo biçimindedir, tarih aralığı ve all-time satış özetlerini içerir.
Örnek sütunlar:
ID | Etkinlik Adı | Toplam | Aralık Satış | Tüm Zaman | Kalan | Fiyat | Aralık Geliri | Toplam Gelir
""";

}

