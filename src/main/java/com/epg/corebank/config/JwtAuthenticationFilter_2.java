package com.epg.corebank.config;

import com.epg.corebank.Repositories.UserRepository;
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

@Component
public class JwtAuthenticationFilter_2 extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter_2(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (jwtService.isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = jwtService.extractUsername(token);
            List<String> tokenRoles = jwtService.extractRoles(token);
//            List<SimpleGrantedAuthority> authorities = tokenRoles.stream()
//                    .filter(Objects::nonNull)
//                    .map(String::trim)
//                    .map(String::toUpperCase)
//                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
//                    .map(SimpleGrantedAuthority::new)
//                    .toList();

            userRepository.findWithRolesByUsername(username).ifPresent(user -> {
                List<SimpleGrantedAuthority> authorities = user.getUserRoles()
                        .stream()
                        .map(userRole -> userRole.getRole().getName())
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .map(String::toUpperCase)               // e.g. "ADMIN"
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role) // ensure prefix
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // <<< DEBUG LOG >>> -------------------------------------------------
                System.out.println(">>> AUTHENTICATED USER = " + username +
                        " | AUTHORITIES = " + authorities);
                // ------------------------------------------------------------------

                if (authorities.isEmpty()) {
                    userRepository.findWithRolesByUsername(username).ifPresent(u -> {
                        List<SimpleGrantedAuthority> dbAuth = u.getUserRoles()
                                .stream()
                                .map(ur -> ur.getRole().getName())
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .map(String::toUpperCase)
                                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                                .map(SimpleGrantedAuthority::new)
                                .toList();
                        authorities.addAll(dbAuth);
                    });
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user.getUsername(),
                                null,
                                authorities
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }
}