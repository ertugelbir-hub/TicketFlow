package com.ticketapp.controller;

import com.ticketapp.dto.FullSalesReport;
import com.ticketapp.dto.SalesReport;
import com.ticketapp.dto.SalesSummaryItem;
import com.ticketapp.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static com.ticketapp.config.SwaggerExamples.*;
@Tag(name = "Reports", description = "Satış raporları ve istatistikler")
@RestController
@RequestMapping("/api/reports/sales")
@PreAuthorize("hasRole('ADMIN')")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Başarılı"),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli"),
        @ApiResponse(responseCode = "403", description = "Yetkisiz (ADMIN rolü gerekli)")
})
public class ReportController {
    private final ReportService reportService;
    public ReportController( ReportService reportService) { this.reportService = reportService;}

    // A) Satın alma tarihine göre özet
    @Operation(
            summary = "Satın alma tarihine göre satış özeti",
            parameters = {
                    @Parameter(name = "from", example = DATE_FROM),
                    @Parameter(name = "to",   example = DATE_TO)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Başarılı",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(name = "summary-by-purchase", value = REPORT_SUMMARY_BY_PURCHASE)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Validation",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = ERROR_RES)
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Forbidden",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = ERROR_FORBIDDEN)
                            )
                    )
            }
    )
    @GetMapping("/summary-by-purchase")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SalesSummaryItem>> summaryByPurchase(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.summaryByPurchase(from, to));
    }
       // B) Etkinlik tarihine göre özet
       @Operation(
               summary = "Etkinlik tarihine göre satış özeti",
               parameters = {
                       @Parameter(name = "from", example = DATE_FROM),
                       @Parameter(name = "to",   example = DATE_TO)
               },
               responses = {
                       @ApiResponse(responseCode = "200", description = "Başarılı",
                               content = @Content(
                                       mediaType = "application/json",
                                       examples = @ExampleObject(name = "summary-by-event", value = REPORT_SUMMARY_BY_EVENT)
                               )
                       ),
                       @ApiResponse(responseCode = "400", description = "Validation",
                               content = @Content(mediaType = "application/json",
                                       examples = @ExampleObject(value = ERROR_RES)
                               )
                       ),
                       @ApiResponse(responseCode = "403", description = "Forbidden",
                               content = @Content(mediaType = "application/json",
                                       examples = @ExampleObject(value = ERROR_FORBIDDEN)
                               )
                       )
               }
       )
    @GetMapping("/summary-by-event")
    public ResponseEntity<List<SalesSummaryItem>> summaryByEvent(
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to)
    {
        return ResponseEntity.ok(reportService.summaryByEvent(from, to));
    }
    @Operation(
            summary = "All-time satış özeti (tek etkinlik)",
            description = "eventId ile tek bir etkinliğin tüm zaman satışlarını (sold/remaining/revenue) döner."
    )

    @ApiResponse(responseCode = "200", description = "Başarılı")
    @GetMapping("/alltime")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalesReport> allTime(@RequestParam Long eventId) {
        return ResponseEntity.ok(reportService.allTimeSales(eventId));
    }
    @Operation(
            summary = "Birleşik rapor: tarih aralığı + all-time",
            description = "Her etkinlik için hem [from,to] aralığındaki satışı hem de tüm zaman satışı ve gelirlerini birlikte döner.",
        parameters = {
        @Parameter(name = "from", description = "Başlangıç", example = DATE_FROM),
        @Parameter(name = "to",   description = "Bitiş",      example = DATE_TO)
    }, responses = {
    @ApiResponse(
            responseCode = "200",
            description = "Başarılı",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation  = java.util.List.class)),
                    examples = @ExampleObject(name = "report-full", value = REPORT_FULL_LIST)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = ERROR_FORBIDDEN)
                    )
            )
    }
    )
    @GetMapping("/full")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FullSalesReport>> full(
            @Parameter(example = "2025-12-01T00:00")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime from,
            @Parameter(example = "2025-12-31T23:59")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime to
    ) {
        return ResponseEntity.ok(reportService.full(from, to));
    }
    @Operation(
            summary = "Birleşik rapor CSV indir",
            parameters = {
                    @Parameter(name = "from", example = DATE_FROM),
                    @Parameter(name = "to",   example = DATE_TO)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "CSV",
                            content = @Content(
                                    mediaType = "text/csv",
                                    schema = @Schema(type = "string", format = "binary"),
                                    examples = @ExampleObject(name = "csv-preview", value = REPORT_FULL_CSV)
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Forbidden",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = ERROR_FORBIDDEN)
                            )
                    )
            }
    )

    // CSV
    @GetMapping("/full.csv")
    public void fullCsv(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime to,
            HttpServletResponse response) throws IOException {

        byte[] csv = reportService.fullCsv(from, to);
        String filename = reportService.fullCsvFilename(from, to);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        response.getOutputStream().write(csv);
        response.flushBuffer();
    }



    @Operation(
            summary = "Birleşik rapor (sayfalı)",
            description = "Varsayılan: page=0, size=10, sort=title, dir=asc",
            parameters = {
                    @Parameter(name = "from", example = DATE_FROM),
                    @Parameter(name = "to",   example = DATE_TO),
                    @Parameter(name = "page", example = "0"),
                    @Parameter(name = "size", example = "10"),
                    @Parameter(name = "sort", example = "title"),
                    @Parameter(name = "dir",  example = "asc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Başarılı",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(name = "full-page", value = REPORT_FULL_PAGE)
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Forbidden",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = ERROR_FORBIDDEN)
                            )
                    )
            }
    )

    @GetMapping("/full/page")
    public ResponseEntity<Page<FullSalesReport>> fullPaged(
            @Parameter(example = "2025-12-01T00:00")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime from,
            @Parameter(example = "2025-12-31T23:59")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime to,
            @Parameter(example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(example = "title") @RequestParam(defaultValue = "title") String sort,
            @Parameter(example = "asc") @RequestParam(defaultValue = "asc") String dir
    ) {
        return ResponseEntity.ok(reportService.fullPaged(from, to, page, size, sort, dir));
    }
    @Operation(
            summary = "Birleşik rapor PDF indir",
            parameters = {
                    @Parameter(name = "from", example = DATE_FROM),
                    @Parameter(name = "to",   example = DATE_TO)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "PDF",
                            content = @Content(
                                    mediaType = "application/pdf",
                                    schema = @Schema(type = "string", format = "binary"),
                                    examples = @ExampleObject(name = "pdf-info", value = REPORT_FULL_PDF)
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Forbidden",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = ERROR_FORBIDDEN)
                            )
                    )
            }
    )
    @GetMapping(value = "/full.pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> fullPdf(
            @Parameter(example = "2025-12-01T00:00")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime from,
            @Parameter(example = "2025-12-31T23:59")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime to
    ) {
        byte[] body = reportService.fullPdf(from, to);
        String filename = reportService.fullPdfFilename(from, to);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .body(body);
    }
}
