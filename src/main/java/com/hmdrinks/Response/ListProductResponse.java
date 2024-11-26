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
public class ListProductResponse {
    private int currentPage;
    private long totalPage;
    private int limit;
    private int toal;
    List<CRUDProductResponse> productResponses;
}
