package com.manacommunity.parking.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final SecretKey key;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public UserPrincipal getUserPrincipal(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Long id = claims.get("id", Long.class) != null ? claims.get("id", Long.class) : (claims.get("userId", Long.class) != null ? claims.get("userId", Long.class) : 1L);
        Long communityId = claims.get("communityId", Long.class);
        String email = claims.getSubject();
        String name = claims.get("name", String.class);
        List<String> roles = claims.get("roles", List.class);

        return UserPrincipal.builder()
                .id(id)
                .email(email)
                .fullName(name)
                .communityId(communityId)
                .roles(roles != null ? roles : List.of("RESIDENT"))
                .build();
    }
}
