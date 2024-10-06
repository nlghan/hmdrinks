package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Voucher findByVoucherId(int id);

    Voucher findByVoucherIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Integer voucherId, Date currentDate,Date currentDate2);

    List<Voucher> findAll();

    Voucher findByPostPostId(int id);
}
