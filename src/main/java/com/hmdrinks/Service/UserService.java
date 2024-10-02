package com.hmdrinks.Service;

import com.hmdrinks.Entity.OTP;
import com.hmdrinks.Entity.User;
import com.hmdrinks.Entity.UserInfo;
import com.hmdrinks.Enum.Sex;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Exception.NotFoundException;
import com.hmdrinks.Repository.OtpRepository;
import com.hmdrinks.Repository.UserInfoRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Request.ChangePasswordReq;
import com.hmdrinks.Request.IdReq;
import com.hmdrinks.Request.UserInfoUpdateReq;
import org.springframework.beans.factory.annotation.Autowired;
import  org.springframework.mail.*;

import com.hmdrinks.Response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private  final OtpRepository otpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender javaMailSender;

    public ListAllUserResponse getListAllUser() {
        List<User> userList = userRepository.findAll();
        List<DetailUserResponse> detailUserResponseList = new ArrayList<>();

        for (User user : userList) {
            UserInfo userInfoOpt = userInfoRepository.findByUserUserId(user.getUserId());
            if (userInfoOpt != null) {

                detailUserResponseList.add(new DetailUserResponse(
                        user.getUserId(),
                        user.getUserName(),
                        userInfoOpt.getFullName(), // Thay thế với trường thực tế trong UserInfo mà bạn cần
                        user.getIsDeleted(),
                        user.getRole().toString()
                ));
            }
        }

        return new ListAllUserResponse(detailUserResponseList);
    }

    public GetDetailUserInfoResponse getDetailUserInfoResponse(Integer id){
        User userList = userRepository.findByUserId(id);

        if (userList == null) {  // Kiểm tra xem Optional có giá trị hay không
            throw new RuntimeException("Khong ton tai user");
        }
        User user = userList;  // Lấy đối tượng User từ Optional
        Optional<UserInfo> userInfo = Optional.ofNullable(userInfoRepository.findByUserUserId(user.getUserId()));
        if(userInfo.isEmpty()){
            throw new RuntimeException("Khong ton tai thong tin user");
        }

        UserInfo userInfo1 = userInfo.get();
        String fullLocation = userInfo1.getStreet() + ","+ userInfo1.getDistrict() + ","+userInfo1.getCity();
        Sex sexEnum = userInfo1.getSex(); // Lấy giá trị sex dưới dạng enum
        String sex;

        if (sexEnum == null || sexEnum.toString().isEmpty()) {
            sex = Sex.OTHER.toString(); // Nếu null hoặc rỗng, gán giá trị mặc định là "OTHER"
        } else {
            sex = sexEnum.toString(); // Ngược lại, chuyển sex thành chuỗi
        }

        return new GetDetailUserInfoResponse(
                user.getUserId(),
                user.getEmail(),
                userInfo1.getFullName(),
                userInfo1.getPhoneNumber(),
                userInfo1.getAvatar(),
                sex,
                userInfo1.getBirthDate(),
                fullLocation
        );
    }
    public UpdateUserInfoResponse updateUserInfoResponse(UserInfoUpdateReq req){
        User userList = userRepository.findByUserId(req.getUserId());

        if (userList == null) {  // Kiểm tra xem Optional có giá trị hay không
            throw new RuntimeException("Khong ton tai user");
        }
        User user = userList;  // Lấy đối tượng User từ Optional
        Optional<UserInfo> userInfo = Optional.ofNullable(userInfoRepository.findByUserUserId(user.getUserId()));
        if(userInfo.isEmpty()){
            throw new RuntimeException("Khong ton tai thong tin user");
        }
        String[] locationParts = req.getAddress().split(",");
        user.setEmail(req.getEmail());
        userRepository.save(user);
        UserInfo userInfo1 = userInfo.get();
        userInfo1.setFullName(req.getFullName());
        userInfo1.setPhoneNumber(req.getPhoneNumber());
        userInfo1.setAvatar(req.getAvatar());
        userInfo1.setSex(Sex.valueOf(req.getSex()));
        userInfo1.setBirthDate(req.getBirthDay());
        String street = locationParts[0].trim();   // Lấy phần street
        String district = locationParts[1].trim();  // Lấy phần district
        String city = locationParts[2].trim();
        userInfo1.setCity(city);
        userInfo1.setStreet(street);
        userInfo1.setDistrict(district);

        userInfoRepository.save(userInfo1);

        return new UpdateUserInfoResponse(
                user.getUserId(),
                user.getEmail(),
                userInfo1.getFullName(),
                userInfo1.getPhoneNumber(),
                userInfo1.getAvatar(),
                req.getSex(),
                userInfo1.getBirthDate(),
                req.getAddress()
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
        message.setFrom("20133118@student.hcmute.edu.vn");
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
                message.setFrom("20133118@student.hcmute.edu.vn");
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
