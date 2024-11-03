package com.hmdrinks.Response;

import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Status_Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryResponse {
    private int historyId;
    private  int varId;
    private LocalDateTime dateChange;
    private Double oldPrice;
    private Double newPrice;
}
