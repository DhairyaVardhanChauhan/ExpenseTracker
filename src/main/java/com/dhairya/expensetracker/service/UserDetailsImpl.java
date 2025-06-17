package com.dhairya.expensetracker.service;

import com.dhairya.expensetracker.entity.PasswordResetToken;
import com.dhairya.expensetracker.entity.UserInfo;
import com.dhairya.expensetracker.entity.UserRole;
import com.dhairya.expensetracker.eventProducer.UserInfoProducer;
import com.dhairya.expensetracker.model.UserInfoDto;
import com.dhairya.expensetracker.repository.PasswordResetRepository;
import com.dhairya.expensetracker.repository.UserExtraInfoRepository;
import com.dhairya.expensetracker.repository.UserRepository;
import com.dhairya.expensetracker.response.PingResponse;
import com.dhairya.expensetracker.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
@Data
public class UserDetailsImpl implements UserDetailsService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final UserInfoProducer userInfoProducer;
    @Autowired
    private PasswordResetRepository passwordResetRepository;
    @Autowired
    private UserExtraInfoRepository userExtraInfoRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo user =  userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException(username));
        return new CustomUserDetails(user);
    }

    public UserInfo checkIfUserAlreadyExists(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public Boolean signUpUser(UserInfoDto user,Constants.AuthProvider provider) {

        if(Objects.nonNull(checkIfUserAlreadyExists(user.getUsername()))) {
            return false;
        }
        user.setUserId(UUID.randomUUID().toString());
        if(Constants.AuthProvider.LOCAL.equals(provider)){
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(new UserInfo(user.getUserId(),user.getUsername(),user.getPassword(), Constants.AuthProvider.LOCAL,new HashSet<>()));
            System.out.println("Data sent to producer" + user.getPhoneNumber());
            userInfoProducer.sendEventToKafka(user);
        }
        else if(Constants.AuthProvider.GOOGLE.equals(provider)){
            userRepository.save(new UserInfo(user.getUserId(),user.getUsername(),null, Constants.AuthProvider.GOOGLE,new HashSet<>()));
            System.out.println("Data sent to producer" + user.getPhoneNumber());
            userInfoProducer.sendEventToKafka(user);
        }
        return true;

    }

    public PingResponse getUserIdFromUserName(String username) {
        UserInfo userInfo = userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException(username));
        Set<UserRole> userInfoRoles = userInfo.getRoles();
        return new PingResponse(userInfo.getUserId(),userInfoRoles);
    }

    public ResponseEntity<String> resetPassword(String password, String encodedToken) {
        String token = URLDecoder.decode(encodedToken, StandardCharsets.UTF_8);

        PasswordResetToken passwordResetToken = passwordResetRepository
                .findByToken(token)
                .orElseThrow(() -> new RuntimeException("Session not found!"));

        if (passwordResetToken.getExpiresAt().isBefore(Instant.now())) {
            return ResponseEntity.badRequest().body("{\"message\": \"Token has been expired\"}");
        }

        if (passwordResetToken.getUserExtraInfo() == null) {
            return ResponseEntity.badRequest().body("{\"message\": \"No user mapped to the token\"}");
        }

        String userId = passwordResetToken.getUserExtraInfo().getUserId();

        UserInfo userInfo = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        userInfo.setPassword(passwordEncoder.encode(password));
        userRepository.save(userInfo);

        return ResponseEntity.ok("{\"message\": \"Password reset successful\"}");
    }



}
