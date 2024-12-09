package com.hmdrinks.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hmdrinks.Enum.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRUDProductVarFilterBestSellResponse {
    private double avgRating;
    private long totalSell;
    private int varId;
    private Size size;
    private Double price;
    private int quantity;
    private int proId;
    private String proName;
    List<ProductImageResponse> productImageResponseList;
}
