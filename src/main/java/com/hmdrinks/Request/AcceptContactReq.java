package com.hmdrinks.Request;

import com.hmdrinks.Enum.Status_Contact;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class AcceptContactReq {
    private int contactId;
    private String content;
}
