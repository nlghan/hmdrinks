package com.hmdrinks.Config;

import com.hmdrinks.Service.LogoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final MyAccessDeniedHandler myAccessDeniedHandler;
    private final LogoutService logoutService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/swagger-ui.html"

                        ).permitAll()
                        .requestMatchers("/api/fav-item/list").permitAll()
                        .requestMatchers("/api/orders/list-cancel-reason").hasAuthority("ADMIN")
                        .requestMatchers("/api/orders/reason-cancel/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/product/recommended/**").permitAll()
                        .requestMatchers("/api/payment/callback").permitAll()
                        .requestMatchers("/api/v1/auth/social-login/google").permitAll()
                        .requestMatchers("/api/orders/pdf/**").permitAll()
                        .requestMatchers("/api/orders/view/fetchOrdersAwaiting","/api/orders/view/order-cancel/payment-not","/api/orders/view/order-cancel/payment-not").hasAnyAuthority("ADMIN","CUSTOMER")
                        .requestMatchers("/api/payment/zalo/callback").permitAll()
                        .requestMatchers("/api/product/recommended/**").permitAll()
                        .requestMatchers("/api/payment/vnpay_ipn").permitAll()
                        .requestMatchers("/api/product/enable","/api/product/disable").hasAuthority("ADMIN")
                        .requestMatchers("/api/user/enable","/api/user/disable").hasAuthority("ADMIN")
                        .requestMatchers("/api/post/enable","/api/post/disable").hasAuthority("ADMIN")
                        .requestMatchers("/api/cate/enable","/api/cate/disable").hasAuthority("ADMIN")
                        .requestMatchers("/api/voucher/enable","/api/voucher/disable").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/auth/oauth2/callback").permitAll()
                        .requestMatchers("/api/price-history/**").permitAll()
                        .requestMatchers("/api/province/**").permitAll()
                        .requestMatchers("/api/shipment/update-time").hasAnyAuthority("ADMIN","SHIPPER")
                        .requestMatchers("/api/shipment/allocate").hasAuthority("ADMIN")
                        .requestMatchers("/api/shipment/activate/**").hasAnyAuthority("SHIPPER","ADMIN")
                        .requestMatchers("/api/shipment/shipper/**").hasAnyAuthority("SHIPPER","ADMIN")
                        .requestMatchers("/api/shipment/check-time").hasAnyAuthority("ADMIN","SHIPPER","CUSTOMER")
                        .requestMatchers("/api/shipment/view/**").hasAnyAuthority("ADMIN","SHIPPER","CUSTOMER")
                        .requestMatchers("/api/payment/listAll","/api/payment/listAll-method","/api/payment/listAll-status").hasAuthority("ADMIN")
                        .requestMatchers("/api/payment/create/cash","/api/payment/create/credit").hasAnyAuthority("ADMIN", "CUSTOMER","SHIPPER")
                        .requestMatchers("/api/v1/auth/authenticate", "/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/product/view/**", "/api/product/list-product","/api/product/variants/**").permitAll()
                        .requestMatchers("/api/cate/view/**", "/api/cate/list-category").permitAll()
                        .requestMatchers("/api/product/list-review").permitAll()
                        .requestMatchers("/api/post/view/**").permitAll()
                        .requestMatchers("/api/product/list-rating").permitAll()
                        .requestMatchers("/api/report/**").permitAll()
                        .requestMatchers("/api/product/filter-product").permitAll()
                        .requestMatchers("/api/product/search","/api/cate/search").permitAll()
                        .requestMatchers("/api/voucher/view/**").permitAll()
                        .requestMatchers("/api/user-voucher/view-all/**").permitAll()
                        .requestMatchers("/api/user-voucher/**").hasAnyAuthority("ADMIN", "CUSTOMER","SHIPPER")
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/contact/view/**").hasAnyAuthority("ADMIN")
                        .requestMatchers("/api/contact/response").hasAuthority("ADMIN")
                        .requestMatchers("/api/contact/**").hasAnyAuthority("ADMIN", "CUSTOMER","SHIPPER")
                        .requestMatchers("/api/user/**").hasAnyAuthority("ADMIN", "CUSTOMER","SHIPPER")
                        .requestMatchers("/api/image/user/**").hasAnyAuthority("ADMIN", "CUSTOMER","SHIPPER")
                        .requestMatchers("/api/image/**").hasAnyAuthority("ADMIN")
                        .requestMatchers("/api/cate/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/product/**").hasAnyAuthority("ADMIN")
                        .requestMatchers("/api/voucher/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/post/**").hasAnyAuthority("ADMIN")
                        .requestMatchers("/api/productVar/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/cart/**").hasAnyAuthority("ADMIN","CUSTOMER","SHIPPER")
                        .requestMatchers("/api/cart-item/**").hasAnyAuthority("ADMIN","CUSTOMER","SHIPPER")
                        .requestMatchers("/api/fav/**").hasAnyAuthority("ADMIN","CUSTOMER","SHIPPER")
                        .requestMatchers("/api/review/**").hasAnyAuthority("ADMIN","CUSTOMER","SHIPPER")
                        .requestMatchers("/api/fav-item/**").hasAnyAuthority("ADMIN","CUSTOMER","SHIPPER")
                        .requestMatchers("/api/orders/**").hasAnyAuthority("ADMIN","CUSTOMER","SHIPPER")
                        .requestMatchers("api/orders/reason-cancel").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception.accessDeniedHandler(myAccessDeniedHandler))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(logoutService)
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext()));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(Arrays.asList("Authorization", "content-type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
