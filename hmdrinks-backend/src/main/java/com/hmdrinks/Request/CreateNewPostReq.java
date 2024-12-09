package com.hmdrinks.Request;

import com.hmdrinks.Enum.Type_Post;
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
    private Type_Post typePost;
    private int userId;
}
