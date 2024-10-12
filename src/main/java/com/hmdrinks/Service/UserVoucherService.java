package com.hmdrinks.Service;

import com.hmdrinks.Entity.User;
import com.hmdrinks.Entity.UserVoucher;
import com.hmdrinks.Entity.Voucher;
import com.hmdrinks.Enum.Status_UserVoucher;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Repository.PostRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Repository.UserVoucherRepository;
import com.hmdrinks.Repository.VoucherRepository;
import com.hmdrinks.Request.GetVoucherReq;
import com.hmdrinks.Response.GetVoucherResponse;
import com.hmdrinks.Response.ListAllVoucherUserIdResponse;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserVoucherService {
    @Autowired
    private UserVoucherRepository userVoucherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private PostRepository postRepository;

    public GetVoucherResponse getVoucher(GetVoucherReq req)
    {
        User user = userRepository.findByUserId(req.getUserId());
        if(user == null)
        {
            throw new BadRequestException("Not found user");
        }
        Voucher voucher = voucherRepository.findByVoucherId(req.getVoucherId());
        if(voucher == null)
        {
            throw new BadRequestException("Not found voucher for Post");
        }
        UserVoucher userVoucher1 = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(user.getUserId(), req.getVoucherId());
        if(userVoucher1 != null)
        {
            throw new BadRequestException("Voucher already exists");
        }
        UserVoucher userVoucher = new UserVoucher();
        userVoucher.setUser(user);
        userVoucher.setVoucher(voucher);
        userVoucher.setStatus(Status_UserVoucher.INACTIVE);
        userVoucherRepository.save(userVoucher);

        return  new GetVoucherResponse(
                userVoucher.getUserVoucherId(),
                userVoucher.getUser().getUserId(),
                userVoucher.getVoucher().getVoucherId(),
                userVoucher.getStatus().toString()
        );
    }

    public ListAllVoucherUserIdResponse listAllVoucherUserId(int userId){
        User user = userRepository.findByUserId(userId);
        if(user == null)
        {
            throw new BadRequestException("Not found user");
        }
         List<UserVoucher> userVoucherList = userVoucherRepository.findByUserUserId(userId);
         List<GetVoucherResponse> listVoucherResponse = new ArrayList<>();
         for(UserVoucher userVoucher : userVoucherList)
         {
             listVoucherResponse.add( new GetVoucherResponse(
                     userVoucher.getUserVoucherId(),
                     userVoucher.getUser().getUserId(),
                     userVoucher.getVoucher().getVoucherId(),
                     userVoucher.getStatus().toString()
             ));
         }
         return new ListAllVoucherUserIdResponse(listVoucherResponse);
    }

}
