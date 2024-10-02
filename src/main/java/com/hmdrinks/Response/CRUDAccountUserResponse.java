package com.hmdrinks.Response;

import com.hmdrinks.Enum.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CRUDAccountUserResponse {
    private int userId;
    private String fullName;
    private String userName;
    private String email;
    private String password;
    private String role;
}
