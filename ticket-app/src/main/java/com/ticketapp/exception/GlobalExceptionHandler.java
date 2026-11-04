package com.ticketapp.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 1) @Valid (DTO/body) ihlalleri
    // @Valid body hataları 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> onValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "validation");

        List<Map<String, String>> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", defaultMsg(fe)
                ))
                .collect(Collectors.toList());

        body.put("details", details);
        return ResponseEntity.badRequest().body(body);
    }

    private String defaultMsg(FieldError fe) {
        String def = fe.getDefaultMessage();
        if (def != null && !def.isBlank()) return def;

        switch (fe.getField()) {
            case "eventId":
                return "Etkinlik id girilmelidir";
            case "username":
                return "Kullanıcı adı (username) zorunludur";
            case "quantity":
                return "quantity en az 1 olmalı";
            default:
                return "Geçersiz değer";
        }
    }


    // 2) @Validated + @RequestParam/@PathVariable ihlalleri 400
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> onConstraint(ConstraintViolationException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "constraint");

        List<Map<String, String>> details = ex.getConstraintViolations()
                .stream()
                .map(v -> Map.of(
                        "property", v.getPropertyPath().toString(),
                        "message", v.getMessage()
                ))
                .collect(Collectors.toList());

        body.put("details", details);
        return ResponseEntity.badRequest().body(body);
    }
    // 3) JSON parse / tarih formatı → 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> onJsonParse(HttpMessageNotReadableException ex) {
        String msg = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        // Spring 6 / Boot 3: kök nedeni doğrudan alalım
        Throwable root = ex.getRootCause();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "validation");

        List<Map<String, String>> details = new ArrayList<>();

        // ipucu: LocalDateTime parse hatası mı?
        // LocalDateTime parse hatasını "tip güvenli" biçimde veya mesaj ipuçlarıyla yakala
        boolean isDateTimeParse =
                (root instanceof DateTimeParseException) ||
                        (msg != null && (
                                msg.contains("DateTimeParseException") ||  // bazı sürümlerde geçer
                                        msg.contains("could not be parsed") ||     // tipik ifade
                                        msg.contains("LocalDateTime")              // eski/diğer varyasyonlar
                        ));

        if (isDateTimeParse) {
            details.add(Map.of(
                    "field", "dateTime",
                    "message", "Tarih formatı yyyy-MM-dd'T'HH:mm olmalı. Örn: 2025-12-10T20:00"
            ));
        } else {
            details.add(Map.of(
                    "field", "body",
                    "message", "Geçersiz JSON veya alan formatı"
            ));
        }

        body.put("details", details);
        return ResponseEntity.badRequest().body(body);
    }
    // 4) Parametre tipi uyuşmazlığı → 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> onTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "validation");
        body.put("details", List.of(Map.of(
                "field", ex.getName(),
                "message", "Parametre tipi hatalı: " + (ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "beklenen tip")
        )));
        return ResponseEntity.badRequest().body(body);
    }
    // 5) Zorunlu parametre eksik → 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> onMissingParam(MissingServletRequestParameterException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "validation");
        body.put("details", List.of(Map.of(
                "field", ex.getParameterName(),
                "message", "Zorunlu parametre eksik"
        )));
        return ResponseEntity.badRequest().body(body);
    }
    // 6) 404 – Domain yok
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> onNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "not_found");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
    // 7) 409 – Optimistic lock
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> onOptLock(OptimisticLockingFailureException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "concurrency");
        body.put("message", "İşlem çakıştı, lütfen tekrar deneyin.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body); // 409
    }
    // 8) 409 – Çakışma (örn. kullanıcı adı kullanılıyor)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> onDuplicate(DuplicateResourceException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "conflict");
        body.put("message", ex.getMessage()); // örn: "Kullanıcı adı zaten kullanılıyor"
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body); // 409
    }
    // 9) 403 – Yetki yok (ADMIN gerekli)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> onAccessDenied(AccessDeniedException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "forbidden");
        body.put("message", "Bu işlem için yetkiniz yok (ADMIN rolü gerekli).");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body); // 403
    }
    // 10) Genel (iş kuralı vb.) → 400
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> onRuntime(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "runtime");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    // 11) Beklenmeyen → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> onGeneric(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "error");
        body.put("message", "Beklenmeyen bir hata oluştu");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitException(RateLimitException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        // React tarafındaki i18n anahtarını (key) gönderiyoruz
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("retryAfter", ex.getRetryAfter());

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS) // 429 Too Many Requests
                .body(errorResponse);
    }
}

