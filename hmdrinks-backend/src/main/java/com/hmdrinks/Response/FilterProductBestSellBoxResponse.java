package com.hmdrinks.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class FilterProductBestSellBoxResponse {
     private int currentPage;
     private long totalPages;
     private int limit;
     private int total;
     List<CRUDProductVarFilterBestSellResponse> productResponseList;
}
