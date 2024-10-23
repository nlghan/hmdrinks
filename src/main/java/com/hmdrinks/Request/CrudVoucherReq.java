package com.hmdrinks.Request;

import com.hmdrinks.Enum.Status_Voucher;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class CrudVoucherReq {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int voucherId;
    private String key;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double discount;
    private Status_Voucher status;
    private int postId;
}
