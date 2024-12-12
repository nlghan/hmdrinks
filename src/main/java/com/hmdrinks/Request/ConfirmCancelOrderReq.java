package com.hmdrinks.Request;

import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class ConfirmCancelOrderReq {
    private Integer userId;
    private Integer orderId;
}
