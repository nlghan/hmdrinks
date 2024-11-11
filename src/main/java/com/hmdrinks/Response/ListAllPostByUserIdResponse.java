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
public class ListAllPostByUserIdResponse {
    private  int userId;
    private int total;
    List<CRUDPostResponse> listPosts;
}
