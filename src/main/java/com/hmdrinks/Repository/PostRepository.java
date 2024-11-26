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
    List<Post> findAllByIsDeletedFalse();

    Page<Post> findAllByIsDeletedFalseOrderByPostIdDesc(Pageable pageable);
    List<Post> findAllByIsDeletedFalseOrderByPostIdDesc();

    List<Post> findByUserUserIdAndIsDeletedFalse(Integer userId);
    Page<Post> findAllByType(Type_Post typePost,Pageable pageable);
    List<Post> findAllByType(Type_Post typePost);

    Page<Post> findAllByTypeAndIsDeletedFalse(Type_Post typePost,Pageable pageable);
    List<Post> findAllByTypeAndIsDeletedFalse(Type_Post typePost);
}
