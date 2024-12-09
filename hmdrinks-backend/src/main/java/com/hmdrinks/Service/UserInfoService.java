package com.hmdrinks.Service;
import com.hmdrinks.Entity.MyUserDetails;
import com.hmdrinks.Entity.User;
import com.hmdrinks.Enum.Role;
import com.hmdrinks.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class UserInfoService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User usersEntity = userRepository.findByUserNameAndIsDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("no user name found"));
        Collection<SimpleGrantedAuthority> role = new ArrayList<>();
        role.add(new SimpleGrantedAuthority(usersEntity.getRole().toString()));
        return new org.springframework.security.core.userdetails.User(usersEntity.getUserName(), usersEntity.getPassword(), role);
    }
    public MyUserDetails createMyUserDetails(User usersEntity) {
        return new MyUserDetails(usersEntity, usersEntity.getRole());
    }
}