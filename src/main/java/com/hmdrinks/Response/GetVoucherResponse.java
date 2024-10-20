package com.hmdrinks.Response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class GetVoucherResponse {
    private  int id;
    private  int userId;
    private int voucherId;
    private String status;
}

