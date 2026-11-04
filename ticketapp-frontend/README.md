# TicketFlow Frontend (`ticketapp-frontend`) — Arayüz Geliştirici Rehberi
> [!NOTE]
> **Final Reposu:** Bu projeyi Docker ile tek tıkla ayağa kaldırmak için [TicketFlow](https://github.com/ertugelbir-hub/TicketFlow) reposunu ziyaret edin.  
> **Backend API Reposu:** [biletsatisuygulamasi](https://github.com/ertugelbir-hub/biletsatisuygulamasi)

Bu modül, **TicketFlow** platformunun **React 18** ve **Vite** ile geliştirilmiş kullanıcı ve yönetici arayüzüdür. Gerçek zamanlı V-Amfi koltuk haritası, canlı stok animasyonları, çoklu dil (i18n) desteği, interaktif kredi kartı ödeme ekranı ve grafiksel yönetici raporlama panellerini içerir.

---

## 1. Teknik Yığın ve Kütüphaneler

* **Çekirdek & Derleyici:** React 18, Vite, React Router DOM
* **Stil & UI Bileşenleri:** Bootstrap 5, Bootstrap Icons, Özel Glassmorphism & Gece Mavisi (`#080d1a`) Tema CSS, `react-credit-cards-2`, `react-spinners`
* **Gerçek Zamanlı İletişim:** `sockjs-client`, `@stomp/stompjs`
* **Çoklu Dil (i18n):** `i18next`, `react-i18next` (TR, EN, DE)
* **Veri Görselleştirme & Bildirimler:** `recharts` (BarChart satış analitiği), `react-toastify`
* **HTTP & Kimlik Doğrulama:** `axios`, `jwt-decode`

---

## 2. Gereksinimler

Yerel olarak projeyi çalıştırmak için aşağıdaki yazılımların kurulması gerekir:

* **Node.js:** `20+`
* **npm:** Node.js ile birlikte gelen paket yöneticisi

---

## 3. Kurulum, Yerel Başlatma ve Build

### A) Bağımlılıkların Kurulumu

`ticketapp-frontend` klasörüne girin ve gerekli bağımlılıkları yükleyin:

```bash
cd ticketapp-frontend
npm install
```

### B) Geliştirme Sunucusunu Başlatma

```bash
npm run dev
```

Uygulama varsayılan olarak **[http://localhost:5173](http://localhost:5173)** adresinde çalışacaktır.

### C) Production Build Alma

Uygulamayı üretim ortamı için derlemek ve optimize edilmiş statik dosyaları `dist/` klasörüne çıkartmak için:

```bash
npm run build
```

---

## 4. Çevre Değişkenleri (Environment Variables)

Frontend uygulaması, Backend REST API sunucusuna bağlanmak için `VITE_API_BASE_URL` çevre değişkenini kullanır. `ticketapp-frontend` kök dizininde bir `.env` dosyası oluşturarak veya Docker Compose üzerinden bu değeri yapılandırabilirsiniz:

```env
VITE_API_BASE_URL=http://localhost:8080
```

* `src/apiClient.js` içerisinde bu değişken okunur (`import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"`).
* Axios Request Interceptor, `localStorage` veya `sessionStorage` üzerinde bulunan JWT'yi her isteğin `Authorization: Bearer <token>` başlığına otomatik ekler.
* Axios Response Interceptor, Backend'den dönen `429 Too Many Requests` (Bucket4j Rate Limit) hatalarını yakalar ve `retryAfter` saniyesine göre ekranda dinamik geri sayım yapan bir Toast bildirimi gösterir.

---

## 5. Çoklu Dil (i18n) Dizin Yapısı

Uygulama **Türkçe (TR)**, **İngilizce (EN)** ve **Almanca (DE)** dillerini destekler. Seçilen dil tercihi `localStorage` üzerinde `user-language` anahtarıyla saklanır.

```text
ticketapp-frontend/src/
├── i18n.js                 # i18next başlatma, varsayılan dil ve fallback ('tr') yapılandırması
└── locales/
    ├── tr.json             # Türkçe dil sözlüğü
    ├── en.json             # İngilizce dil sözlüğü
    └── de.json             # Almanca dil sözlüğü
```

Bileşenler içerisinde `useTranslation` hook'u ile çeviri anahtarları çağrılır ve üst menüden (`Navbar.jsx`) veya giriş ekranından (`Auth.jsx`) sayfa yenilenmeden dil değiştirilebilir.

---

## 6. WebSocket Bağlantı Mimarisi

Canlı koltuk seçimi ve anlık stok güncellemeleri için Backend'in `/ws` SockJS uç noktasına **STOMP** protokolü ile bağlantı kuruluyor:

* **Bağlantı Adresi:** `http://localhost:8080/ws`
* **Canlı Satış ve Stok (`/topic/sales`):**
  * `src/App.jsx` içerisinde.
  * Bir bilet satıldığında kalan koltuk sayısını (`remainingSeats`), son 24 saatteki satış sayacını (`soldLast24Hours`) ve *"Az önce X bilet daha satıldı!"* uyarısını `EventDetail.jsx` bileşeninde anlık animasyonlarla (`stock-flash`, `blink-infinite`) güncelliyor.
* **Canlı Koltuk Haritası (`/topic/events/{eventId}/seats`):**
  * `src/components/events/SeatMap.jsx` içerisinde.
  * Bir kullanıcı bir koltuğu tuttuğunda (`hold`), bıraktığında (`unhold`) veya satın aldığında koltuk rengini (Boş: Gri, Seçili: Yeşil, Rezerve: Sarı, Dolu: Kırmızı) tüm kullanıcılarda anında günceller ve 10 dakikalık rezervasyon geri sayım sayacını (`timeLeft`) çalıştırır.