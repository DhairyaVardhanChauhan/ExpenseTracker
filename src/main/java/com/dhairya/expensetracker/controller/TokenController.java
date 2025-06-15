package com.dhairya.expensetracker.controller;

import com.dhairya.expensetracker.entity.RefreshToken;
import com.dhairya.expensetracker.model.PasswordResetDto;
import com.dhairya.expensetracker.model.ResetPasswordDto;
import com.dhairya.expensetracker.request.AuthTokenDto;
import com.dhairya.expensetracker.request.RefreshTokenDto;
import com.dhairya.expensetracker.response.JwtResponseDto;
import com.dhairya.expensetracker.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
public class TokenController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsImpl userDetailsServiceImpl;

    @Autowired
    private PasswordService passwordService;
    @Qualifier("userDetailsService")
    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/auth/v1/login")
    public ResponseEntity AuthenticateAndGetToken(@RequestBody AuthTokenDto authTokenDto) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authTokenDto.getUsername(), authTokenDto.getPassword()));
        if(authentication.isAuthenticated()) {
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authTokenDto.getUsername());
            return new ResponseEntity<>(JwtResponseDto.builder().accessToken(jwtService.generateToken(authTokenDto.getUsername())).refreshToken(refreshToken.getToken()).build(), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Error in User Service",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/auth/v1/refreshToken")
    public JwtResponseDto refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        return refreshTokenService.findByToken(refreshTokenDto.getToken()).map((refreshToken)->refreshTokenService.verifyExpiration(refreshToken)).map((RefreshToken::getUserInfo)).map((userInfo)->{
            String accessToken = jwtService.generateToken(userInfo.getUsername());
            return JwtResponseDto.builder().accessToken(accessToken).refreshToken(refreshTokenDto.getToken()).build();
        }).orElseThrow(()-> new RuntimeException("Refresh token not found!"));
    }

    @PostMapping("/auth/v1/forgotPassword")
    public ResponseEntity<String> sendForgotPasswordMail(@RequestBody String email) {

        try{
            PasswordResetDto passwordResetDto = passwordService.createToken(email);
            passwordService.sendForgotPasswordMail(email,passwordResetDto);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/auth/v1/reset/password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto,@RequestParam("token")String token) {
        if(!Objects.equals(resetPasswordDto.getConfirmPassword(), resetPasswordDto.getNewPassword())){
            return new ResponseEntity<>("Passwords do not match!",HttpStatus.BAD_REQUEST);
        }
        return userDetailsServiceImpl.resetPassword(resetPasswordDto.getNewPassword(),token);
    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }
}
