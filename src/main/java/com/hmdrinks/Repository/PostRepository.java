package com.hmdrinks.Repository;

import com.hmdrinks.Entity.Post;
import com.hmdrinks.Enum.Type_Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface PostRepository extends JpaRepository<Post, Integer> {
    Post findByPostId(int postId);
    Post findByPostIdAndIsDeletedFalse(int postId);
    List<Post> findAll();
    Page<Post> findAll(Pageable pageable);
    Page<Post> findAllByIsDeletedFalse(Pageable pageable);

    List<Post> findByUserUserIdAndIsDeletedFalse(Integer userId);
    Page<Post> findAllByType(Type_Post typePost,Pageable pageable);
    Page<Post> findAllByTypeAndIsDeletedFalse(Type_Post typePost,Pageable pageable);
}
