package com.hmdrinks.Service;
import com.hmdrinks.Entity.Post;
import com.hmdrinks.Entity.Voucher;
import com.hmdrinks.Enum.Status_Voucher;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.PostRepository;
import com.hmdrinks.Repository.VoucherRepository;
import com.hmdrinks.Request.CreateVoucherReq;
import com.hmdrinks.Request.CrudVoucherReq;
import com.hmdrinks.Response.CRUDVoucherResponse;
import com.hmdrinks.Response.ListAllVoucherResponse;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class VoucherService {
    @Autowired
    private VoucherRepository voucherRepository;
    @Autowired
    private PostRepository postRepository;

    public ResponseEntity<?> createVoucher(CreateVoucherReq req) {
        Post post = postRepository.findByPostId(req.getPostId());
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Post not found");
        }

        Voucher existingVoucher = voucherRepository.findByPostPostIdAndIsDeletedFalse(req.getPostId());
        if (existingVoucher != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Voucher Post already exists");
        }

        LocalDateTime createPostDate = post.getDateCreate();
        LocalDateTime currentDate = LocalDateTime.now();

        if (req.getStartDate().isBefore(currentDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Start date must be greater than or equal to the current date");
        }
        if (req.getEndDate().isBefore(createPostDate) || req.getEndDate().isBefore(currentDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("End date must be greater than or equal to post creation date and current date");
        }

        if (req.getStartDate().isBefore(createPostDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Start date must be greater than or equal to post creation date");
        }

        Voucher voucher = new Voucher();
        voucher.setPost(post);
        voucher.setStartDate(req.getStartDate());
        voucher.setEndDate(req.getEndDate());
        voucher.setKey(req.getKeyVoucher());
        voucher.setIsDeleted(false);
        voucher.setDiscount(req.getDiscount());
        voucher.setStatus(Status_Voucher.ACTIVE);
        voucherRepository.save(voucher);
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDVoucherResponse(
                voucher.getVoucherId(),
                voucher.getKey(),
                voucher.getStartDate(),
                voucher.getEndDate(),
                voucher.getDiscount(),
                voucher.getStatus(),
                voucher.getPost().getPostId()
        ));
    }

    public ResponseEntity<?> updateVoucher(CrudVoucherReq req){
        Voucher voucher = voucherRepository.findByVoucherIdAndIsDeletedFalse(req.getVoucherId());
        if(voucher == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Voucher not found");
        }
        if(voucher.getPost().getPostId() == req.getVoucherId())
        {
            Post post = postRepository.findByPostId(voucher.getPost().getPostId());
            if(post == null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Post not found");
            }
            LocalDateTime createPost = post.getDateCreate();
            LocalDateTime currentDate = LocalDateTime.now();

            if (req.getStartDate().isBefore(currentDate)) {
                return ResponseEntity.status(HttpStatus.valueOf(400))
                        .body("Start date must be greater than or equal to current date");
            }
            if (req.getEndDate().isBefore(createPost) || req.getEndDate().isBefore(currentDate)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("End date must be greater than start date and current date");
            }
            if (req.getStartDate().isBefore(createPost)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Start date must be greater than or equal to post creation date");
            }
            if(req.getEndDate().isBefore(createPost)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("End date must be greater than or equal to post creation date");
            }
            voucher.setPost(post);
            voucher.setStartDate(req.getStartDate());
            voucher.setEndDate(req.getEndDate());
            voucher.setDiscount(req.getDiscount());
            voucher.setKey(req.getKey());
            voucherRepository.save(voucher);
            return  ResponseEntity.status(HttpStatus.OK).body(new CRUDVoucherResponse(
                    voucher.getVoucherId(),
                    voucher.getKey(),
                    voucher.getStartDate(),
                    voucher.getEndDate(),
                    voucher.getDiscount(),
                    voucher.getStatus(),
                    voucher.getPost().getPostId()
            ));
        }
        else
        {
            Voucher vou = voucherRepository.findByPostPostIdAndIsDeletedFalse(req.getPostId());
            if(vou == null){
                throw new BadRequestException("Voucher Not found");
            }
            Post post = postRepository.findByPostId(req.getPostId());
            if(post == null){
                throw new BadRequestException("Not found post");
            }
            LocalDateTime createPost = post.getDateCreate();
            LocalDateTime currentDate = LocalDateTime.now();
            if (req.getStartDate().isBefore(currentDate)) {
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date must be greater than or equal to current date");
            }
            if (req.getEndDate().isBefore(createPost) || req.getEndDate().isBefore(currentDate)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("End date must be greater than start date and current date");
            }
            if (req.getStartDate().isBefore(createPost)) {
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date must be greater than or equal to post creation date");
            }
            if(req.getEndDate().isBefore(createPost)){
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("End date must be greater than post creation date");
            }
            voucher.setPost(post);
            voucher.setStartDate(req.getStartDate());
            voucher.setEndDate(req.getEndDate());
            voucher.setDiscount(req.getDiscount());
            voucher.setKey(req.getKey());
            voucherRepository.save(voucher);
            return  ResponseEntity.status(HttpStatus.OK).body(new CRUDVoucherResponse(
                    voucher.getVoucherId(),
                    voucher.getKey(),
                    voucher.getStartDate(),
                    voucher.getEndDate(),
                    voucher.getDiscount(),
                    voucher.getStatus(),
                    voucher.getPost().getPostId()
            ));
        }
    }

    public ResponseEntity<?> getVoucherById(int voucherId){
        Voucher voucher = voucherRepository.findByVoucherIdAndIsDeletedFalse(voucherId);
        if(voucher == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voucher not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CRUDVoucherResponse(
                voucher.getVoucherId(),
                voucher.getKey(),
                voucher.getStartDate(),
                voucher.getEndDate(),
                voucher.getDiscount(),
                voucher.getStatus(),
                voucher.getPost().getPostId()
        ));
    }
    public ResponseEntity<?> listAllVoucher()
    {
        List<Voucher> voucherList = voucherRepository.findAll();
        List<CRUDVoucherResponse> crudVoucherResponses = new ArrayList<>();
        for(Voucher voucher : voucherList){
            crudVoucherResponses.add(new CRUDVoucherResponse(
                    voucher.getVoucherId(),
                    voucher.getKey(),
                    voucher.getStartDate(),
                    voucher.getEndDate(),
                    voucher.getDiscount(),
                    voucher.getStatus(),
                    voucher.getPost().getPostId()
            ));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ListAllVoucherResponse(crudVoucherResponses));
    }
}