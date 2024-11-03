package com.hmdrinks.Response;

import com.hmdrinks.Enum.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRUDProductVarResponse {
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
