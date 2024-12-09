package com.hmdrinks.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListItemFavouriteResponse {
    private int favId;
    private int total;
    List<CRUDFavouriteItemResponse> favouriteItemResponseList;
}
