package com.hmdrinks.Config;

import com.hmdrinks.Entity.Token;
import com.hmdrinks.Repository.TokenRepository;
import com.hmdrinks.Service.JwtService;
import com.hmdrinks.Service.UserInfoService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;




import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserInfoService myUserDetailsService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getServletPath().contains("/api/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        String userEmail = null;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        String msg = new String();
        int customStatus = HttpServletResponse.SC_UNAUTHORIZED;
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (ExpiredJwtException ex) {
            msg = "Expired JWT token";
            customStatus = 410; // Đặt mã trạng thái 410 cho token hết hạn
        } catch (SignatureException ex) {
            msg = "Invalid JWT signature";
        } catch (MalformedJwtException ex) {
            msg = "Invalid JWT token";
        } catch (UnsupportedJwtException ex) {
            msg = "Unsupported JWT token";
        } catch (IllegalArgumentException ex) {
            msg = "JWT claims string is empty.";
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;
            try {
                userDetails = this.myUserDetailsService.loadUserByUsername(userEmail);
            } catch (UsernameNotFoundException ex) {
                msg = "JWT token not exist";
            }
            if (userDetails != null) {
                var isTokenValid = tokenRepository.findByAccessToken(jwt)
                        .map(t -> {
                            // Giả sử t.getExpire() trả về Date
                            Date expiredDateTime = t.getExpire();
                            Instant expiredInstant = expiredDateTime.toInstant();
                            Instant extendedExpiryTime = expiredInstant.plusSeconds(90000000);
                            Instant now = Instant.now();
                            return now.isBefore(extendedExpiryTime);
                        })
                        .orElse(false);


                if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    msg = "JWT token is not found";
                }
            }
        }
        if (!msg.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            sb.append("\"error\": \"Unauthorized\",");
            sb.append("\"message\": \"" + msg + "\",");
            sb.append("\"path\": \"")
                    .append(request.getRequestURL())
                    .append("\"");
            sb.append("} ");
            response.setContentType("application/json");
            response.setStatus(customStatus);
            response.getWriter().write(sb.toString());
            return;
        }
        filterChain.doFilter(request, response);
    }
}
