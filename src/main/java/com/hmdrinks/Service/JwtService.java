package com.hmdrinks.Service;

import com.hmdrinks.Enum.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractNotCheckingClaim(token, claims -> claims.get("UserId", String.class));
    }

    public Date extractCreateDate(String token) {
        return extractNotCheckingClaim(token, Claims::getIssuedAt);
    }

    public <T> T extractNotCheckingClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractNotCheckingClaims(token);
        return claimsResolver.apply(claims);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails, String userId, String roles) {
        return generateToken(new HashMap<>(), userDetails, userId, roles);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            String userId,
            String roles
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration, userId, roles);
    }

    public String generateRefreshToken(
            UserDetails userDetails,
            String userId,
            String role
    ) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration, userId, role);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration,
            String userId,
            String roles
    ) {
        // Java 16 và cao hơn, hoặc sử dụng Collectors.toList() cho Java thấp hơn

        extraClaims.put("Roles", roles); // Lưu danh sách role vào claims
        extraClaims.put("UserId", userId); // Thêm UserId vào claims
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Claims extractNotCheckingClaims(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(getSignInKey())
                    .parseClaimsJws(token).getBody();
            return claims;
        } catch (ExpiredJwtException ex) {
            DefaultClaims claims = (DefaultClaims) ex.getClaims();
            return claims;
        }
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}