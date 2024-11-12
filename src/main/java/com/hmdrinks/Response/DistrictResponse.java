package com.hmdrinks.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import scala.Int;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class DistrictResponse {
    private int districtId;
    private String districtName;
}
