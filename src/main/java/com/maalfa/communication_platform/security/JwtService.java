package com.maalfa.communication_platform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Handles ONLY the access token — a short-lived, stateless, signed JWT.
 * The refresh token is deliberately NOT a JWT; it's the opaque random
 * string already modeled by the RefreshToken entity, since that's what
 * lets a single token be looked up and revoked in the DB (needed for
 * "log out this device" / "log out everywhere").
 */
@Service
public class JwtService {

    // Set these in application.properties / application.yml:
    //   app.jwt.secret=<a long, random, base64-safe string — at least 32 bytes>
    //   app.jwt.access-token-expiration-ms=900000   (15 minutes)
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration-ms:900000}")
    private long accessTokenExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(UserPrincipal userPrincipal) {
        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Returns false rather than throwing for expired/malformed/invalid-signature
    // tokens — the filter just needs a yes/no to decide whether to authenticate.
    public boolean isTokenValid(String token, UserPrincipal userPrincipal) {
        try {
            String username = extractUsername(token);
            Date expiration = extractClaim(token, Claims::getExpiration);
            return username.equals(userPrincipal.getUsername()) && expiration.after(new Date());
        } catch (ExpiredJwtException | SignatureException | IllegalArgumentException ex) {
            return false;
        }
    }
}
