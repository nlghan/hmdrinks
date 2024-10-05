package com.hmdrinks.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordReq {
    private Integer userId;
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;
}
