package com.hmdrinks.Controller;

import com.hmdrinks.Enum.Type_Post;
import com.hmdrinks.Request.CRUDPostReq;
import com.hmdrinks.Request.CreateNewPostReq;
import com.hmdrinks.Request.IdReq;
import com.hmdrinks.Response.*;
import com.hmdrinks.Service.PostService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
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

    @GetMapping(value = "/view/all/desc")
    public ResponseEntity<ListAllPostResponse> getAllPostsDESC(@RequestParam(name = "page") String page,
                                                           @RequestParam(name = "limit") String limit){
        return  ResponseEntity.ok(postService.getAllPostByDESC(page,limit));
    }

    @GetMapping(value = "/view/type/all")
    public ResponseEntity<ListAllPostResponse> getAllPostsByTye(@RequestParam(name = "page") String page,
                                                                @RequestParam(name = "limit") String limit,
                                                                @RequestParam(name = "type")Type_Post typePost){
        return  ResponseEntity.ok(postService.getAllPostByType(page,limit,typePost));
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
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, req.getUserId());

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return  postService.updatePost(req);
    }

    @PutMapping(value = "/enable")
    public ResponseEntity<?> enablePost(@RequestBody IdReq req, HttpServletRequest httpRequest) {
        return  postService.enablePost(req.getId());
    }

    @PutMapping(value = "/disable")
    public ResponseEntity<?> disablePost(@RequestBody IdReq req, HttpServletRequest httpRequest) {
        return  postService.disablePost(req.getId());
    }
}