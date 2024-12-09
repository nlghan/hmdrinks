package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    Optional<Token> findByAccessToken(String accessToken);

    Token findByUserUserId(Integer userId);

    Optional<Token> findByRefreshToken(String refreshToken);

}
