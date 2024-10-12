package com.hmdrinks.Repository;

import com.hmdrinks.Entity.OTP;
import com.hmdrinks.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
@Repository
 public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUserId(int userId);

    User findByEmail(String email);

    Page<User> findAll(Pageable pageable);

   Page<User> findByUserNameContainingOrEmailContainingOrFullNameContainingOrStreetContainingOrDistrictContainingOrCityContainingOrPhoneNumberContaining(
           String userName,
           String email,
           String fullName,
           String street,
           String district,
           String city,
           String phoneNumber,
           Pageable pageable);

    Optional<User> findByEmailAndUserIdNot(String email, Integer userId);
    Optional<User> findByUserNameAndIsDeletedFalse(String username);
    Optional<User> findByPhoneNumberAndIsDeletedFalse(String phoneNumber);

}
