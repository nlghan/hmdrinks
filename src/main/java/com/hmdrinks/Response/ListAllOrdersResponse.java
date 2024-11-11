package com.hmdrinks.Response;

import com.hmdrinks.Enum.Status_Order;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListAllOrdersResponse {
    private int currentPage;
    private long totalPage;
    private int limit;
    private int total;
    private  int userId;
    List<CreateOrdersResponse> listOrders;
}
