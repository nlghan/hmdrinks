package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Status_UserVoucher;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_voucher")
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userVoucherId")
    private int userVoucherId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucherId")
    private Voucher voucher;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status_UserVoucher status;
}
