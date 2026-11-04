# TicketFlow Backend (`ticket-app`) — Geliştirici Rehberi
> [!NOTE]
> **Final Reposu:** Bu projeyi Docker ile kullanmak için tek tıkla [TicketFlow](https://github.com/ertugelbir-hub/TicketFlow) reposunu ziyaret edin.  
> **Frontend Web İstemcisi:** [ticketapp-frontend](https://github.com/ertugelbir-hub/biletsatisuygulamasi-frontend)

Bu **TicketFlow** platformunun bir bileşeni olup RESTful API,WebSocket Sunucusu,Dağıtık Hız Sınırlaması ve Apache Kafka tabanlı mesajların eşzamanlı işlenmesi sunmaktadır. Java 21 ve Spring Boot 3 üzerine kuruludur.

---

## 1. Kullanılan Teknolojiler ve Kütüphaneler

* **Temel:** Java 21, Spring Boot 3.5.3, Maven
* **Veri Erişimi:** Spring Data JPA (Hibernate 6), PostgreSQL Driver, H2 Database (`dev` ve `test` profilleri için)
* **Güvenlik & Hız Sınırlandırma:** Spring Security 6, JJWT (`0.11.5`), Bucket4j (`8.1.0` - Redis/Lettuce CAS tabanlı), Spring AOP
* **Mesajlaşma & Gerçek Zamanlı İletişim:** Spring for Apache Kafka, Spring WebSocket (STOMP & SockJS)
* **Önbellek:** Spring Data Redis (`@Cacheable`, `@CacheEvict`)
* **Doküman & Barkod Üretimi:** OpenPDF (`1.3.39`), Google ZXing (`3.5.3` - QR Code)
* **E-Posta & Dokümantasyon:** Spring Boot Starter Mail (JavaMailSender), SpringDoc OpenAPI Starter WebMVC UI (`2.8.13`)
* **Test:** JUnit 5, Mockito, Spring Boot Starter Test, Spring Security Test

---

## 2. Gereksinimler

Docker dışında yerel makinenizde projeyi derlemek ve çalıştırmak için aşağıdaki yazılımların yüklü olması gerekir:

* **Java JDK:** `21`
* **Apache Maven:** `3.9+`
* **Altyapı Servisleri (Varsayılan Profil İçin):** PostgreSQL (`5432`), Redis (`6379`), Apache Kafka (`9092`)

---

## 3. Yerel Çalıştırma ve Test Adımları

### A) Uygulamayı Çalıştırma

`ticket-app` dizinine gidin ve Maven Spring Boot eklentisi ile uygulamayı başlatın:

```bash
cd ticket-app
mvn clean spring-boot:run
```

### B) Birim ve Controller Testlerini Çalıştırma

Testler `@ActiveProfiles("test")` ile çalışır; dış bağımlılıklara ihtiyaç duymadan H2 in-memory veritabanı (`jdbc:h2:mem:ticket-app-test`), basit önbellek (`spring.cache.type=simple`) ve kapalı Kafka dinleyicisi ile tamamen izole şekilde yürütülür:

```bash
mvn test
```

### C) JAR Paketi Oluşturma

```bash
mvn clean package -Dmaven.test.skip=true
```

---

## 4. Çevre Değişkenleri (Environment Variables)

Uygulama yapılandırması `src/main/resources/application.properties` dosyasında tanımlanmıştır ve üretim/konteyner ortamında aşağıdaki çevre değişkenleri ile ezilebilir (override):

| Çevre Değişkeni | Varsayılan Değer / Örnek | Açıklama |
| :--- | :--- | :--- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/ticketdb` | PostgreSQL JDBC bağlantı adresi (Docker'da `jdbc:postgresql://ticket-db:5432/ticketdb`) |
| `DB_USERNAME` | `postgres` | Veritabanı kullanıcı adı |
| `DB_PASSWORD` | `password123` | Veritabanı şifresi |
| `JWT_SECRET` | `9f2a3c6e7b1d4f8a2c9e1b7f4d3a6c9f1e2d4b6a8c0e3f5d7a9c1e3g5` | HMAC-SHA256 algoritması için kullanılan gizli JWT anahtarı |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `kafka:29092` | Kafka broker bağlantı adresi (Yerel çalışmada `localhost:9092`) |
| `SPRING_DATA_REDIS_HOST` | `redis` | Spring Cache için Redis ipsi(Yerel çalışmada `localhost`) |
| `APP_REDIS_URL` | `redis://localhost:6379` | Bucket4j `RateLimitConfig` (Lettuce istemcisi) için Redis bağlantı URL'i |
| `MAIL_HOST` | `sandbox.smtp.mailtrap.io` | E-posta gönderimi için SMTP sunucu adresi |
| `MAIL_PORT` | `2525` | SMTP sunucu portu |
| `MAIL_USERNAME` | `your_mailtrap_username` | SMTP kullanıcı adı |
| `MAIL_PASSWORD` | `your_mailtrap_password` | SMTP şifresi |

---

## 5. Swagger / OpenAPI Erişimi

Uygulama çalışırken tüm REST uç noktalarını,DTO şemalarını, istek/yanıt gövdesi örneklerini (`SwaggerExamples.java`) ve hataları keşfedebilir ve test edebilirsiniz:

* **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI v3 JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

**Yetkilendirme:** `/api/auth/login` uç noktasından aldığınız JWT değerini sağ üstteki **Authorize (`BearerAuth`)** butonuna girerek korumalı (`ADMIN` ve `USER`) uç noktaları doğrudan Swagger üzerinden test edebilirsiniz.

---

## 6. Kafka ve WebSocket Akış Detayları

### A) Apache Kafka Yapılandırması

Bilet satın alma işlemi (`TicketService.purchase`) tamamlandığında, işlem süresi uzamaması için bildirim ve belge üretimi Kafka'ya devredilir:

* **Topic Adı:** `notification-topic`
* **Consumer Group ID:** `ticket-notification-group`
* **Producer Sınıfı:** `com.ticketapp.service.NotificationProducer`
* **Consumer Sınıfı:** `com.ticketapp.service.NotificationConsumer`
* **Taşınan Olay Nesnesi (`TicketNotificationEvent`):**
  ```json
  {
    "ticketId": 101,
    "eventId": 1,
    "username": "ayse",
    "eventTitle": "Rock Konseri",
    "quantity": 2,
    "totalPrice": 300.00,
    "email": "ayse@example.com",
    "remainingSeats": 98,
    "soldLast24Hours": 14
  }
  ```

### B) WebSocket (STOMP / SockJS) Kanalları

`WebSocketConfig` sınıfı üzerinde `/ws` STOMP bağlantı noktası ve `/topic` mesaj aracısı (broker) tanımlanmıştır:

| Kanal (Destination) | Tetikleyen Sınıf | Açıklama |
| :--- | :--- | :--- |
| `/topic/sales` | `NotificationConsumer` | Bu kanal Kafka `notification-topic` 'dan herhangi bir mesaj tüketildiğinde tetiklenir. İstemcilere kalan koltuk stokunu (`remainingSeats`), son 24 saatteki satış sayısını (`soldLast24Hours`) ve anlık satılan koltuk sayısını yayınlayacaktır. |
| `/topic/events/{eventId}/seats` | `TicketController`, `TicketService`, `SeatCleanupService` | Koltuk 10 dakikalığına tutulduğunda (`hold`), bırakıldığında (`unhold`), satın alındığında (`purchase`) veya 60 saniyelik periyodik temizlikte (`releaseExpiredSeats`) süresi dolduğunda tüm bağlı müşteriler için koltuk haritasını gerçek zamanlı güncelleyecek. |