package com.ticketapp.service;

import com.ticketapp.entity.User;
import com.ticketapp.exception.DuplicateResourceException;
import com.ticketapp.exception.ErrorMessages;
import com.ticketapp.exception.ResourceNotFoundException;
import com.ticketapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;   // <-- ekledik

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {  // <-- ekledik
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException(ErrorMessages.USERNAME_ALREADY_USED);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = getByUsernameOr404(username);

        // 1. Eski şifre doğru mu?
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mevcut şifre yanlış!");
        }

        // 2. Yeni şifre güvenli mi? (Basit kontrol)
        if (newPassword.length() < 4) {
            throw new RuntimeException("Yeni şifre en az 4 karakter olmalı.");
        }

        // 3. Değiştir ve Kaydet
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public User getByUsernameOr404(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));
}
}