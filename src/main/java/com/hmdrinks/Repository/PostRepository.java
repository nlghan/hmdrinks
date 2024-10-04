package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PostRepository extends JpaRepository<Post, Integer> {
    Post findByPostId(int postId);
    List<Post> findAll();
    List<Post> findByUserUserId(Integer userId);
}
