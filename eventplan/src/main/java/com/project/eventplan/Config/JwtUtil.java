package com.project.eventplan.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final long AUTH_TOKEN_TTL = 1000L * 60 * 60;
    private static final long RSVP_TOKEN_TTL = 1000L * 60 * 60 * 24 * 7;
    private static final long FEEDBACK_TOKEN_TTL = 1000L * 60 * 60 * 24 * 14;

    private final String secret;

    public JwtUtil(@Value("${app.jwt.secret}") String secret) {
        this.secret = secret;
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + AUTH_TOKEN_TTL))
                .signWith(getKey())
                .compact();
    }

    public long getAuthTokenTtlSeconds() {
        return AUTH_TOKEN_TTL / 1000L;
    }

    public String generateGuestInvitationToken(Long guestId) {
        return Jwts.builder()
                .setSubject("guest-rsvp")
                .claim("guestId", guestId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + RSVP_TOKEN_TTL))
                .signWith(getKey())
                .compact();
    }

    public String generateFeedbackToken(Long guestId, Long eventId) {
        return Jwts.builder()
                .setSubject("guest-feedback")
                .claim("guestId", guestId)
                .claim("eventId", eventId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + FEEDBACK_TOKEN_TTL))
                .signWith(getKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public Long extractGuestId(String token) {
        Number guestId = extractAllClaims(token).get("guestId", Number.class);
        return guestId == null ? null : guestId.longValue();
    }

    public Long extractEventId(String token) {
        Number eventId = extractAllClaims(token).get("eventId", Number.class);
        return eventId == null ? null : eventId.longValue();
    }

    public boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return extractedEmail.equals(email) && !isTokenExpired(token);
    }

    public boolean validateGuestResponseToken(String token) {
        Claims claims = extractAllClaims(token);
        return "guest-rsvp".equals(claims.getSubject())
                && claims.get("guestId", Number.class) != null
                && !isTokenExpired(token);
    }

    public boolean validateFeedbackToken(String token) {
        Claims claims = extractAllClaims(token);
        return "guest-feedback".equals(claims.getSubject())
                && claims.get("guestId", Number.class) != null
                && claims.get("eventId", Number.class) != null
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
