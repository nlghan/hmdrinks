package com.hmdrinks.Enum;



import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResponseCode {

    BOOKING_NOT_FOUND("BOOKING_NOT_FOUND", 404),
    VNPAY_SIGNING_FAILED("VNPAY_SIGNING_FAILED", 400),
    VNPAY_CHECKSUM_FAILED("VNPAY_CHECKSUM_FAILED", 400),
    ;
    private final String type;
    private final Integer code;
}