package com.hmdrinks.Response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TotalSearchUserResponse {
    private int currentPage;
    private long totalPage;
    private int limit;
    private int total;
    List<DetailUserResponse> detailUserResponseList;
}
