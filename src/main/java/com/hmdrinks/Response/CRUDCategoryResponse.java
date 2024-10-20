package com.hmdrinks.Response;



import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRUDCategoryResponse {
    private int cateId;
    private String cateName;
    private String cateImg;
    private Boolean isDeleted;
    private LocalDate dateCreated;
    private LocalDate dateUpdated;
    private LocalDate dateDeleted;
}

