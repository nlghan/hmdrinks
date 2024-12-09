package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Status_Contact;
import com.hmdrinks.Enum.Status_Voucher;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contact")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contactId",columnDefinition = "BIGINT")
    private Integer contactId;

    @Column(name = "description")
    private String description;

    @Column(name = "fullName")
    private String fullName;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "createDate",nullable = false,columnDefinition = "DATETIME")
    private LocalDateTime createDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status" ,nullable = false)
    private Status_Contact status;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "date_deleted",columnDefinition = "DATETIME")
    private LocalDateTime dateDeleted;

    @Column(name = "date_updated",columnDefinition = "DATETIME")
    private LocalDateTime updateDate;

}
