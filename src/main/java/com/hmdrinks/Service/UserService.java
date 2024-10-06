package com.hmdrinks.Service;

import com.hmdrinks.Entity.OTP;
import com.hmdrinks.Entity.User;
import com.hmdrinks.Enum.Sex;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Exception.ConflictException;
import com.hmdrinks.Exception.NotFoundException;
import com.hmdrinks.Repository.OtpRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Request.ChangePasswordReq;
import com.hmdrinks.Request.UserInfoUpdateReq;
import com.hmdrinks.SupportFunction.SupportFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import  org.springframework.mail.*;

import com.hmdrinks.Response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final  UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final SupportFunction supportFunction;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender javaMailSender;

    public ListAllUserResponse getListAllUser(String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<User> userList = userRepository.findAll(pageable);
        List<DetailUserResponse> detailUserResponseList = new ArrayList<>();

        for (User user : userList) {

            String fullLocation = user.getStreet() + ","+ user.getDistrict() + ","+ user.getCity();
            detailUserResponseList.add(new DetailUserResponse(
                        user.getUserId(),
                        user.getUserName(),
                        user.getFullName(),
                        user.getAvatar(),
                        user.getBirthDate(),
                        fullLocation,
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getSex().toString(),
                        user.getType().toString(),
                        user.getIsDeleted(),
                        user.getDateDeleted(),
                        user.getDateUpdated(),
                        user.getDateCreated(),
                        user.getRole().toString()
                ));
            }
        return new ListAllUserResponse(page,userList.getTotalPages(),limit, detailUserResponseList);
    }

    public GetDetailUserInfoResponse getDetailUserInfoResponse(Integer id){
        User userList = userRepository.findByUserId(id);

        if (userList == null) {
            throw new RuntimeException("Khong ton tai user");
        }



        String fullLocation = userList.getStreet() + ","+ userList.getDistrict() + ","+ userList.getCity();
        return new GetDetailUserInfoResponse(
                userList.getUserId(),
                userList.getUserName(),
                userList.getFullName(),
                userList.getAvatar(),
                userList.getBirthDate(),
                fullLocation,
                userList.getEmail(),
                userList.getPhoneNumber(),
                userList.getSex().toString(),
                userList.getType().toString(),
                userList.getIsDeleted(),
                userList.getDateDeleted(),
                userList.getDateUpdated(),
                userList.getDateCreated(),
                userList.getRole().toString()
        );
    }
    public UpdateUserInfoResponse updateUserInfoResponse(UserInfoUpdateReq req){
        // Tìm người dùng hiện tại theo userId
        User userList = userRepository.findByUserId(req.getUserId());

        if (userList == null) {
            throw new NotFoundException("User does not exist");
        }

        // Kiểm tra xem email đã được sử dụng bởi người dùng khác hay không
        Optional<User> user = userRepository.findByEmailAndUserIdNot(req.getEmail(), req.getUserId());
        if(user.isPresent()) { // Đảo ngược điều kiện
            throw new ConflictException("Email already exists");
        }

        // Kiểm tra số điện thoại
        supportFunction.checkPhoneNumber(req.getPhoneNumber(), req.getUserId(), userRepository);
        User userWithEmail = userRepository.findByEmail(req.getEmail());

        // Cập nhật thông tin người dùng
        LocalDate currentDate = LocalDate.now();
        String[] locationParts = req.getAddress().split(",");
        userList.setEmail(req.getEmail());
        userList.setFullName(req.getFullName());
        userList.setPhoneNumber(req.getPhoneNumber());
        userList.setAvatar(req.getAvatar());
        userList.setSex(Sex.valueOf(req.getSex()));
        userList.setBirthDate(req.getBirthDay());
        userList.setDateUpdated(Date.valueOf(currentDate));

        if(locationParts.length >= 3){
            String street = locationParts[0].trim();   // Lấy phần street
            String district = locationParts[1].trim();  // Lấy phần district
            String city = locationParts[2].trim();
            userList.setCity(city);
            userList.setStreet(street);
            userList.setDistrict(district);
        } else {
            throw new BadRequestException("Invalid address format");
        }

        userRepository.save(userList);

        return new UpdateUserInfoResponse(
                userList.getUserId(),
                userList.getUserName(),
                userList.getFullName(),
                userList.getAvatar(),
                userList.getBirthDate(),
                req.getAddress(),
                userList.getEmail(),
                userList.getPhoneNumber(),
                userList.getSex().toString(),
                userList.getType().toString(),
                userList.getIsDeleted(),
                userList.getDateDeleted(),
                userList.getDateUpdated(),
                userList.getDateCreated(),
                userList.getRole().toString()
        );
    }


    public SendEmailResponse sendEmail(String email) {
        Random random = new Random();
        User users = userRepository.findByEmail(email);

        if (users == null) {
            throw new NotFoundException("Username or email does not exist");
        }

        OTP otpEntity = otpRepository.findByEmail(email);
        if (otpEntity != null) {
            otpRepository.deleteById(otpEntity.getOtpId());
        }

        int randomNumber = random.nextInt(900000) + 100000;
        String to = users.getEmail();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("HMDRINKS");
        message.setTo(to);
        String text = "Dear user,\n\n"
                + "We have received a request to reset the password for your account.\n"
                + "Please use the following One-Time Password (OTP) to proceed with the password recovery process:\n\n"
                + "Your Password Recovery OTP: " + randomNumber + "\n\n"
                + "This OTP is valid for a single use and will expire in 10 minutes for security reasons.\n"
                + "Please make sure to use the OTP within this timeframe.\n\n"
                + "For security purposes, do not share this OTP with anyone.\n"
                + "If you did not initiate this request, you can safely ignore this email.\n"
                + "Please ensure the security of your account and do not reply to this message.\n\n"
                + "Best regards,\n"
                + "Your Application Support Team";
        message.setSubject("Password Recovery OTP");
        message.setText(text);
        javaMailSender.send(message);
        LocalDateTime currentDateTime = LocalDateTime.now();
        OTP otp = new OTP();
        otp.setEmail(users.getEmail());
        otp.setUserName(users.getUserName());
        otp.setOtp(String.valueOf(randomNumber));
        otp.setTimeOtp(currentDateTime);
        otp.setStatus(Boolean.TRUE);
        otpRepository.save(otp);
        return new SendEmailResponse(otp.getEmail(), "OTP has been sent to your email.");
    }

    public MessageResponse AcceptOTP(String email, int OTP) {
        String newPass = "pass12345";
        OTP otp = otpRepository.findOTP(email, String.valueOf(OTP));
        User users = userRepository.findByEmail(email);
        if (otp != null && OTP == Integer.parseInt(otp.getOtp())) {
            LocalDateTime currentDateTime = LocalDateTime.now();
            LocalDateTime tenMinutesAgo = currentDateTime.minusMinutes(10);
            boolean isWithinTenMinutes = (otp.getTimeOtp().isAfter(tenMinutesAgo) && otp.getTimeOtp().isBefore(currentDateTime));
            if (isWithinTenMinutes) {
                users.setPassword(passwordEncoder.encode(newPass));
                userRepository.save(users);
                String to = users.getEmail();
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("HMDRINKS");
                message.setTo(to);
                String text = "Hello user,\n\n"
                        + "We have received a password recovery request for your account. Below is your new password:\n\n"
                        + "Username:" + users.getUserName() + "\n\n"
                        + "New Password: " + newPass + "\n\n"
                        + "Please make sure to copy the password above and paste it into the login page to avoid any errors.\n\n"
                        + "If you did not initiate this request, please disregard this email and ensure the security of your account.\n\n"
                        + "Best regards,\n"
                        + "Your Application Support Team";

                message.setSubject("Password Recovery Information for Your Account");
                message.setText(text);
                javaMailSender.send(message);
                otp.setStatus(Boolean.FALSE);
                otpRepository.save(otp);
                return new MessageResponse("Your new password was sent to your email. Please check email.");
            } else {
                otp.setStatus(Boolean.FALSE);
                otpRepository.save(otp);
                return new MessageResponse("OTP existing time is expired.");
            }
        } else {
            return new MessageResponse("Your username, email or OTP is not correct.");
        }
    }
    public TotalSearchUserResponse totalSearchUser(String keyword,String pageFromParam, String limitFromParam) {
        int page = Integer.parseInt(pageFromParam);
        int limit = Integer.parseInt(limitFromParam);
        if (limit >= 100) limit = 100;
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<User> userList = userRepository.findByUserNameContainingOrEmailContainingOrFullNameContainingOrStreetContainingOrDistrictContainingOrCityContainingOrPhoneNumberContaining(
                keyword,keyword,keyword,keyword,keyword,keyword,keyword,pageable);
        List<DetailUserResponse> detailUserResponseList = new ArrayList<>();

        for (User user : userList) {

            String fullLocation = user.getStreet() + ","+ user.getDistrict() + ","+ user.getCity();
            detailUserResponseList.add(new DetailUserResponse(
                    user.getUserId(),
                    user.getUserName(),
                    user.getFullName(),
                    user.getAvatar(),
                    user.getBirthDate(),
                    fullLocation,
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getSex().toString(),
                    user.getType().toString(),
                    user.getIsDeleted(),
                    user.getDateDeleted(),
                    user.getDateUpdated(),
                    user.getDateCreated(),
                    user.getRole().toString()
            ));
        }
        return new TotalSearchUserResponse(page,userList.getTotalPages(),limit, detailUserResponseList);
    }

    public ChangePasswordResponse changePasswordResponse(ChangePasswordReq req) {
        User users = userRepository.findByUserId(req.getUserId());

        if (users == null) {
            throw new NotFoundException("Not found user");
        }

        if (!passwordEncoder.matches(req.getCurrentPassword(), users.getPassword())) {
            throw new BadRequestException("The current password is incorrect");
        }

        if (req.getNewPassword().equals(req.getCurrentPassword())) {
            throw new BadRequestException("You're using an old password");
        }
        if (!req.getNewPassword().matches(req.getConfirmNewPassword())) {
            throw new BadRequestException("No overlap");
        }
        users.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(users);
        String message = "Password change successfully";
        return new ChangePasswordResponse(
                message,
                req.getNewPassword()
        );

    }

}
