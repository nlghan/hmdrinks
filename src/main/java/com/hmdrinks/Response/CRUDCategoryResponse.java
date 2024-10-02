package com.hmdrinks.Response;



import lombok.*;

@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRUDCategoryResponse {
    private int cateId;
    private String cateName;
    private String cateImg;
}

