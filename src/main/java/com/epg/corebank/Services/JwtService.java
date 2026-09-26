package com.epg.corebank.Services;

import com.epg.corebank.Models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Generates a signed JWT that contains:
     *   - sub      : username
     *   - userId   : database id
     *   - email    : e‑mail address
     *   - roles    : List<String> (e.g. ["ROLE_ADMIN"])
     *   - iat / exp timestamps
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtExpirationMs);

        // ---- 1️⃣ Build the list of role strings that we want to embed ----
        List<String> roles = user.getUserRoles()
                .stream()
                .map(ur -> ur.getRole().getName())
                .map(String::toUpperCase)                     // "ADMIN"
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r) // ensure prefix
                .toList();

        // ---- 2️⃣ Put everything into a claims map -----------------------
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);                       // <-- THIS WAS MISSING
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());

        // ---- 3️⃣ Build the JWT -----------------------------------------
        return Jwts.builder()
                .claims(claims)               // <-- add the map to the token
                .subject(user.getUsername())  // "sub" claim
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Returns the list of role strings that were stored in the token
     * (e.g. ["ROLE_ADMIN","ROLE_USER"]).
     */
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObject = claims.get("roles");

        if (rolesObject instanceof List<?> rawList) {
            // Convert whatever objects are inside the list to String
            return rawList.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }

        // The claim could have been stored as a single string instead of an array
        if (rolesObject instanceof String single) {
            return List.of(single);
        }

        // No roles found → return empty list (will cause 403)
        return List.of();
    }
}
