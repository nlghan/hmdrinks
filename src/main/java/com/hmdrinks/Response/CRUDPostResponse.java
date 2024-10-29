package com.hmdrinks.Response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRUDPostResponse {
    private int postId;
    private String bannerUrl;
    private String description;
    private String title;
    private String shortDescription;
    private int userId;
    private  Boolean isDeleted;
    private Date dateDeleted;
    private LocalDateTime dateCreated;
}
