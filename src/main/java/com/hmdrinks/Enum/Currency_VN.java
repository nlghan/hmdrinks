package com.hmdrinks.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Currency_VN {

    USD("USD"),
    VND("VND"),
    ;

    private final String value;
}

