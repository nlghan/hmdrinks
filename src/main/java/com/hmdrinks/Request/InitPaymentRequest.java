package com.hmdrinks.Request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class InitPaymentRequest {
    private String requestId;

    private String ipAddress;

    private Long userId;

    private String txnRef;

    private long amount;

}
