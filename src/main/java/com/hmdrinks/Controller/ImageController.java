package com.hmdrinks.Controller;

import com.hmdrinks.Response.ImgResponse;
import com.hmdrinks.Service.ImgService;
import com.hmdrinks.Service.JwtService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/image")
public class ImageController {
    @Autowired
    private ImgService imgService;
    @Autowired
    private SupportFunction supportFunction;

    @PostMapping(value = "/product-image/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImgResponse> handleUploadProductImagesFull(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("proId") Integer id,
            HttpServletRequest httpRequest) {
            ImgResponse uploadResults = null;
        try {
            for (MultipartFile file : files) {
                ImgResponse result = imgService.uploadImgListProduct(file, id);
                uploadResults = result;
            }
        } catch (IOException e) {

        }
        return ResponseEntity.ok(uploadResults);
    }

    @Autowired
    private JwtService jwtService;

    @PostMapping(value = "/user/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> handleUploadUserImage(@RequestParam("file") MultipartFile file, @RequestParam("userId") Integer id, HttpServletRequest httpRequest) throws IOException {
        supportFunction.checkUserAuthorization(httpRequest,Long.valueOf(id));
        return ResponseEntity.ok(imgService.uploadImgUser(file,id));
    }

    @PostMapping(value = "/cate/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> handleUploadCategoryImage(@RequestParam("file") MultipartFile file, @RequestParam("cateId") Integer id) throws IOException {
        return ResponseEntity.ok(imgService.uploadImgCategory(file,id));
    }

    @PostMapping(value = "/post/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> handleUploadPostImage(@RequestParam("file") MultipartFile file, @RequestParam("postId") Integer id) throws IOException {
        return ResponseEntity.ok(imgService.uploadImgPost(file,id));
    }
}