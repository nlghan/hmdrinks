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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public boolean isCurrentDateWithinVoucherPeriod(Voucher voucher) {
        LocalDateTime currentDate = LocalDateTime.now();
        return (voucher.getStartDate() != null && voucher.getEndDate() != null &&
                !currentDate.isBefore(voucher.getStartDate()) &&
                !currentDate.isAfter(voucher.getEndDate()));
    }

    public ResponseEntity<?> getVoucher(GetVoucherReq req)
    {
        User user = userRepository.findByUserIdAndIsDeletedFalse(req.getUserId());
        if(user == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user");
        }
        Voucher voucher = voucherRepository.findByVoucherIdAndIsDeletedFalse(req.getVoucherId());
        if(voucher == null)
        {
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found voucher for Post");
        }
        if (!isCurrentDateWithinVoucherPeriod(voucher)) {
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The current date is not within the valid period.");
        }
        UserVoucher userVoucher1 = userVoucherRepository.findByUserUserIdAndVoucherVoucherId(user.getUserId(), req.getVoucherId());
        if(userVoucher1 != null)
        {
            return  ResponseEntity.status(HttpStatus.CONFLICT).body("Voucher already exists");
        }

        int totalVoucherUserBefore = 0;
        List<UserVoucher> userVoucherList = userVoucherRepository.findByVoucherVoucherId(req.getVoucherId());
        for(UserVoucher userVoucher : userVoucherList){
            if(userVoucher.getUser().getUserId() != req.getUserId())
            {
                totalVoucherUserBefore = totalVoucherUserBefore + 1 ;
            }

        }
        int currentVoucher = voucher.getNumber() - totalVoucherUserBefore;
        if(currentVoucher < 0)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Out of discount codes to use");
        }
        voucher.setNumber(voucher.getNumber() - 1);
        voucherRepository.save(voucher);
        UserVoucher userVoucher = new UserVoucher();
        userVoucher.setUser(user);
        userVoucher.setVoucher(voucher);
        userVoucher.setStatus(Status_UserVoucher.INACTIVE);
        userVoucherRepository.save(userVoucher);
        return  ResponseEntity.status(HttpStatus.OK).body(new GetVoucherResponse(
                userVoucher.getUserVoucherId(),
                userVoucher.getUser().getUserId(),
                userVoucher.getVoucher().getVoucherId(),
                userVoucher.getStatus().toString()
        ));
    }

    public ResponseEntity<?> listAllVoucherUserId(int userId){
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId);
        if(user == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found user");
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
         return ResponseEntity.status(HttpStatus.OK).body(new ListAllVoucherUserIdResponse(userVoucherList.size(), listVoucherResponse));
    }
}