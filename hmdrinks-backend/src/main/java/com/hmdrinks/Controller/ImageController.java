package com.hmdrinks.Controller;

import com.hmdrinks.Response.ImgResponse;
import com.hmdrinks.Service.ImgService;
import com.hmdrinks.Service.JwtService;
import com.hmdrinks.SupportFunction.SupportFunction;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
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
    public ResponseEntity<?> handleUploadProductImagesFull(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("proId") Integer id,
            HttpServletRequest httpRequest) {

        List<ImgResponse> uploadResults = new ArrayList<>();  // Danh sách để lưu kết quả tải lên
        List<String> failedUploads = new ArrayList<>();        // Danh sách lưu lại các tệp tải lên thất bại

        for (MultipartFile file : files) {
            try {
                ImgResponse result = imgService.uploadImgListProduct(file, id);
                uploadResults.add(result);
            } catch (IOException e) {
                failedUploads.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        if (uploadResults.isEmpty() && !failedUploads.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("All uploads failed: " + String.join(", ", failedUploads));
        } else if (!failedUploads.isEmpty()) {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body("Some files failed to upload: " + String.join(", ", failedUploads));
        }
        return ResponseEntity.ok(uploadResults);
    }

    @Autowired
    private JwtService jwtService;

    @PostMapping(value = "/user/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> handleUploadUserImage(@RequestParam("file") MultipartFile file, @RequestParam("userId") Integer id, HttpServletRequest httpRequest) throws IOException {
        ResponseEntity<?> authResponse = supportFunction.checkUserAuthorization(httpRequest, id);

        if (!authResponse.getStatusCode().equals(HttpStatus.OK)) {
            return authResponse;
        }
        return imgService.uploadImgUser(file,id);
    }

    @PostMapping(value = "/cate/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> handleUploadCategoryImage(@RequestParam("file") MultipartFile file, @RequestParam("cateId") Integer id) throws IOException {
        return imgService.uploadImgCategory(file,id);
    }

    @PostMapping(value = "/post/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> handleUploadPostImage(@RequestParam("file") MultipartFile file, @RequestParam("postId") Integer id) throws IOException {
        return imgService.uploadImgPost(file,id);
    }
}