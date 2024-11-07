package com.hmdrinks.Response;

import com.hmdrinks.Enum.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class FilterProductBoxResponse {
     private boolean isShowFilterMonopoly;
     private int total;
     List<CRUDProductVarFilterResponse> productResponseList;
     private String textResponseAPI;
     private boolean isFilterRange;
}
