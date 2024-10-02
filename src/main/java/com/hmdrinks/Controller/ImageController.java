package com.hmdrinks.Controller;

import com.hmdrinks.Service.ImgService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/image")
public class ImageController {

    @Autowired
    private ImgService imgService;

    @PostMapping(value = "/user/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> handleUploadUserImage(@RequestParam("file") MultipartFile file, @RequestParam("userId") Integer id) throws IOException {
        return ResponseEntity.ok(imgService.uploadImgUser(file,id));
    }

}
