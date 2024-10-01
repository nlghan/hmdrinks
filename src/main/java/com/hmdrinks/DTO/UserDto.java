package com.hmdrinks.DTO;

import com.hmdrinks.Enum.Role;
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

public class UserDto {
    private int userId;
    private String userName;
    private String email;
    private Role role;
    private boolean isDeleted;
    private Date dateDeleted;
    private String password;
    private TypeLogin type;


}
