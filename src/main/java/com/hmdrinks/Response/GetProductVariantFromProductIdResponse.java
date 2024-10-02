package com.hmdrinks.Response;

import lombok.*;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GetProductVariantFromProductIdResponse {
    private int proId;
    List<CRUDProductVarResponse> responseList;
}
