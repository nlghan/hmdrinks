package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Voucher findByVoucherIdAndIsDeletedFalse(int voucherId);
    List<Voucher> findByIsDeletedFalse();
    Voucher findByPostPostIdAndIsDeletedFalse(int postId);
}
