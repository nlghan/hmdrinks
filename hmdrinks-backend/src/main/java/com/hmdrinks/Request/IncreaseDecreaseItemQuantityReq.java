package com.hmdrinks.Request;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncreaseDecreaseItemQuantityReq {
    private int userId;
    private int cartItemId;
    private int quantity;

}
