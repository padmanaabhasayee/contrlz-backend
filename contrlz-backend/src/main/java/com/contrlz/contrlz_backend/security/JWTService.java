package com.contrlz.contrlz_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
public class JWTService {

    private static SecretKey getKey() {
        String secretKey = "Contrlz_SSSIHL_ThisIsASecretKey_2025";
        String base64Key = Base64.getEncoder().encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
        return Keys.hmacShaKeyFor(base64Key.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(String username, String role, long expirationTime) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getKey())
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return (extractUserName(token).equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        return claimResolver.apply(Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload());
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
