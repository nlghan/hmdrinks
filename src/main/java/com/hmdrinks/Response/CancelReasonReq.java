package com.hmdrinks.Response;

import com.hmdrinks.Enum.CancelReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CancelReasonReq {
    private int userId;
    private int orderId;
    private CancelReason cancelReason;
}
