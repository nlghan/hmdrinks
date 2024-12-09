package com.hmdrinks.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "token")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tokenId",columnDefinition = "BIGINT")
    private Integer tokenId;

    @Column(name = "accessToken", nullable = false)
    private String accessToken;

    @Column(name = "expire")
    private Date expire;

    @Column(name = "refreshToken", nullable = false)
    private String refreshToken;

    @OneToOne
    @JoinColumn(name = "userId", nullable = false)  // userId trong bảng token là khóa ngoại
    private User user;  // Đổi tên biến từ userId thành user
}
