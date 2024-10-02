package com.hmdrinks.Service;

import com.hmdrinks.Entity.User;
import com.hmdrinks.Entity.UserInfo;
import com.hmdrinks.Enum.Role;
import com.hmdrinks.Enum.TypeLogin;
import com.hmdrinks.Exception.BadRequestException;
import com.hmdrinks.Exception.ConflictException;
import com.hmdrinks.Repository.UserInfoRepository;
import com.hmdrinks.Repository.UserRepository;
import com.hmdrinks.Request.CreateAccountUserReq;
import com.hmdrinks.Response.CRUDAccountUserResponse;
import com.hmdrinks.SupportFunction.SupportFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public CRUDAccountUserResponse createAccountUser(CreateAccountUserReq req){
        Optional <User> user = userRepository.findByUserNameAndIsDeletedFalse(req.getUserName());

        if(!user.isEmpty())
        {
            throw new ConflictException("User name already exists");
        }
        if(!SupportFunction.checkRole(req.getRole().toString())){
            throw new BadRequestException("Role is wrong");
        }

        User user1 = new User();
        user1.setType(TypeLogin.BASIC);
        user1.setEmail(req.getEmail());
        user1.setRole(req.getRole());
        user1.setIsDeleted(false);
        user1.setUserName(req.getUserName());
        user1.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user1);

        UserInfo userInfo = new UserInfo();

        userInfo.setFullName(req.getFullName());

        userInfo.setUser(user1);
        userInfoRepository.save(userInfo);
        Optional<User> userNewq = userRepository.findByUserNameAndIsDeletedFalse(req.getUserName());
        User userNew= userNewq.get();
        return new CRUDAccountUserResponse(
                userNew.getUserId(),
                userInfo.getFullName(),
                userNew.getUserName(),
                userNew.getEmail(),
                userNew.getPassword(),
                userNew.getRole().toString()
        );

    }
}
