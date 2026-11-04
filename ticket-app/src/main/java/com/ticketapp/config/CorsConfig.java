package com.ticketapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Frontend hangi origin’den çalışıyorsa onu buraya yazıyoruz
        config.setAllowedOriginPatterns(List.of("http://localhost:*"));

        // Hangi HTTP method’larına izin verelim
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Hangi header’lara izin verelim
        config.setAllowedHeaders(List.of("*"));

        // Response’a hangi header’lar eklenebilir
        config.setExposedHeaders(List.of("Authorization"));

        // Kimlik bilgisi (cookie / Authorization header) taşınmasına izin ver
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Bütün endpoint’ler için bu CORS ayarı geçerli olsun
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
