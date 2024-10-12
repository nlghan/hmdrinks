package com.hmdrinks.Service;

import com.hmdrinks.Entity.MyUserDetails;
import com.hmdrinks.Entity.User;
import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.Sex;
import com.hmdrinks.Enum.TypeLogin;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Exception.ConflictException;
import com.hmdrinks.Exception.NotFoundException;
import com.hmdrinks.Repository.TokenRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Request.LoginBasicReq;
import com.hmdrinks.Request.UserCreateReq;
import com.hmdrinks.Response.AuthenticationResponse;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.hmdrinks.Entity.Token;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthenticationService {
        private final UserRepository userRepository;
        private final UserInfoService myUserDetailsService;
        private final TokenRepository tokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        @Value("${application.security.jwt.expiration}")
        private long jwtExpiration;


        public AuthenticationResponse register(UserCreateReq userCreateReq) {
            Optional<User> userCheck = userRepository.findByUserNameAndIsDeletedFalse(userCreateReq.getUserName());
            if (userCheck.isPresent())
                throw new ConflictException("User name already exists");

            LocalDate currentDate1 = LocalDate.now();
            User user1 = new User();
            user1.setType(TypeLogin.BASIC);
            user1.setEmail("None");
            user1.setRole(Role.CUSTOMER);
            user1.setIsDeleted(false);
            user1.setUserName(userCreateReq.getUserName());
            user1.setAvatar("None");
            user1.setDistrict("None");
            user1.setCity("None");
            user1.setStreet("None");
            user1.setSex(Sex.OTHER);
            user1.setDateCreated(java.sql.Date.valueOf(currentDate1));
            user1.setPhoneNumber("None");
            user1.setPassword(passwordEncoder.encode(userCreateReq.getPassword()));
            user1.setFullName(userCreateReq.getFullName());
            userRepository.save(user1);

            Token token = new Token();
            MyUserDetails myUserDetails = myUserDetailsService.createMyUserDetails(user1);
            Date currentDate = new Date();

            var jwtToken = jwtService.generateToken(myUserDetails, String.valueOf(user1.getUserId()), user1.getRole().toString());
            var refreshToken = jwtService.generateRefreshToken(myUserDetails, String.valueOf(user1.getUserId()), user1.getRole().toString());

            token.setAccessToken(jwtToken);
            token.setRefreshToken(refreshToken);
            token.setExpire(currentDate);
            token.setUser(user1);

            tokenRepository.save(token);


            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();
        }

    public AuthenticationResponse authenticate(LoginBasicReq request) {
        var user = userRepository.findByUserNameAndIsDeletedFalse(request.getUserName())
                .orElseThrow(() -> new UsernameNotFoundException("Not found user name"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword())
        );

        Token token = tokenRepository.findByUserUserId(user.getUserId());

        if (token == null) {
            token = new Token();
            token.setUser(user);
        }

        MyUserDetails myUserDetails = myUserDetailsService.createMyUserDetails(user);
        Date currentDate = new Date();

        var jwtToken = jwtService.generateToken(myUserDetails, String.valueOf(user.getUserId()), user.getRole().toString());
        var refreshToken = jwtService.generateRefreshToken(myUserDetails, String.valueOf(user.getUserId()), user.getRole().toString());

        token.setAccessToken(jwtToken);
        token.setRefreshToken(refreshToken);
        token.setExpire(currentDate);

        tokenRepository.save(token);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }


    public AuthenticationResponse refreshToken(
                HttpServletRequest request,
                HttpServletResponse response
        ) {
            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            final String refreshToken;
            final String userName;
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new BadRequestException("header is null or didnt not start with Bearer");
            }
            refreshToken = authHeader.substring(7);
            userName = jwtService.extractUsername(refreshToken);


            if (userName != null) {
                var user = this.userRepository.findByUserNameAndIsDeletedFalse(userName)
                        .orElseThrow(() -> new NotFoundException("REFRESH token not found or expired"));

                MyUserDetails myUserDetails = myUserDetailsService.createMyUserDetails(user);
                Date currentDate = new Date();
                Token token = tokenRepository.findByRefreshToken(refreshToken).orElseThrow(() -> new NotFoundException("REFRESH token not found or expired "));

                if (jwtService.isTokenValid(refreshToken, myUserDetails)) {
                    var accessToken = jwtService.generateToken(myUserDetails, String.valueOf(user.getUserId()), user.getRole().toString());
                    token.setAccessToken(accessToken);
                    token.setExpire(currentDate);
                    tokenRepository.save(token);
                    return AuthenticationResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build();
                }

            }
            throw new MalformedJwtException("token invalid");
        }



    }
