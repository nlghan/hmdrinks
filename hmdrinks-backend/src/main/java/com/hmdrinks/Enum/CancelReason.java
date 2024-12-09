package com.hmdrinks.Enum;

import lombok.Getter;

@Getter
public enum CancelReason {
    CHANGED_MY_MIND,         // Đổi ý không muốn mua nữa
    FOUND_CHEAPER_ELSEWHERE, // Tìm thấy giá rẻ hơn ở nơi khác
    ORDERED_BY_MISTAKE,      // Đặt nhầm sản phẩm
    DELIVERY_TOO_SLOW,       // Giao hàng quá chậm
    WRONG_PRODUCT_SELECTED,  // Chọn nhầm sản phẩm
    NOT_NEEDED_ANYMORE,      // Không cần sản phẩm nữa
    PAYMENT_ISSUES,          // Gặp vấn đề khi thanh toán
    PREFER_DIFFERENT_STORE,  // Thích mua ở cửa hàng khác hơn
    UNSATISFIED_WITH_SERVICE,// Không hài lòng với dịch vụ
    OTHER_REASON             // Lý do khác
}
