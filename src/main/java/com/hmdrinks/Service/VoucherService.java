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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class VoucherService {
    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private PostRepository postRepository;

    public CRUDVoucherResponse createVoucher(CreateVoucherReq req){
        Post post = postRepository.findByPostId(req.getPostId());
        if(post == null){
            throw new BadRequestException("Not found post");
        }
        Voucher vou = voucherRepository.findByPostPostId(req.getPostId());
        if(vou != null){
            throw new BadRequestException("Voucher Post already exists");
        }

        Date createPost = post.getDateCreate();
        Date currentDate = new Date();

        if (req.getStartDate().before(currentDate)) {
            throw new BadRequestException("Start date must be greater than or equal to current date");
        }
        if (req.getEndDate().before(createPost) || req.getEndDate().before(currentDate)) {
            throw new BadRequestException("End date must be greater than start date and current date");
        }
        if (req.getStartDate().before(createPost)) {
            throw new BadRequestException("Start date must be greater than or equal to post creation date");
        }
        if(req.getEndDate().before(createPost)){
            throw new BadRequestException("End date must be greater than or equal to post creation date");
        }

        Voucher voucher = new Voucher();
        voucher.setPost(post);
        voucher.setStartDate(req.getStartDate());
        voucher.setEndDate(req.getEndDate());
        voucher.setIsDeleted(false);
        voucher.setDiscount(req.getDiscount());
        voucher.setStatus(Status_Voucher.ACTIVE);
        voucherRepository.save(voucher);
        return new CRUDVoucherResponse(
               voucher.getVoucherId(),
               voucher.getStartDate(),
                voucher.getEndDate(),
                voucher.getDiscount(),
                voucher.getStatus(),
                voucher.getPost().getPostId()
        );
    }

    public CRUDVoucherResponse updateVoucher(CrudVoucherReq req){
        Voucher voucher = voucherRepository.findByVoucherId(req.getVoucherId());
        if(voucher == null){
            throw new BadRequestException("Not found voucher");
        }
        if(voucher.getPost().getPostId() == req.getVoucherId())
        {
            Post post = postRepository.findByPostId(voucher.getPost().getPostId());
            if(post == null){
                throw new BadRequestException("Not found post");
            }


            Date createPost = post.getDateCreate();
            Date currentDate = new Date();

            if (req.getStartDate().before(currentDate)) {
                throw new BadRequestException("Start date must be greater than or equal to current date");
            }
            if (req.getEndDate().before(createPost) || req.getEndDate().before(currentDate)) {
                throw new BadRequestException("End date must be greater than start date and current date");
            }
            if (req.getStartDate().before(createPost)) {
                throw new BadRequestException("Start date must be greater than or equal to post creation date");
            }
            if(req.getEndDate().before(createPost)){
                throw new BadRequestException("End date must be greater than or equal to post creation date");
            }
            voucher.setPost(post);
            voucher.setStartDate(req.getStartDate());
            voucher.setEndDate(req.getEndDate());
            voucher.setDiscount(req.getDiscount());
            return new CRUDVoucherResponse(
                    voucher.getVoucherId(),
                    voucher.getStartDate(),
                    voucher.getEndDate(),
                    voucher.getDiscount(),
                    voucher.getStatus(),
                    voucher.getPost().getPostId()
            );
        }
        else
        {
            Voucher vou = voucherRepository.findByPostPostId(req.getPostId());
            if(vou != null){
                throw new BadRequestException("Voucher Post already exists");
            }
            Post post = postRepository.findByPostId(req.getPostId());
            if(post == null){
                throw new BadRequestException("Not found post");
            }
            Date createPost = post.getDateCreate();
            Date currentDate = new Date();

            if (req.getStartDate().before(currentDate)) {
                throw new BadRequestException("Start date must be greater than or equal to current date");
            }
            if (req.getEndDate().before(createPost) || req.getEndDate().before(currentDate)) {
                throw new BadRequestException("End date must be greater than start date and current date");
            }
            if (req.getStartDate().before(createPost)) {
                throw new BadRequestException("Start date must be greater than or equal to post creation date");
            }
            if(req.getEndDate().before(createPost)){
                throw new BadRequestException("End date must be greater than or equal to post creation date");
            }
            voucher.setPost(post);
            voucher.setStartDate(req.getStartDate());
            voucher.setEndDate(req.getEndDate());
            voucher.setDiscount(req.getDiscount());

            return new CRUDVoucherResponse(
                    voucher.getVoucherId(),
                    voucher.getStartDate(),
                    voucher.getEndDate(),
                    voucher.getDiscount(),
                    voucher.getStatus(),
                    voucher.getPost().getPostId()
            );
        }

    }

    public CRUDVoucherResponse getVoucherById(int voucherId){
        Voucher voucher = voucherRepository.findByVoucherId(voucherId);
        if(voucher == null){
            throw new BadRequestException("Not found voucher");
        }
        return new CRUDVoucherResponse(
                voucher.getVoucherId(),
                voucher.getStartDate(),
                voucher.getEndDate(),
                voucher.getDiscount(),
                voucher.getStatus(),
                voucher.getPost().getPostId()
        );
    }

    public ListAllVoucherResponse listAllVoucher()
    {
        List<Voucher> voucherList = voucherRepository.findAll();
        List<CRUDVoucherResponse> crudVoucherResponses = new ArrayList<>();
        for(Voucher voucher : voucherList){
            crudVoucherResponses.add(new CRUDVoucherResponse(
                    voucher.getVoucherId(),
                    voucher.getStartDate(),
                    voucher.getEndDate(),
                    voucher.getDiscount(),
                    voucher.getStatus(),
                    voucher.getPost().getPostId()
            ));
        }
        return  new ListAllVoucherResponse(crudVoucherResponses);
    }
    //
}