package com.hmdrinks.Repository;

import com.hmdrinks.Entity.OTP;
import com.hmdrinks.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUserId(int userId);

    User findByEmail(String email);

    Optional<User> findByEmailAndUserIdNot(String email, Integer userId);
    Optional<User> findByUserNameAndIsDeletedFalse(String username);

}
