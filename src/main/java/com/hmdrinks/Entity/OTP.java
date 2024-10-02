package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "otp")
public class OTP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otpId")
    private int otpId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "otp", nullable = false)
    private String Otp;

    @Column(name = "time_otp", nullable = false)
    private LocalDateTime timeOtp;

    @Column(name = "status", nullable = false)
    private Boolean Status;

}
