package com.hmdrinks.Service;

import com.hmdrinks.Entity.User;
import com.hmdrinks.Entity.UserInfo;
import com.hmdrinks.Enum.Sex;
import com.hmdrinks.Repository.UserInfoRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Request.IdReq;
import com.hmdrinks.Request.UserInfoUpdateReq;
import com.hmdrinks.Response.DetailUserResponse;
import com.hmdrinks.Response.GetDetailUserInfoResponse;
import com.hmdrinks.Response.ListAllUserResponse;
import com.hmdrinks.Response.UpdateUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;

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
        Optional<User> userList = userRepository.findByUserId(id);

        if (userList.isEmpty()) {  // Kiểm tra xem Optional có giá trị hay không
            throw new RuntimeException("Khong ton tai user");
        }
        User user = userList.get();  // Lấy đối tượng User từ Optional
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
        Optional<User> userList = userRepository.findByUserId(req.getUserId());

        if (userList.isEmpty()) {  // Kiểm tra xem Optional có giá trị hay không
            throw new RuntimeException("Khong ton tai user");
        }
        User user = userList.get();  // Lấy đối tượng User từ Optional
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


}
