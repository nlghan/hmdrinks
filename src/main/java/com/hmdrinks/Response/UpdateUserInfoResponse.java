package com.hmdrinks.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoResponse {
    private Integer userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatar;
    private String sex;
    private Date birthDay;
    private String address;

}
