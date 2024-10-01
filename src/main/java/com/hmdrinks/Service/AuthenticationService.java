package com.hmdrinks.Service;

import com.hmdrinks.Entity.MyUserDetails;
import com.hmdrinks.Entity.User;
import com.hmdrinks.Entity.UserInfo;
import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.TypeLogin;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Exception.ConflictException;
import com.hmdrinks.Exception.NotFoundException;
import com.hmdrinks.Repository.TokenRepository;
import com.hmdrinks.Repository.UserInfoRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Request.LoginBasicReq;
import com.hmdrinks.Request.UserCreateReq;
import com.hmdrinks.Response.AuthenticationResponse;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.DoubleStream;


@Service
@RequiredArgsConstructor
public class AuthenticationService {
        private final UserRepository userRepository;
        private final UserInfoService myUserDetailsService;
        private final TokenRepository tokenRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final UserInfoRepository userInfoRepository;

        @Value("${application.security.jwt.expiration}")
        private long jwtExpiration;


        public AuthenticationResponse register(UserCreateReq userCreateReq) {
            Optional<User> userCheck = userRepository.findByUserNameAndIsDeletedFalse(userCreateReq.getUserName());
            if (userCheck.isPresent())
                throw new ConflictException("User name already exists");
            User user = new User();
            user.setType(TypeLogin.BASIC);
            user.setEmail("");
            user.setRole(Role.ADMIN);
            user.setIsDeleted(false);
            user.setUserName(userCreateReq.getUserName());
            user.setPassword(passwordEncoder.encode(userCreateReq.getPassword()));

            userRepository.save(user);
            UserInfo userInfo = new UserInfo();

            userInfo.setFullName(userCreateReq.getFullName());

            userInfo.setUser(user);

            var savedUser = userInfoRepository.save(userInfo);

            Token token = new Token();
            MyUserDetails myUserDetails = myUserDetailsService.createMyUserDetails(user);
            Date currentDate = new Date();

            var jwtToken = jwtService.generateToken(myUserDetails, String.valueOf(user.getUserId()), user.getRole().toString());
            var refreshToken = jwtService.generateRefreshToken(myUserDetails, String.valueOf(user.getUserId()), user.getRole().toString());

            token.setAccessToken(jwtToken);
            token.setRefreshToken(refreshToken);
            token.setExpire(currentDate);
            token.setUser(user);

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
