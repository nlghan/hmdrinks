package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Payment_Method;
import com.hmdrinks.Enum.Type_Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postId",columnDefinition = "BIGINT")
    private Integer postId;

    @Column(name = "bannerUrl", nullable = false)
    private String bannerUrl;

    @Column(name = "title")
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type_Post type;

    @Lob
    @Column(name = "description",columnDefinition = "TEXT")
    private  String description;

    @Column(name = "date_create",nullable = false)
    private LocalDateTime dateCreate;

    @Column(name = "shortDes", nullable = false)
    private String shortDes;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "date_deleted")
    private Date dateDeleted;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL)  // mappedBy để ánh xạ với thuộc tính user bên UserInfo
    private Voucher voucher;
}
