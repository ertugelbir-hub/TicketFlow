package com.ticketapp.exception;

public class ErrorMessages {


    // Ticket satın alma
    public static final String INVALID_QUANTITY   = "Adet 1 veya daha fazla olmalı";
    public static final String NO_SEATS_LEFT      = "Yeterli koltuk yok";
    public static final String RETRY_FAILED       = "İşlem çakıştı, lütfen tekrar deneyin.";
    public static final String PURCHASE_FAILED    = "Satın alma işlemi başarısız oldu.";

    // Ortak bulunamadı mesajları
    public static final String USER_NOT_FOUND     = "Kullanıcı bulunamadı";
    public static final String USERNAME_ALREADY_USED = "Kullanıcı adı zaten kullanılıyor";
    public static final String EVENT_NOT_FOUND    = "Etkinlik bulunamadı";
    public static final String TICKET_NOT_FOUND   = "Bilet bulunamadı";

    // Yetki
    public static final String TICKET_CANCEL_FORBIDDEN = "Bu bileti iptal etme yetkiniz yok";

}
