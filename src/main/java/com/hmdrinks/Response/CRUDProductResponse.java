package com.hmdrinks.Response;

import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRUDProductResponse {
    private int proId;
    private int cateId;
    private String proName;
    private String proImg;
    private String description;
    private boolean isDeleted;
    private Date dateDeleted;
    private LocalDate dateCreated;
    private LocalDate dateUpdated;
}
