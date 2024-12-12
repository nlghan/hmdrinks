package com.hmdrinks.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateReq {
    private  String fullName;
    private String userName;
    private String password;
    private String email;
}
