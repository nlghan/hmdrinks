package com.hmdrinks.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;



@AllArgsConstructor
@Getter
public enum Locale_VN {

    VIETNAM("vn"),
    US("us"),
    ;

    private final String code;
}
