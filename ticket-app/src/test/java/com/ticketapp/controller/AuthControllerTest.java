package com.ticketapp.controller;

import com.ticketapp.entity.User;
import com.ticketapp.security.JwtService;
import com.ticketapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false) // Security filtrelerini testte kapatıyoruz
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    UserService userService;

    @MockBean
    PasswordEncoder passwordEncoder;

    @MockBean
    JwtService jwtService;

    // SecurityConfig içindeki filter bean'i için mock
    @MockBean
    com.ticketapp.security.JwtAuthFilter jwtAuthFilter;

    /**
     * 1) Kayıt mutlu senaryo:
     *    POST /api/auth/register
     *    Geçerli JSON gelince UserService.registerUser çağrılıyor ve
     *    "Kayıt başarılı" mesajı dönüyor mu?
     */
    @Test
    void register_validRequest_returns200AndMessage() throws Exception {
        // GIVEN - registerUser çağrıldığında herhangi bir User dönsün (body'yi kullanmıyoruz)
        Mockito.when(userService.registerUser(any(User.class)))
                .thenReturn(new User());

        String body = """
                {
                  "username": "mehmet",
                  "password": "1234",
                  "email": "m@example.com",
                  "role": "CUSTOMER"
                }
                """;

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("Kayıt başarılı"));
    }

    /**
     * 2) Login mutlu senaryo:
     *    Kullanıcı bulunuyor + passwordEncoder.matches true
     *    → 200 ve { "token": "fake-jwt" } dönmeli.
     */
    @Test
    void login_validCredentials_returnsToken() throws Exception {
        // GIVEN - veritabanında var olan kullanıcı
        User user = new User();
        user.setId(1L);
        user.setUsername("mehmet");
        user.setPassword("ENCODED"); // şifre encodeli hali
        user.setRole("ADMIN");

        Mockito.when(userService.findByUsername("mehmet"))
                .thenReturn(Optional.of(user));

        Mockito.when(passwordEncoder.matches("1234", "ENCODED"))
                .thenReturn(true);

        Mockito.when(jwtService.generate("mehmet", "ADMIN"))
                .thenReturn("fake-jwt-token");

        String body = """
                {
                  "username": "mehmet",
                  "password": "1234"
                }
                """;

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    /**
     * 3) Login - kullanıcı yok:
     *    userService.findByUsername boş Optional dönerse
     *    → 401 ve "Kullanıcı bulunamadı" body dönmeli.
     */
    @Test
    void login_userNotFound_returns401() throws Exception {
        Mockito.when(userService.findByUsername("mehmet"))
                .thenReturn(Optional.empty());

        String body = """
                {
                  "username": "mehmet",
                  "password": "1234"
                }
                """;

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Kullanıcı bulunamadı"));
    }

    /**
     * 4) Login - şifre yanlış:
     *    Kullanıcı var ama passwordEncoder.matches false
     *    → 401 ve "Şifre hatalı" body dönmeli.
     */
    @Test
    void login_wrongPassword_returns401() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("mehmet");
        user.setPassword("ENCODED");
        user.setRole("ADMIN");

        Mockito.when(userService.findByUsername("mehmet"))
                .thenReturn(Optional.of(user));

        Mockito.when(passwordEncoder.matches("yanlis", "ENCODED"))
                .thenReturn(false);

        String body = """
                {
                  "username": "mehmet",
                  "password": "yanlis"
                }
                """;

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Şifre hatalı"));
    }

    /**
     * 5) Login - eksik username/password:
     *    username ya da password boş string ise
     *    → 400 ve "username ve password zorunlu" dönmeli.
     */
    @Test
    void login_blankUsername_returns400() throws Exception {
        String body = """
                {
                  "username": "",
                  "password": "1234"
                }
                """;

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("username ve password zorunlu"));
    }
}
