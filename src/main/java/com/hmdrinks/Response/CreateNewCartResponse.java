package com.hmdrinks.Response;

import com.hmdrinks.Enum.Status_Cart;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateNewCartResponse {
    private int cartId;
    private double Price;
    private int totalProduct;
    private int userId;
    private Status_Cart statusCart;
}
