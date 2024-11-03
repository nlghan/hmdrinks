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
public class ListAllPaymentResponse {
    private int currentPage;
    private long totalPage;
    private int limit;
    List<CRUDPaymentResponse> listPosts;
}
