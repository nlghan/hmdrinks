package com.hmdrinks.Response;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CRUDReviewResponse {
    private int reviewId;
    private int userId;
    private int proId;
    private String fullName;
    private String content;
    private int ratingStart;
    private Boolean isDelete;
    private LocalDateTime dateDeleted;
    private LocalDateTime dateUpdated;
    private LocalDateTime dateCreated;

    public CRUDReviewResponse(String reviewNotFound) {

    }
}
