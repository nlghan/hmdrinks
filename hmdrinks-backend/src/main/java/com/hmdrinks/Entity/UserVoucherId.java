package com.hmdrinks.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserVoucherId implements Serializable {

    @Column(name = "userId")
    private Integer userId;

    @Column(name = "postId")
    private Integer postId;

    // Constructors
    public UserVoucherId() {}

    public UserVoucherId(Integer userId, Integer postId) {
        this.userId = userId;
        this.postId = postId;
    }

    // Getters và Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    // Override equals và hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserVoucherId that = (UserVoucherId) o;

        if (!Objects.equals(userId, that.userId)) return false;
        return Objects.equals(postId, that.postId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, postId);
    }
}
