package com.hmdrinks.Response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ListAllCancelReasonResponse {
    private int total;
    List<CancelReasonResponse> list;
}
