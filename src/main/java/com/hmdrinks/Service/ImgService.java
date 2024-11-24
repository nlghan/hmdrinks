package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Exception.NotFoundException;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Response.ImgResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import java.io.IOException;
import java.util.Optional;

@Service
public class ImgService {
    @Value("${cloudinary.url}")
    private String cloudinaryUrl;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> uploadImgUser(MultipartFile multipartFile, int userId) throws IOException {

        if (!processFile(multipartFile)) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Incorrect formatting");
        }
        User users = userRepository.findByUserId(userId);
        if (users == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        if(users.getIsDeleted()){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("User is deleted");
        }
        Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
        cloudinary.config.secure = true;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            Map<String, Object> params = ObjectUtils.asMap(
                    "use_filename", true,
                    "unique_filename", false,
                    "overwrite", true
            );
            File tempFile = File.createTempFile("upload-", ".tmp");
            multipartFile.transferTo(tempFile);
            Map<String, Object> uploadResult = cloudinary.uploader().upload(tempFile, params);
            String imageUrl = (String) uploadResult.get("secure_url");
            ImgResponse imgResponse = new ImgResponse();
            imgResponse.setUrl(imageUrl);
            User user = userRepository.findByUserId(userId);
            if(user == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            user.setAvatar(imageUrl);
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.OK).body(imgResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new IOException("Could not upload image: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> uploadImgCategory(MultipartFile multipartFile, int cateId) throws IOException {
        if (!processFile(multipartFile)) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Incorrect formatting");
        }
        Category category = categoryRepository.findByCateId(cateId);
        if (category== null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found");
        }
        if(category.getIsDeleted()){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Category is deleted");
        }
        Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
        cloudinary.config.secure = true;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            Map<String, Object> params = ObjectUtils.asMap(
                    "use_filename", true,
                    "unique_filename", false,
                    "overwrite", true
            );
            File tempFile = File.createTempFile("upload-", ".tmp");
            multipartFile.transferTo(tempFile);
            Map<String, Object> uploadResult = cloudinary.uploader().upload(tempFile, params);
            String imageUrl = (String) uploadResult.get("secure_url");
            ImgResponse imgResponse = new ImgResponse();
            imgResponse.setUrl(imageUrl);
            category.setCateImg(imageUrl);
            categoryRepository.save(category);

            return ResponseEntity.status(HttpStatus.OK).body(imgResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new IOException("Could not upload image: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> uploadImgPost(MultipartFile multipartFile, int postId) throws IOException {
        if (!processFile(multipartFile)) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Incorrect formatting");
        }
        Post post = postRepository.findByPostId(postId);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }
        if(post.getIsDeleted()){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Post is deleted");
        }
        Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
        cloudinary.config.secure = true;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            Map<String, Object> params = ObjectUtils.asMap(
                    "use_filename", true,
                    "unique_filename", false,
                    "overwrite", true
            );
            File tempFile = File.createTempFile("upload-", ".tmp");
            multipartFile.transferTo(tempFile);
            Map<String, Object> uploadResult = cloudinary.uploader().upload(tempFile, params);
            String imageUrl = (String) uploadResult.get("secure_url");
            ImgResponse imgResponse = new ImgResponse();
            imgResponse.setUrl(imageUrl);
            post.setBannerUrl(imageUrl);
            postRepository.save(post);
            return ResponseEntity.status(HttpStatus.OK).body(imgResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new IOException("Could not upload image: " + e.getMessage()));
        }
    }

    public ImgResponse uploadImgListProduct(MultipartFile multipartFile, int proId) throws IOException {
        if (!processFile(multipartFile)) {
            throw new IOException("Incorrect formatting");
        }

        Product product = productRepository.findByProId(proId);
        if (product == null) {
            throw new IOException("Product not found");
        }

        if(product.getIsDeleted()){
            throw new IOException("Product is deleted");
        }

        Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
        cloudinary.config.secure = true;
        try {
            File tempFile = File.createTempFile("upload-", ".tmp");
            multipartFile.transferTo(tempFile);

            Map<String, Object> params = ObjectUtils.asMap(
                    "use_filename", true,
                    "unique_filename", false,
                    "overwrite", true
            );

            Map<String, Object> uploadResult = cloudinary.uploader().upload(tempFile, params);
            String imageUrl = (String) uploadResult.get("secure_url");

            // Update product images
            String currentProImg = product.getListProImg();
            String newProImgEntry = (currentProImg == null || currentProImg.trim().isEmpty())
                    ? "1: " + imageUrl
                    : currentProImg + ", " + (currentProImg.split(",").length + 1) + ": " + imageUrl;

            product.setListProImg(newProImgEntry);
            product.setDateUpdated(LocalDateTime.now());

            productRepository.save(product);

            ImgResponse imgResponse = new ImgResponse();
            imgResponse.setUrl(product.getListProImg());

            return imgResponse;

        } catch (Exception e) {
            throw new IOException("Could not upload image: " + e.getMessage());
        }
    }

    public boolean processFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();

        if (originalFilename != null) {
           if (originalFilename.endsWith(".jpg") ||
                    originalFilename.endsWith(".png") ||
                    originalFilename.endsWith(".jpeg") ||
                    originalFilename.endsWith(".raw") ||
                    originalFilename.endsWith(".psd") ||
                    originalFilename.endsWith(".tif") ||
                    originalFilename.endsWith(".tiff") ||
                    originalFilename.endsWith(".gif") ||
                    originalFilename.endsWith(".webp") ||
                    originalFilename.endsWith(".bmp") ||
                    originalFilename.endsWith(".heif") ||
                    originalFilename.endsWith(".xcf") ||
                    contentType.startsWith("image/")) {
                return true;
            }
        }
        return false;
    }
}