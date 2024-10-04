package com.hmdrinks.Entity;

import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.TypeLogin;
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
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private int userId;

    @Column(name = "username", nullable = false)
    private String userName;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "date_deleted")
    private Date dateDeleted;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TypeLogin type;

    // OneToOne: Một User có một UserInfo
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)  // mappedBy để ánh xạ với thuộc tính user bên UserInfo
    private UserInfo userInfo;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)  // mappedBy để ánh xạ với thuộc tính user bên UserInfo
    private Token token;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private  List<Post> posts;


}
