package com.hmdrinks.Request;

import com.hmdrinks.Enum.Size;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeSizeItemReq {
    private int userId;
    private int cartItemId;
    private Size size;

}
