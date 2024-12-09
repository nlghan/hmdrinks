package com.hmdrinks.Response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ListAllHistoryOrderResponse {
    private int userId;
    private int total;
    List<HistoryOrderResponse> list;
}
