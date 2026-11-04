package com.ticketapp.security;

import com.ticketapp.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UserService userService;

    public JwtAuthFilter(JwtService jwt, UserService userService) {
        this.jwt = jwt;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                var claims = jwt.parse(token).getBody();
                String username = claims.getSubject();
                String role = String.valueOf(claims.get("role"));

                var maybeUser = userService.findByUsername(username); // Optional<User>
                if (maybeUser.isPresent()) {
                    var u = maybeUser.get();

                    // role claim'i yoksa bir default verelim (opsiyonel)
                    String effectiveRole = (role != null && !role.isBlank()) ? role : "CUSTOMER";

                    var auth = new UsernamePasswordAuthenticationToken(
                            u.getUsername(), // principal
                            null,            // credentials
                            List.of(new SimpleGrantedAuthority("ROLE_" + effectiveRole))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) { /* token hatalı/expired ise anon devam */}
        }
        chain.doFilter(req, res);
    }
}
