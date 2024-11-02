package com.hmdrinks.Response;

import com.hmdrinks.Enum.Status_Cart;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewFavouriteResponse {
    private int favId;
    private int userId;
    private Boolean isDelete;
    private LocalDateTime dateDeleted;
    private LocalDateTime dateUpdated;
    private LocalDateTime dateCreated;
}
