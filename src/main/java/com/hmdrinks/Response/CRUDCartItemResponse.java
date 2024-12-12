package com.hmdrinks.Response;

import com.hmdrinks.Enum.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CRUDCartItemResponse {
    private  int cartItemId;
    private  int proId;
    private String proName;
    private  int cartId;
    private Size size;
    private double totalPrice;
    private  int quantity;
}
