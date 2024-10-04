package com.hmdrinks.Response;

import lombok.*;

import java.util.Date;

@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRUDPostResponse {
    private int postId;
    private String url;
    private String description;
    private String title;
    private String shortDescription;
    private int userId;
    private  Boolean isDeleted;
    private Date dateDeleted;
    private Date dateCreated;
}
