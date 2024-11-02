package com.hmdrinks.Response;



import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private LocalDateTime dateDeleted;
}

