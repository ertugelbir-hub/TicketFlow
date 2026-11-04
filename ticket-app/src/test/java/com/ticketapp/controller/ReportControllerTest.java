package com.ticketapp.controller;

import com.ticketapp.service.ReportService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReportController.class)
@AutoConfigureMockMvc(addFilters = false) // Security filtrelerini bu sınıfta kapatıyoruz
@ActiveProfiles("test")
class ReportControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ReportService reportService;

    // SecurityConfig içindeki bean'lere karşılık mock'lar
    @MockBean
    com.ticketapp.security.JwtAuthFilter jwtAuthFilter;

    @MockBean
    com.ticketapp.security.JwtService jwtService;

    /**
     * 1) CSV rapor:
     *    GET /api/reports/sales/full.csv
     *    ReportService.fullCsv(...) çıktısı response gövdesine
     *    ve doğru header’larla yazılıyor mu?
     */
    @Test
    void fullCsv_admin_ok() throws Exception {
        // GIVEN - sahte CSV içeriği
        byte[] csv = "eventId,title\n1,Konser\n".getBytes(StandardCharsets.UTF_8);

        Mockito.when(reportService.fullCsv(any(), any()))
                .thenReturn(csv);
        Mockito.when(reportService.fullCsvFilename(any(), any()))
                .thenReturn("sales-2025-12.csv");

        mvc.perform(get("/api/reports/sales/full.csv")
                        // from / to param vermesek de opsiyonel, sorun değil
                )
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"sales-2025-12.csv\""))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL,
                        "no-cache, no-store, must-revalidate"))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().bytes(csv)); // body aynen service’ten gelen CSV
    }

    /**
     * 2) PDF rapor:
     *    GET /api/reports/sales/full.pdf?from=...&to=...
     *    -> ReportService.fullPdf(...) çıktısı PDF olarak dönüyor mu?
     */
    @Test
    void fullPdf_admin_ok() throws Exception {
        // GIVEN - sahte PDF içeriği
        byte[] pdf = new byte[]{1, 2, 3, 4, 5};

        Mockito.when(reportService.fullPdf(any(), any()))
                .thenReturn(pdf);
        Mockito.when(reportService.fullPdfFilename(any(), any()))
                .thenReturn("sales-2025-12.pdf");

        mvc.perform(get("/api/reports/sales/full.pdf")
                        .param("from", "2025-12-01T00:00")
                        .param("to", "2025-12-31T23:59"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"sales-2025-12.pdf\""))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL,
                        "no-cache, no-store, must-revalidate"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(pdf));
    }

//    /**
//     * 3) Güvenlik testi (şimdilik devre dışı):
//     *    Security filtrelerini addFilters=false ile kapattığımız için burada 403 alamıyoruz.
//     *    Gerçek entegrasyon testi yazınca bu @Disabled kaldırılacak.
//     */
@Disabled("Security filtreleri test profilinde kapalı (addFilters = false). " +
        "Gerçek 403 testi JWT ile entegrasyon testinde yazılacak.")
    @Test
    void full_admin_required_403() throws Exception {
        mvc.perform(get("/api/reports/sales/full.pdf")
                        .param("from", "2025-12-01T00:00")
                        .param("to", "2025-12-31T23:59")
                        .with(user("alice").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
