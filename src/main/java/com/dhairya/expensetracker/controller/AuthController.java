package com.dhairya.expensetracker.controller;

import com.dhairya.expensetracker.entity.RefreshToken;
import com.dhairya.expensetracker.model.UserInfoDto;
import com.dhairya.expensetracker.response.JwtResponseDto;
import com.dhairya.expensetracker.service.JwtService;
import com.dhairya.expensetracker.service.RefreshTokenService;
import com.dhairya.expensetracker.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private UserDetailsImpl userDetailsImpl;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("auth/v1/signup")
    public ResponseEntity signup(@RequestBody UserInfoDto userInfoDto){
        try {
            Boolean signUped = userDetailsImpl.signUpUser(userInfoDto);
            if (signUped) {
                return new ResponseEntity<>("Already Exists", HttpStatus.BAD_REQUEST);
            }
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfoDto.getUserName());
            String jwtToken = jwtService.generateToken(userInfoDto.getUserName());
            return new ResponseEntity<>(JwtResponseDto.builder().accessToken(jwtToken).refreshToken(refreshToken.getToken()).build(), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
