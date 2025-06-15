package com.dhairya.expensetracker.controller;

import com.dhairya.expensetracker.entity.RefreshToken;
import com.dhairya.expensetracker.model.UserInfoDto;
import com.dhairya.expensetracker.response.JwtResponseDto;
import com.dhairya.expensetracker.response.PingResponse;
import com.dhairya.expensetracker.service.JwtService;
import com.dhairya.expensetracker.service.RefreshTokenService;
import com.dhairya.expensetracker.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
public class AuthController {

    @Autowired
    private UserDetailsImpl userDetailsImpl;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtService jwtService;
    @Qualifier("userDetailsService")
    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/auth/v1/signup")
    public ResponseEntity signup(@RequestBody UserInfoDto userInfoDto){
        try {
            Boolean signUped = userDetailsImpl.signUpUser(userInfoDto);
            if (!signUped) {
                return new ResponseEntity<>("Already Exists", HttpStatus.BAD_REQUEST);
            }
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfoDto.getUsername());
            String jwtToken = jwtService.generateToken(userInfoDto.getUsername());
            return new ResponseEntity<>(JwtResponseDto.builder().accessToken(jwtToken).refreshToken(refreshToken.getToken()).build(), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/auth/v1/ping")
    public ResponseEntity ping(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()){
            PingResponse pingResponse = userDetailsImpl.getUserIdFromUserName(authentication.getName());
            if(Objects.nonNull(pingResponse)){
                return ResponseEntity.ok(pingResponse);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized!");
    }

}
