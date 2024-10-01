package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Sex;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "userinfo")  // Đổi tên bảng thành user_info cho phù hợp với thông lệ
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userInfoId")
    private int userInfoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex")
    private Sex sex;

    @Column(name = "fullName")
    private String fullName;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @Column(name = "birthDate")
    private Date birthDate;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "city")
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "street")
    private String street;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "date_deleted")
    private Date dateDeleted;

    // OneToOne: Mỗi UserInfo thuộc về một User
    @OneToOne
    @JoinColumn(name = "userId", nullable = false)  // userId trong bảng user_info là khóa ngoại
    private User user;  // Đổi tên biến từ userId thành user
}
