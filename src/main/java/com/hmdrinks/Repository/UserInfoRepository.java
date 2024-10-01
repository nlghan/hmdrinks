package com.hmdrinks.Repository;

import com.hmdrinks.Entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.OptionalInt;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
     UserInfo findByUserUserId(Integer userId);
}
