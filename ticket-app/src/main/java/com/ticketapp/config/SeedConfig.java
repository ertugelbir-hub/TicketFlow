package com.ticketapp.config;

import com.ticketapp.entity.User;
import com.ticketapp.repository.UserRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedConfig {

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepo, PasswordEncoder encoder) {
        return args -> {
            // ADMIN yoksa oluştur
            if (userRepo.findByUsername("admin").isEmpty()) {
                User u = new User();
                u.setUsername("admin");
                u.setPassword(encoder.encode("admin123")); // giriş şifresi
                u.setEmail("admin@example.com");
                u.setRole("ADMIN");                       // kritik: token’da ROLE_ADMIN olsun
                userRepo.save(u);
                /*System.out.println("Seed -> admin/admin123 oluşturuldu.");*/

            }
            // USER yoksa oluştur
            if (userRepo.findByUsername("ayse").isEmpty()) {
                User u1 = new User();
                u1.setUsername("ayse");
                u1.setPassword(encoder.encode("ayse123")); // giriş şifresi
                u1.setEmail("ayse@example.com");
                u1.setRole("USER");                       // kritik: token’da ROLE_USER olsun
                userRepo.save(u1);
                /*System.out.println("Seed -> ayse/ayse123 oluşturuldu.");*/

            }
            // USER2 yoksa oluştur
            if (userRepo.findByUsername("ahmet").isEmpty()) {
                User u2 = new User();
                u2.setUsername("ahmet");
                u2.setPassword(encoder.encode("ahmet123")); // giriş şifresi
                u2.setEmail("ahmet@example.com");
                u2.setRole("USER");                       // kritik: token’da ROLE_USER olsun
                userRepo.save(u2);
                /*System.out.println("Seed -> ahmet/ahmet123 oluşturuldu.");*/

            }
        };

    }
}
