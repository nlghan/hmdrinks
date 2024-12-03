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
public class CRUDProductVarFilterResponse {
    private double avgRating;
    private int proId;
    private String proName;
    List<ProductImageResponse> productImageResponseList;
    private boolean isDeleted;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateDeleted;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateCreated;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateUpdated;
    List<CRUDProductVarResponse> listProductVariants;
}
