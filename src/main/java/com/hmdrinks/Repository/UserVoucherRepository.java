package com.hmdrinks.Repository;

import com.hmdrinks.Entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserVoucherRepository  extends JpaRepository<UserVoucher,Integer> {
    List<UserVoucher> findByUserUserId(int userId);

    UserVoucher findByUserUserIdAndVoucherVoucherId(int userId,int voucherId);
}
