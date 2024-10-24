package com.hmdrinks.Controller;

import com.hmdrinks.Request.CRUDPostReq;
import com.hmdrinks.Request.CreateNewPostReq;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.PostService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/post")
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private SupportFunction supportFunction;

    @PostMapping(value = "/create")
    public ResponseEntity<?> createPost(@RequestBody CreateNewPostReq req, HttpServletRequest httpRequest){
        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(req.getUserId()));
        return postService.createPost(req);
    }

    @GetMapping(value ="/view/{id}")
    public ResponseEntity<?> getOnePost(
            @PathVariable Integer id
    ){
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping(value = "/view/all")
    public ResponseEntity<ListAllPostResponse> getAllPosts(@RequestParam(name = "page") String page,
                                                           @RequestParam(name = "limit") String limit){
        return  ResponseEntity.ok(postService.getAllPost(page,limit));
    }

    @GetMapping(value = "/view/author/{userId}")
    public ResponseEntity<?> getOnePostByUserId(
            @PathVariable Integer userId
    ){
        return  postService.listAllPostByUserId(userId);
    }

    @PutMapping(value = "/update")
    public ResponseEntity<?> updatePost(
            @RequestBody CRUDPostReq req, HttpServletRequest httpRequest
    )
    {
        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(req.getUserId()));
        return  postService.updatePost(req);
    }
}