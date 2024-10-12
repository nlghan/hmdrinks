package com.hmdrinks.Service;

import com.hmdrinks.Entity.*;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Exception.NotFoundException;
import com.hmdrinks.Repository.*;
import com.hmdrinks.Response.ImgResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
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

    public ImgResponse uploadImgUser(MultipartFile multipartFile, int userId) throws IOException {

        if (!processFile(multipartFile)) {
            throw new BadRequestException("Incorrect formatting");
        }


        User users = userRepository.findByUserId(userId);
        if (users == null) {
            throw new NotFoundException("Not found userId");
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
                throw  new RuntimeException("Khong ton tai userId");
            }
            user.setAvatar(imageUrl);
            userRepository.save(user);
            return imgResponse;

        } catch (Exception e) {
            System.out.println("Error uploading image: " + e.getMessage());
            throw new IOException("Could not upload image: " + e.getMessage());
        }
    }

    public ImgResponse uploadImgCategory(MultipartFile multipartFile, int cateId) throws IOException {

        if (!processFile(multipartFile)) {
            throw new BadRequestException("Incorrect formatting");
        }


        Category category = categoryRepository.findByCateId(cateId);
        if (category== null) {
            throw new NotFoundException("Not found cateId");
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
            return imgResponse;

        } catch (Exception e) {
            System.out.println("Error uploading image: " + e.getMessage());
            throw new IOException("Could not upload image: " + e.getMessage());
        }
    }

    public ImgResponse uploadImgPost(MultipartFile multipartFile, int postId) throws IOException {

        if (!processFile(multipartFile)) {
            throw new BadRequestException("Incorrect formatting");
        }


        Post post = postRepository.findByPostId(postId);


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
            return imgResponse;

        } catch (Exception e) {
            System.out.println("Error uploading image: " + e.getMessage());
            throw new IOException("Could not upload image: " + e.getMessage());
        }
    }

//    public ImgResponse uploadImgProduct(MultipartFile multipartFile, int proId) throws IOException {
//
//        if (!processFile(multipartFile)) {
//            throw new BadRequestException("Incorrect formatting");
//        }
//
//
//        Product product = productRepository.findByProId(proId);
//        if (product == null) {
//            throw new NotFoundException("Not found proId");
//        }
//
//
//        Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
//        cloudinary.config.secure = true;
//
//        try {
//            InputStream inputStream = multipartFile.getInputStream();
//            Map<String, Object> params = ObjectUtils.asMap(
//                    "use_filename", true,
//                    "unique_filename", false,
//                    "overwrite", true
//            );
//            File tempFile = File.createTempFile("upload-", ".tmp");
//            multipartFile.transferTo(tempFile);
//            Map<String, Object> uploadResult = cloudinary.uploader().upload(tempFile, params);
//            String imageUrl = (String) uploadResult.get("secure_url");
//            ImgResponse imgResponse = new ImgResponse();
//            imgResponse.setUrl(imageUrl);
//            product.setProImg(imageUrl);
//            productRepository.save(product);
//            return imgResponse;
//
//        } catch (Exception e) {
//            System.out.println("Error uploading image: " + e.getMessage());
//            throw new IOException("Could not upload image: " + e.getMessage());
//        }
//    }

    public ImgResponse uploadImgListProduct(MultipartFile multipartFile, int proId) throws IOException {

        if (!processFile(multipartFile)) {
            throw new BadRequestException("Incorrect formatting");
        }


        Product product = productRepository.findByProId(proId);
        if (product == null) {
            throw new NotFoundException("Not found proId");
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
            if (!product.getListProImg().isEmpty() || product.getListProImg() == null) {
                String currentProImg = product.getListProImg();
                if (currentProImg.trim().isEmpty()) {
                    product.setListProImg("1: " + imageUrl);
                } else {
                    // Nếu không rỗng, tiếp tục xử lý
                    int currentStt = currentProImg.split(",").length;
                    int newStt = currentStt + 1;
                    product.setDateUpdated(LocalDate.now());
                    String newProImg = currentProImg + ", " + newStt + ": " + imageUrl;
                    product.setListProImg(newProImg);
                }
            } else {
                product.setListProImg("1: " + imageUrl);
            }

            productRepository.save(product);
            imgResponse.setUrl(product.getListProImg());
            return imgResponse;

        } catch (Exception e) {
            System.out.println("Error uploading image: " + e.getMessage());
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

