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
public class TotalSearchShipmentResponse {
    private int currentPage;
    private long totalPage;
    private int limit;
    private List<CRUDShipmentResponse> listShipment;
}
