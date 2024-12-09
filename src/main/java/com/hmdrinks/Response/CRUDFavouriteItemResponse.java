package com.hmdrinks.Response;

import com.hmdrinks.Enum.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CRUDFavouriteItemResponse {
    private  int favItemId;
    private  int favId;
    private  int proId;
    private  Size size;
}
