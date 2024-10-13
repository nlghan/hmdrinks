package com.hmdrinks.Response;

import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRUDProductResponse {
    private int proId;
    private int cateId;
    private String proName;
    List<ProductImageResponse> productImageResponseList;
    private String description;
    private boolean isDeleted;
    private Date dateDeleted;
    private LocalDate dateCreated;
    private LocalDate dateUpdated;
}
