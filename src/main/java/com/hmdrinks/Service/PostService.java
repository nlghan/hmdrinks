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
import com.hmdrinks.Response.*;
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
       User user = userRepository.findByUserIdAndIsDeletedFalse(req.getUserId());
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
        Post post= postRepository.findByPostIdAndIsDeletedFalse(postId);
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
        Post post = postRepository.findByPostIdAndIsDeletedFalse(req.getPostId());
        if(post == null) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found post");
        }
        User user = userRepository.findByUserIdAndIsDeletedFalse(req.getUserId());
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


    @Transactional
    public ListAllPostResponse getAllPostByType(String pageFromParam, String limitFromParam, Type_Post typePost) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Post> posts = postRepository.findAllByTypeAndIsDeletedFalse(typePost, pageable);
        List<Post> posts1 = postRepository.findAllByTypeAndIsDeletedFalse(typePost);
        List<CRUDPostAndVoucherResponse> responses = new ArrayList<>();
        long total = posts.getTotalElements(); // Lấy tổng số bài viết

        for (Post post : posts) {
            Voucher voucher = post.getVoucher();

            // Kiểm tra nếu có voucher thì trả về thông tin voucher, nếu không có thì voucher là null
            CRUDVoucherResponse voucherResponse = (voucher != null) ? new CRUDVoucherResponse(
                    voucher.getVoucherId(),
                    voucher.getKey(),
                    voucher.getNumber(),
                    voucher.getStartDate(),
                    voucher.getEndDate(),
                    voucher.getDiscount(),
                    voucher.getStatus(),
                    voucher.getPost().getPostId()
            ) : null;

            responses.add(new CRUDPostAndVoucherResponse(
                    post.getPostId(),
                    post.getType(),
                    post.getBannerUrl(),
                    post.getDescription(),
                    post.getTitle(),
                    post.getShortDes(),
                    post.getUser().getUserId(),
                    post.getIsDeleted(),
                    post.getDateDeleted(),
                    post.getDateCreate(),
                    voucherResponse
            ));
        }

        return new ListAllPostResponse(
                page,
                posts.getTotalPages(),
                limit,
                posts1.size(), // Tổng số bài viết
                responses
        );
    }


    @Transactional
    public ListAllPostResponse getAllPostByDESC(String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Post> posts = postRepository.findAllByIsDeletedFalseOrderByPostIdDesc(pageable);
        List<Post> posts1 = postRepository.findAllByIsDeletedFalseOrderByPostIdDesc();
        List<CRUDPostAndVoucherResponse> responses = new ArrayList<>();
        int total = 0;
        for(Post post : posts) {
            Voucher voucher = post.getVoucher();

            // Kiểm tra nếu có voucher thì trả về thông tin voucher, nếu không có thì voucher là null
            CRUDVoucherResponse voucherResponse = (voucher != null) ? new CRUDVoucherResponse(
                    voucher.getVoucherId(),
                    voucher.getKey(),
                    voucher.getNumber(),
                    voucher.getStartDate(),
                    voucher.getEndDate(),
                    voucher.getDiscount(),
                    voucher.getStatus(),
                    voucher.getPost().getPostId()
            ) : null;
            responses.add(new CRUDPostAndVoucherResponse(
                    post.getPostId(),
                    post.getType(),
                    post.getBannerUrl(),
                    post.getDescription(),
                    post.getTitle(),
                    post.getShortDes(),
                    post.getUser().getUserId(),
                    post.getIsDeleted(),
                    post.getDateDeleted(),
                    post.getDateCreate(),
                    voucherResponse
            ));
            total++;
        }
        return new ListAllPostResponse(
                page,
                posts.getTotalPages(),
                limit,
                posts1.size(),
                responses
        );
    }

    @Transactional
    public ListAllPostResponse getAllPost(String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Post> posts = postRepository.findAllByIsDeletedFalse(pageable);
        List<Post> posts1 = postRepository.findAllByIsDeletedFalse();
        List<CRUDPostAndVoucherResponse> responses = new ArrayList<>();
        int total = 0;
        for(Post post : posts) {
            Voucher voucher = post.getVoucher();

            CRUDVoucherResponse voucherResponse = (voucher != null) ? new CRUDVoucherResponse(
                    voucher.getVoucherId(),
                    voucher.getKey(),
                    voucher.getNumber(),
                    voucher.getStartDate(),
                    voucher.getEndDate(),
                    voucher.getDiscount(),
                    voucher.getStatus(),
                    voucher.getPost().getPostId()
            ) : null;
            responses.add(new CRUDPostAndVoucherResponse(
                    post.getPostId(),
                    post.getType(),
                    post.getBannerUrl(),
                    post.getDescription(),
                    post.getTitle(),
                    post.getShortDes(),
                    post.getUser().getUserId(),
                    post.getIsDeleted(),
                    post.getDateDeleted(),
                    post.getDateCreate(),
                    voucherResponse
            ));
            total++;
        }
        return new ListAllPostResponse(
                page,
                posts.getTotalPages(),
                limit,
                posts1.size(),
                responses
        );
    }

    public ResponseEntity<?> listAllPostByUserId(int userId) {
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId);
        if(user == null) {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user");
        }
        List<Post> posts = postRepository.findByUserUserIdAndIsDeletedFalse(userId);
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
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllPostByUserIdResponse(userId,posts.size(), responses));
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