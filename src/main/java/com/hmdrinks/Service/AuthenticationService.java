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
import org.sparkproject.jetty.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.hmdrinks.Entity.Token;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private static final String GOOGLE_OAUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String SCOPE = "openid email profile";

    private static final String RESPONSE_TYPE = "code";

        @Value("${application.security.jwt.expiration}")
        private long jwtExpiration;

        public ResponseEntity<?> register(UserCreateReq userCreateReq) {
            Optional<User> userCheck = userRepository.findByUserNameAndIsDeletedFalse(userCreateReq.getUserName());
            if (userCheck.isPresent())
                return ResponseEntity.status(409).body("User name already exists");
            LocalDate currentDate1 = LocalDate.now();
            User user1 = new User();
            user1.setType(TypeLogin.BASIC);
            user1.setEmail(userCreateReq.getEmail());
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
            return ResponseEntity.status(HttpStatus.OK_200).body(AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build());
        }

    public ResponseEntity<?> authenticate(LoginBasicReq request) {
        try {
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

            return ResponseEntity.status(HttpStatus.OK_200).body(AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build());

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND_404).body(e.getMessage());
        } catch (BadCredentialsException e) {
            // Trả về mã trạng thái 401 khi thông tin xác thực không đúng
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED_401).body("Invalid username or password");
        } catch (Exception e) {
            // Trả về mã trạng thái 500 cho các lỗi khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR_500).body("An error occurred: " + e.getMessage());
        }
    }

    public ResponseEntity<?> authenticateGoogle(String email) {
        try {
            List<TypeLogin> types = Arrays.asList(TypeLogin.EMAIL, TypeLogin.BOTH);
            var user = userRepository.findByEmailAndIsDeletedFalseAndTypeIn(email, types)
                    .orElseThrow(() -> new UsernameNotFoundException("Not found email"));

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

            return ResponseEntity.status(HttpStatus.OK_200).body(AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build());

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND_404).body(e.getMessage());
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED_401).body("Invalid email");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR_500).body("An error occurred: " + e.getMessage());
        }
    }




    public ResponseEntity<?> refreshToken(
                HttpServletRequest request,
                HttpServletResponse response
        ) {
            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            final String refreshToken;
            final String userName;
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED_401).body("header is null or didnt not start with Beare");
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
                    return ResponseEntity.status(HttpStatus.OK_200).body(AuthenticationResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build());
                }
            }
            throw new MalformedJwtException("token invalid");
        }

    public String generateGoogleOAuthURL() throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder(GOOGLE_OAUTH_URL);
        url.append("?client_id=").append(URLEncoder.encode(clientId, "UTF-8"));
        url.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
        url.append("&response_type=").append(RESPONSE_TYPE);
        url.append("&scope=").append(URLEncoder.encode(SCOPE   , "UTF-8"));
        String state = "someRandomState";
        url.append("&state=").append(URLEncoder.encode(state, "UTF-8"));
        return url.toString();
    }
    }