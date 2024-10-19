package com.hmdrinks.Response;

import com.hmdrinks.Enum.Status_Cart;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewFavouriteResponse {
    private int favId;
    private int userId;
    private Boolean isDelete;
    private LocalDate dateDeleted;
    private LocalDate dateUpdated;
    private LocalDate dateCreated;
}
