package com.dhairya.expensetracker.controller;

import com.dhairya.expensetracker.entity.RefreshToken;
import com.dhairya.expensetracker.request.AuthTokenDto;
import com.dhairya.expensetracker.request.RefreshTokenDto;
import com.dhairya.expensetracker.response.JwtResponseDto;
import com.dhairya.expensetracker.service.JwtService;
import com.dhairya.expensetracker.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("auth/v1/login")
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

        @PostMapping("auth/v1/refreshToken")
    public JwtResponseDto refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        return refreshTokenService.findByToken(refreshTokenDto.getToken()).map((refreshToken)->refreshTokenService.verifyExpiration(refreshToken)).map((RefreshToken::getUserInfo)).map((userInfo)->{
            String accessToken = jwtService.generateToken(userInfo.getUsername());
            return JwtResponseDto.builder().accessToken(accessToken).build();
        }).orElseThrow(()-> new RuntimeException("Refresh token not found!"));
    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }
}
