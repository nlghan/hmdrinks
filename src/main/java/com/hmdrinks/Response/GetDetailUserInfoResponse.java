package com.hmdrinks.Response;

import lombok.*;

import java.util.Date;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetDetailUserInfoResponse {
    private Integer userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatar;
    private String sex;
    private Date birthDay;
    private String address;

}
