package com.hmdrinks.Service;

import com.hmdrinks.Entity.Post;
import com.hmdrinks.Entity.ProductImage;
import com.hmdrinks.Entity.User;
import com.hmdrinks.Enum.Role;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Exception.NotFoundException;
import com.hmdrinks.Repository.PostRepository;
import com.hmdrinks.Repository.ProductImageRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Request.CRUDPostReq;
import com.hmdrinks.Request.CreateNewPostReq;
import com.hmdrinks.Response.CRUDPostResponse;
import com.hmdrinks.Response.ImgResponse;
import com.hmdrinks.Response.ListAllPostByUserIdResponse;
import com.hmdrinks.Response.ListAllPostResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ProductImageService {
    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private UserRepository userRepository;


    public ImgResponse deleteImageFromProduct(int proId, int deleteStt) {
        ProductImage productImage = productImageRepository.findByProduct_ProId(proId);
        if (productImage == null) {
            throw new NotFoundException("Not found product with ID: " + proId);
        }
        String currentProImg = productImage.getProImg();
        if (currentProImg == null || currentProImg.isEmpty()) {
            throw new BadRequestException("No images found for this product.");
        }
        String[] imageEntries = currentProImg.split(", ");
        List<String> updatedImageEntries = new ArrayList<>();
        int currentStt = 1;
        for (String imageEntry : imageEntries) {
            String[] parts = imageEntry.split(": ");  // Phân tách stt và url
            int stt = Integer.parseInt(parts[0]);      // Lấy số thứ tự hiện tại
            String url = parts[1];                     // Lấy URL

            if (stt == deleteStt) {
                continue;
            }
            updatedImageEntries.add(currentStt + ": " + url);
            currentStt++;
        }
        String updatedProImg = String.join(", ", updatedImageEntries);
        productImage.setProImg(updatedProImg);
        productImage.setDateUpdated(LocalDate.now());
        productImageRepository.save(productImage);

        return new  ImgResponse(productImage.getProImg());
    }

    public ImgResponse deleteAllImageFromProduct(int proId) {
        ProductImage productImage = productImageRepository.findByProduct_ProId(proId);
        if (productImage == null) {
            throw new NotFoundException("Not found product with ID: " + proId);
        }
        productImage.setProImg("");
        productImage.setDateUpdated(LocalDate.now());
        productImageRepository.save(productImage);
        return new  ImgResponse(productImage.getProImg());
    }


}
