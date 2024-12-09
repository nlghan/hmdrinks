package com.hmdrinks.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListItemCartResponse {
    private  int cartId;
    private int total;
    List<CRUDCartItemResponse> listCartItemResponses;
}
