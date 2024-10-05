package com.hmdrinks.Response;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class ListAllVoucherUserIdResponse {
    List<GetVoucherResponse> getVoucherResponseList;
}

