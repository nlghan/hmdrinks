package com.hmdrinks.Request;

import com.hmdrinks.Enum.Status_Contact;
import com.hmdrinks.Enum.Status_Voucher;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class CrudContactReq {
    private int contactId;
    private String email;
    private String phone;
    private String fullName;
    private String description;
    private Status_Contact statusContact;
}
