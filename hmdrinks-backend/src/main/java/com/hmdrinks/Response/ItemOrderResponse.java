package com.hmdrinks.Response;

import com.hmdrinks.Enum.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ItemOrderResponse {
    private  int cartItemId;
    private  int cartId;
    private  int proId;
    private String proName;
    private Size size;
    private double priceItem;
    private double totalPrice;
    private int quantity;
}
