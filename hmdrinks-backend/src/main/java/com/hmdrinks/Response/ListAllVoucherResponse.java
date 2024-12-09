package com.hmdrinks.Response;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ListAllVoucherResponse {
    private int total;
    List<CRUDVoucherResponse> voucherResponseList;
}
