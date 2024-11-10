package com.hmdrinks.Response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRUDProductRecommentResponse {
    private int proId;
    private double rate;
    private int cateId;
    private String proName;
    List<ProductImageResponse> productImageResponseList;
    private String description;
    private boolean isDeleted;
    private LocalDateTime dateDeleted;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
}
