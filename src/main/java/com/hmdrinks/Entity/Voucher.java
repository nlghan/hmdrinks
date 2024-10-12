package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Size;
import com.hmdrinks.Enum.Status_Voucher;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucherId",columnDefinition = "BIGINT")
    private Integer voucherId;

    @Column(name = "startDate",nullable = false)
    private Date startDate;

    @Column(name = "discount",nullable = false)
    private Double discount;

    @Column(name = "endDate",nullable = false)
    private Date endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status" ,nullable = false)
    private Status_Voucher status;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "date_deleted")
    private Date dateDeleted;

    @OneToOne
    @JoinColumn(name = "postId", nullable = false)
    private Post post;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserVoucher> userVouchers;

    @OneToMany(mappedBy = "voucher",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orders> orders;

}
