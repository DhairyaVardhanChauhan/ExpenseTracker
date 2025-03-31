package com.dhairya.expensetracker.service;

import com.dhairya.expensetracker.entity.UserInfo;
import com.dhairya.expensetracker.model.UserInfoDto;
import com.dhairya.expensetracker.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Data
public class UserDetailsImpl implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user =  userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException(username));
        return new CustomUserDetails(user);
    }

    public UserInfo checkIfUserAlreadyExists(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public Boolean signUpUser(UserInfoDto user) {

        if(Objects.nonNull(checkIfUserAlreadyExists(user.getUsername()))) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUserId(UUID.randomUUID().toString());
        userRepository.save(new UserInfo(user.getUserId(),user.getUserName(),user.getPassword(), new HashSet<>()));
        return true;


    }

}
