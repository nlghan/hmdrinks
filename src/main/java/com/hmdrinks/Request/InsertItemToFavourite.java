package com.hmdrinks.Request;

import com.hmdrinks.Enum.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsertItemToFavourite {
    private int userId;
    private int favId;
    private int proId;
    private Size size;
}
