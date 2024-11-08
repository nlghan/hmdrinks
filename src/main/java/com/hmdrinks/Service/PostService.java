package com.hmdrinks.Service;

import com.hmdrinks.Entity.Post;
import com.hmdrinks.Entity.User;
import com.hmdrinks.Entity.Voucher;
import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.Status_Voucher;
import com.hmdrinks.Enum.Type_Post;
import com.hmdrinks.Repository.PostRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Repository.VoucherRepository;
import com.hmdrinks.Request.CRUDPostReq;
import com.hmdrinks.Request.CreateNewPostReq;
import com.hmdrinks.Response.CRUDPostResponse;
import com.hmdrinks.Response.ListAllPostByUserIdResponse;
import com.hmdrinks.Response.ListAllPostResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VoucherRepository voucherRepository;

    public ResponseEntity<?> createPost(CreateNewPostReq req) {
       User user = userRepository.findByUserId(req.getUserId());
       if(user == null) {
           return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user");
       }
       if(user.getRole() != Role.ADMIN)
       {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not allowed to create a post");
       }
       LocalDateTime currentDate = LocalDateTime.now();
       Post post = new Post();
       post.setTitle(req.getTitle());
       post.setDescription(req.getDescription());
       post.setUser(user);
       post.setType(req.getTypePost());
       post.setBannerUrl(req.getUrl());
       post.setShortDes(req.getShortDescription());
       post.setIsDeleted(false);
       post.setDateCreate(currentDate);
       postRepository.save(post);
       return ResponseEntity.status(HttpStatus.OK).body(new CRUDPostResponse(
               post.getPostId(),
               post.getType(),
               post.getBannerUrl(),
               post.getDescription(),
               post.getTitle(),
               post.getShortDes(),
               post.getUser().getUserId(),
               post.getIsDeleted(),
               post.getDateDeleted(),
               post.getDateCreate()
       ));
    }
    public ResponseEntity<?> getPostById(int postId) {
        Post post= postRepository.findByPostId(postId);
        if(post == null) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found post");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDPostResponse(
                post.getPostId(),
                post.getType(),
                post.getBannerUrl(),
                post.getDescription(),
                post.getTitle(),
                post.getShortDes(),
                post.getUser().getUserId(),
                post.getIsDeleted(),
                post.getDateDeleted(),
                post.getDateCreate()
        ));
    }

    public ResponseEntity<?> updatePost(CRUDPostReq req) {
        Post post = postRepository.findByPostId(req.getPostId());
        if(post == null) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found post");
        }
        User user = userRepository.findByUserId(req.getUserId());
        if(user == null) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user");
        }
        if(user.getRole() != Role.ADMIN)
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not allowed to update a post");
        }
        post.setTitle(req.getTitle());
        post.setDescription(req.getDescription());
        post.setShortDes(req.getShortDescription());
        post.setBannerUrl(req.getUrl());
        post.setType(req.getTypePost());
        postRepository.save(post);
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDPostResponse(
                post.getPostId(),
                post.getType(),
                post.getBannerUrl(),
                post.getDescription(),
                post.getTitle(),
                post.getShortDes(),
                post.getUser().getUserId(),
                post.getIsDeleted(),
                post.getDateDeleted(),
                post.getDateCreate()
        ));
    }


    public ListAllPostResponse getAllPostByType(String pageFromParam, String limitFromParam,Type_Post typePost) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Post> posts = postRepository.findAllByType(typePost,pageable);
        List<CRUDPostResponse> responses = new ArrayList<>();
        for(Post post : posts) {
            responses.add(new CRUDPostResponse(
                    post.getPostId(),
                    post.getType(),
                    post.getBannerUrl(),
                    post.getDescription(),
                    post.getTitle(),
                    post.getShortDes(),
                    post.getUser().getUserId(),
                    post.getIsDeleted(),
                    post.getDateDeleted(),
                    post.getDateCreate()
            ));
        }
        return new ListAllPostResponse(
                page,
                posts.getTotalPages(),
                limit,
                responses
        );
    }

    public ListAllPostResponse getAllPost(String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Post> posts = postRepository.findAll(pageable);
        List<CRUDPostResponse> responses = new ArrayList<>();
        for(Post post : posts) {
            responses.add(new CRUDPostResponse(
                    post.getPostId(),
                    post.getType(),
                    post.getBannerUrl(),
                    post.getDescription(),
                    post.getTitle(),
                    post.getShortDes(),
                    post.getUser().getUserId(),
                    post.getIsDeleted(),
                    post.getDateDeleted(),
                    post.getDateCreate()
            ));
        }
        return new ListAllPostResponse(
                page,
                posts.getTotalPages(),
                limit,
                responses
        );
    }

    public ResponseEntity<?> listAllPostByUserId(int userId) {
        User user = userRepository.findByUserId(userId);
        if(user == null) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user");
        }
        List<Post> posts = postRepository.findByUserUserId(userId);
        List<CRUDPostResponse> responses = new ArrayList<>();
        for(Post post : posts) {
            responses.add(new CRUDPostResponse(
                    post.getPostId(),
                    post.getType(),
                    post.getBannerUrl(),
                    post.getDescription(),
                    post.getTitle(),
                    post.getShortDes(),
                    post.getUser().getUserId(),
                    post.getIsDeleted(),
                    post.getDateDeleted(),
                    post.getDateCreate()
            ));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllPostByUserIdResponse(userId, responses));
    }

    @Transactional
    public  ResponseEntity<?> disablePost(int postId)
    {
        Post post = postRepository.findByPostId(postId);
        if(post == null) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found post");
        }
        if(post.getIsDeleted())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Post already disabled");
        }
        Voucher voucher = voucherRepository.findByPostPostId(postId);
        if(voucher!= null)
        {
            voucher.setIsDeleted(true);
            voucher.setDateDeleted(LocalDateTime.now());
            voucher.setStatus(Status_Voucher.EXPIRED);
            voucherRepository.save(voucher);
        }
        post.setIsDeleted(true);
        post.setDateDeleted(Date.valueOf(LocalDate.now()));
        postRepository.save(post);

        return ResponseEntity.status(HttpStatus.OK).body(new CRUDPostResponse(
                post.getPostId(),
                post.getType(),
                post.getBannerUrl(),
                post.getDescription(),
                post.getTitle(),
                post.getShortDes(),
                post.getUser().getUserId(),
                post.getIsDeleted(),
                post.getDateDeleted(),
                post.getDateCreate()
        ));
    }
    @Transactional
    public  ResponseEntity<?> enablePost(int postId)
    {
        Post post = postRepository.findByPostId(postId);

        if(post == null) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found post");
        }
        if(!post.getIsDeleted())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Post already enable");
        }
        Voucher voucher = voucherRepository.findByPostPostId(postId);
        if(voucher!= null)
        {
            voucher.setIsDeleted(false);
            voucher.setDateDeleted(null);
            voucher.setStatus(Status_Voucher.ACTIVE);
            voucherRepository.save(voucher);
        }
        post.setIsDeleted(false);
        post.setDateDeleted(null);
        postRepository.save(post);

        return ResponseEntity.status(HttpStatus.OK).body(new CRUDPostResponse(
                post.getPostId(),
                post.getType(),
                post.getBannerUrl(),
                post.getDescription(),
                post.getTitle(),
                post.getShortDes(),
                post.getUser().getUserId(),
                post.getIsDeleted(),
                post.getDateDeleted(),
                post.getDateCreate()
        ));
    }
}