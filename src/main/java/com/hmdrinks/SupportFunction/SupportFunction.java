package com.hmdrinks.SupportFunction;

import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SupportFunction {

    private final JwtService jwtService;

    // Constructor-based dependency injection
    @Autowired
    public SupportFunction(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public boolean checkRole(String role) {
        return role.equals("ADMIN") || role.equals("CUSTOMER") || role.equals("SHIPPER");
    }

    public void checkUserAuthorization(HttpServletRequest httpRequest, Long userIdFromRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Authorization header is missing or invalid");
        }

        String jwt = authHeader.substring(7);
        String userIdFromToken = jwtService.extractUserId(jwt);

        if (!String.valueOf(userIdFromRequest).equals(userIdFromToken)) {
            throw new BadRequestException("You do not have permission to perform this action");
        }
    }
}
