package com.hmdrinks.Response;

import com.hmdrinks.Enum.Status_Voucher;
import lombok.*;

import java.util.Date;
@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRUDVoucherResponse {
    private int voucherId;
    private Date startDate;
    private Date endDate;
    private Double discount;
    private Status_Voucher status;
    private int postId;
}
