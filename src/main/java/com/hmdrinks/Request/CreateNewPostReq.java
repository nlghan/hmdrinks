package com.hmdrinks.Request;

import lombok.*;

@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateNewPostReq {
    private String url;
    private String description;
    private String title;
    private String shortDescription;
    private int userId;
}
