package com.ticketapp.config;

import io.github.bucket4j.BucketConfiguration; // Bunu ekle
import io.github.bucket4j.ConsumptionProbe;      // Bunu ekle
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Profile("!test")
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private ProxyManager<byte[]> proxyManager;

    @Autowired
    private RateLimitConfig config;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Kullanıcının IP adresini alıyoruz ve byte dizisine çeviriyoruz
        String ip = request.getRemoteAddr();
        byte[] key = ip.getBytes(StandardCharsets.UTF_8);

        // Kovayı getirmek yerine kurallarını alıyoruz
        BucketConfiguration bucketConfig = config.bucketConfiguration();

        // tryConsume yerine durum raporunu (probe) üretiyoruz
        ConsumptionProbe probe = proxyManager.builder().build(key, bucketConfig).tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {

            filterChain.doFilter(request, response);
        } else {
            // Nanosaniyeyi saniyeye çeviriyoruz
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000L;
            if (waitForRefill == 0) waitForRefill = 1;


            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Credentials", "true");


            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");

            // JSON içerisine dinamik retryAfter saniyesini gömüyoruz:
            String jsonResponse = String.format("{\"message\":\"rateLimit\",\"retryAfter\":%d}", waitForRefill);
            response.getWriter().write(jsonResponse);
        }
    }
}