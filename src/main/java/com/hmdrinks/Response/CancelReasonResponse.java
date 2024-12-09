package com.hmdrinks.Response;

import com.hmdrinks.Enum.CancelReason;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CancelReasonResponse {
    private int userId;
    private int orderId;
    private CancelReason cancelReason;
}
