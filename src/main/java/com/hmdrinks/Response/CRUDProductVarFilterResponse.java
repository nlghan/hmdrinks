package com.hmdrinks.Response;

import com.hmdrinks.Enum.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRUDProductVarFilterResponse {
    private double avgRating;
    private int varId;
    private int proId;
    private Size size;
    private Double price;
    private int stock;
    private boolean isDeleted;
    private LocalDateTime dateDeleted;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
}
