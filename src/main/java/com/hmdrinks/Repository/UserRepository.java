package com.hmdrinks.Repository;

import com.hmdrinks.Entity.OTP;
import com.hmdrinks.Entity.User;
import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.TypeLogin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
@Repository
 public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUserId(int userId);

    User findByUserIdAndIsDeletedFalse(int userId);

    User findByEmail(String email);
    User findByEmailAndIsDeletedFalse(String email);

    Page<User> findAll(Pageable pageable);
    List<User> findAllByIsDeletedFalse();

    @Query("SELECT u FROM User u WHERE u.role = :role")
    Page<User> findAllByRole(@Param("role") Role role, Pageable pageable);




    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findAllByRole(@Param("role") Role role);

   Page<User> findByUserNameContainingOrEmailContainingOrFullNameContainingOrStreetContainingOrDistrictContainingOrCityContainingOrPhoneNumberContaining(
           String userName,
           String email,
           String fullName,
           String street,
           String district,
           String city,
           String phoneNumber,
           Pageable pageable);

    List<User> findByUserNameContainingOrEmailContainingOrFullNameContainingOrStreetContainingOrDistrictContainingOrCityContainingOrPhoneNumberContaining(
            String userName,
            String email,
            String fullName,
            String street,
            String district,
            String city,
            String phoneNumber);

    Optional<User> findByEmailAndUserIdNot(String email, Integer userId);
    Optional<User> findByEmailAndIsDeletedFalseAndTypeIn(String username, List<TypeLogin> types);
    Optional<User> findByUserNameAndIsDeletedFalse(String username);
    Optional<User> findByPhoneNumberAndIsDeletedFalse(String phoneNumber);

}
