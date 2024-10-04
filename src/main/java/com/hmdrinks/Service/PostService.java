package com.hmdrinks.Service;

import com.hmdrinks.Entity.Post;
import com.hmdrinks.Entity.User;
import com.hmdrinks.Enum.Role;
import com.hmdrinks.Repository.PostRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Request.CRUDPostReq;
import com.hmdrinks.Request.CreateNewPostReq;
import com.hmdrinks.Response.CRUDPostResponse;
import com.hmdrinks.Response.ListAllPostByUserIdResponse;
import com.hmdrinks.Response.ListAllPostResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;


    public CRUDPostResponse createPost(CreateNewPostReq req) {
       User user = userRepository.findByUserId(req.getUserId());
       if(user == null) {
           throw new RuntimeException("Not found user");
       }
       if(user.getRole() != Role.ADMIN)
       {
           throw new RuntimeException("Not allowed to add post");
       }
       Date currentDate = new Date();
       Post post = new Post();
       post.setTitle(req.getTitle());
       post.setDescription(req.getDescription());
       post.setUser(user);
       post.setBannerUrl(req.getUrl());
       post.setShortDes(req.getShortDescription());
       post.setIsDeleted(false);
       post.setDateCreate(currentDate);

       postRepository.save(post);
       return new CRUDPostResponse(
               post.getPostId(),
               post.getBannerUrl(),
               post.getDescription(),
               post.getTitle(),
               post.getShortDes(),
               post.getUser().getUserId(),
               post.getIsDeleted(),
               post.getDateDeleted(),
               post.getDateCreate()
       );
    }
    public CRUDPostResponse getPostById(int postId) {
        Post post= postRepository.findByPostId(postId);
        if(post == null) {
            throw new RuntimeException("Not found post");
        }
        return new CRUDPostResponse(
                post.getPostId(),
                post.getBannerUrl(),
                post.getDescription(),
                post.getTitle(),
                post.getShortDes(),
                post.getUser().getUserId(),
                post.getIsDeleted(),
                post.getDateDeleted(),
                post.getDateCreate()
        );
    }

    public CRUDPostResponse updatePost(CRUDPostReq req) {
        Post post = postRepository.findByPostId(req.getPostId());
        if(post == null) {
            throw new RuntimeException("Not found post");
        }
        User user = userRepository.findByUserId(req.getUserId());
        if(user == null) {
            throw new RuntimeException("Not found user");
        }
        if(user.getRole() != Role.ADMIN)
        {
            throw new RuntimeException("Not allowed to add post");
        }
        post.setTitle(req.getTitle());
        post.setDescription(req.getDescription());
        post.setShortDes(req.getShortDescription());
        post.setBannerUrl(req.getUrl());
        postRepository.save(post);
        return new CRUDPostResponse(
                post.getPostId(),
                post.getBannerUrl(),
                post.getDescription(),
                post.getTitle(),
                post.getShortDes(),
                post.getUser().getUserId(),
                post.getIsDeleted(),
                post.getDateDeleted(),
                post.getDateCreate()
        );
    }

    public ListAllPostResponse getAllPost() {
        List<Post> posts = postRepository.findAll();
        List<CRUDPostResponse> responses = new ArrayList<>();
        for(Post post : posts) {
            responses.add(new CRUDPostResponse(
                    post.getPostId(),
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
                responses
        );
    }

    public ListAllPostByUserIdResponse listAllPostByUserId(int userId) {
        User user = userRepository.findByUserId(userId);
        if(user == null) {
            throw new RuntimeException("Not found user");
        }
        List<Post> posts = postRepository.findByUserUserId(userId);
        List<CRUDPostResponse> responses = new ArrayList<>();
        for(Post post : posts) {
            responses.add(new CRUDPostResponse(
                    post.getPostId(),
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

        return new ListAllPostByUserIdResponse(userId, responses);
    }
///
    //shhss
}
