package com.hmdrinks.Request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductReq {
    private int cateId;
    private String proName;
    private String proImg;
    private String description;
}
