package com.ticketapp.config;

import com.ticketapp.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer; // Lambda DSL için gerekli
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            // 1. CORS ve CSRF ayarları (Lambda DSL)
           .cors(Customizer.withDefaults())
           .csrf(AbstractHttpConfigurer::disable)

            // 2. Oturum Yönetimi (Stateless)
           .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. Yetkilendirme Kuralları (authorizeHttpRequests kullanıyoruz)
           .authorizeHttpRequests(auth -> auth
               .requestMatchers(
                    "/api/auth/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/h2-console/**",
                       "/ws-ticket/**",
                       "/ws/**",
                       "/api/tickets/events/**"
                ).permitAll()

                // Etkinlikleri okumak (GET) herkese açık
               .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()

                // Admin yetkileri
               .requestMatchers(HttpMethod.POST, "/api/events/**").hasRole("ADMIN")
               .requestMatchers(HttpMethod.PUT, "/api/events/**").hasRole("ADMIN")
               .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasRole("ADMIN")
               .requestMatchers("/api/reports/**").hasRole("ADMIN")
               .requestMatchers("/api/users/**").hasRole("ADMIN")

                // Kullanıcı yetkileri
               .requestMatchers("/api/tickets/**").authenticated()

                // Geri kalan her şey için giriş şart
               .anyRequest().authenticated()
            )

            // Hata Yönetimi
           .exceptionHandling(exception -> exception
               .accessDeniedHandler(accessDeniedHandler())
               .authenticationEntryPoint(authenticationEntryPoint())
            )

            // H2 Console için Frame ayarı
           .headers(headers -> headers.frameOptions(frame -> frame.disable()))

            // JWT Filtresini ekle
           .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (req, res, ex) -> {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"error\":\"forbidden\",\"message\":\"Bu işlem için yetkiniz yok.\"}");
        };
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (req, res, ex) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Lütfen giriş yapınız.\"}");
        };
    }
}