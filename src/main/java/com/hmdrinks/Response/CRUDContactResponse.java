package com.hmdrinks.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hmdrinks.Enum.Status_Contact;
import com.hmdrinks.Enum.Status_Voucher;
import lombok.*;
import org.apache.spark.util.collection.PrimitiveVector;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CRUDContactResponse {
    private int contactId;
    private  int userId;
    private String description;
    private Status_Contact status;
    private Boolean isDeleted;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private LocalDateTime deleteDate;
}
