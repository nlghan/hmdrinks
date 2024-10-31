package com.hmdrinks.SupportFunction;

import com.hmdrinks.Entity.User;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Exception.ConflictException;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.Optional;


@Component
public class SupportFunction {
    private final JwtService jwtService;
    @Autowired
    public SupportFunction(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public boolean checkRole(String role) {
        return role.equals("ADMIN") || role.equals("CUSTOMER") || role.equals("SHIPPER");
    }

    public ResponseEntity<?> checkUserAuthorization(HttpServletRequest httpRequest, int userIdFromRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header is missing or invalid");
        }
        String jwt = authHeader.substring(7);
        String userIdFromTokenStr = jwtService.extractUserId(jwt);
        int userIdFromToken;
        try {
            userIdFromToken = Integer.parseInt(userIdFromTokenStr);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID format in token");
        }

        if (userIdFromRequest != userIdFromToken) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to perform this action");
        }
        else
        {
            return ResponseEntity.status(HttpStatus.OK).body("You have successfully logged in");
        }

    }

    public ResponseEntity<?> checkPhoneNumber(String phoneNumber, Integer userId, UserRepository userRepository) {
        if (phoneNumber == null || phoneNumber.length() != 10) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Số điện thoại không hợp lệ. Phải chứa 10 chữ số.");
        }

        Optional<User> existingUserOptional = userRepository.findByPhoneNumberAndIsDeletedFalse(phoneNumber);
        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get(); // Lấy user từ Optional
            if (!(existingUser.getUserId() ==userId)) {
                throw new ConflictException("Số điện thoại đã tồn tại.");
            }
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
