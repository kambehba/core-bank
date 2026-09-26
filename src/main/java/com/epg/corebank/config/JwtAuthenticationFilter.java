package com.epg.corebank.config;

import com.epg.corebank.Services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Reads the JWT, validates it and extracts the user name + roles
 * directly from the token (no DB round‑trip).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // -----------------------------------------------------------------
        // 1️⃣ Extract the Bearer token from the Authorization header
        // -----------------------------------------------------------------
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // -----------------------------------------------------------------
        // 2️⃣ Validate the token & make sure we have not already set auth
        // -----------------------------------------------------------------
        if (!jwtService.isTokenValid(token) ||
                SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // -----------------------------------------------------------------
        // 3️⃣ Pull the username (subject) from the token
        // -----------------------------------------------------------------
        String username = jwtService.extractUsername(token);

        // -----------------------------------------------------------------
        // 4️⃣ Extract the role strings that were stored in the token
        //    during login (JwtService.generateToken puts a "roles" claim)
        // -----------------------------------------------------------------
        List<String> tokenRoles = jwtService.extractRoles(token);

        // -----------------------------------------------------------------
        // 5️⃣ Normalise each role string:
        //     – trim whitespace
        //     – upper‑case
        //     – guarantee a leading "ROLE_" prefix
        // -----------------------------------------------------------------
        List<SimpleGrantedAuthority> authorities = tokenRoles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // -----------------------------------------------------------------
        // 6️⃣ Build the Authentication object and store it
        // -----------------------------------------------------------------
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // -----------------------------------------------------------------
        // 7️⃣ Continue the filter chain
        // -----------------------------------------------------------------
        filterChain.doFilter(request, response);
    }
}
