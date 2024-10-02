package com.hmdrinks.Request;


import com.hmdrinks.Enum.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRUDProductVarReq {
    private int varId;
    private int proId;
    private Size size;
    private Double price;
    private int stock;
}
