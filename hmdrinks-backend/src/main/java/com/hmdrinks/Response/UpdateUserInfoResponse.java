package com.hmdrinks.Response;

import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.Sex;
import com.hmdrinks.Enum.TypeLogin;
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
    private String userName;
    private String fullName;
    private String avatar;
    private Date birth_date;
    private String address;
    private String email;
    private String phone;
    private String sex;
    private String type;
    private Boolean isDelete;
    private Date dateDeleted;
    private Date dateUpdated;
    private Date dateCreated;
    private String role;
}
