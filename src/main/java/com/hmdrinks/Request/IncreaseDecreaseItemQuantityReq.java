package com.hmdrinks.Request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class IncreaseDecreaseItemQuantityReq {
    private int userId;
    private int cartItemId;
    private int quantity;

}
