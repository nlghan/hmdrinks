package com.hmdrinks.Repository;

import com.hmdrinks.Entity.OTP;
import com.hmdrinks.Entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


public  interface  OtpRepository extends JpaRepository<OTP, Integer>  {
    OTP findByOtpId(Integer id);

    OTP findByUser_Email(String email);
    @Query(value = "SELECT * FROM otp  WHERE email = ?1 AND otp = ?2 AND status = 1", nativeQuery = true)
    OTP findOTP(String email, String Otp);

}
