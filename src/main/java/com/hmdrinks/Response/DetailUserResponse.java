package com.hmdrinks.Response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DetailUserResponse {
    private Integer userId;
    private String userName;
    private String fullName;
    private Boolean isDelete;
    private String role;
}
