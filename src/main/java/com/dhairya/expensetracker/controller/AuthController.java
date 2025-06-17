package com.dhairya.expensetracker.controller;

import com.dhairya.expensetracker.entity.RefreshToken;
import com.dhairya.expensetracker.model.UserInfoDto;
import com.dhairya.expensetracker.request.GoogleUserInfoDto;
import com.dhairya.expensetracker.response.JwtResponseDto;
import com.dhairya.expensetracker.response.PingResponse;
import com.dhairya.expensetracker.service.JwtService;
import com.dhairya.expensetracker.service.PasswordService;
import com.dhairya.expensetracker.service.RefreshTokenService;
import com.dhairya.expensetracker.service.UserDetailsImpl;
import com.dhairya.expensetracker.utils.Constants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
    @Autowired
    private PasswordService passwordService;

    @PostMapping("/auth/v1/signup")
    public ResponseEntity signup(@RequestBody UserInfoDto userInfoDto){
        try {
            return validateAndSignUpUser(userInfoDto,Constants.AuthProvider.LOCAL);
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

    @GetMapping("/auth/v1/oauth/callback")
    public void  grantCode(@RequestParam(value = "code") String code, @RequestParam(value = "scope",required = false) String scope, @RequestParam(value = "authuser",required = false) String authUser, @RequestParam("prompt") String prompt, HttpServletResponse response) throws IOException {
        String accessToken = passwordService.getOauthAccessTokenGoogle(code);
        GoogleUserInfoDto googleUserInfoDto = passwordService.getProfileDetailsGoogle(accessToken);
        UserInfoDto userInfoDto = buildUserInfoFromGoogle(googleUserInfoDto);
        try {
            ResponseEntity<?> result = validateAndSignUpUser(userInfoDto, Constants.AuthProvider.GOOGLE);
            JwtResponseDto jwtTokens = (JwtResponseDto) result.getBody();
            setCookies(jwtTokens, response);
            response.sendRedirect("http://localhost:5173/oauth-success");
        }catch (Exception e){
            response.sendRedirect("http://localhost:5173/login");
        }
    }

    private ResponseEntity validateAndSignUpUser(UserInfoDto userInfoDto, Constants.AuthProvider provider) {
        Boolean signUped = userDetailsImpl.signUpUser(userInfoDto,provider);
        if (!signUped && Constants.AuthProvider.LOCAL.equals(provider)) {
            return new ResponseEntity<>("Already Exists", HttpStatus.BAD_REQUEST);
        }
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfoDto.getUsername());
        String jwtToken = jwtService.generateToken(userInfoDto.getUsername());
        return new ResponseEntity<>(JwtResponseDto.builder().accessToken(jwtToken).refreshToken(refreshToken.getToken()).build(), HttpStatus.OK);
    }

    private UserInfoDto buildUserInfoFromGoogle(GoogleUserInfoDto googleUserInfoDto){
        String name = googleUserInfoDto.getName();
        String firstName = "";
        String lastName = "";
        if (name != null) {
            String[] parts = name.trim().split(" ");
            if (parts.length >= 2) {
                firstName = parts[0];
                lastName = parts[1];
            } else if (parts.length == 1) {
                firstName = parts[0];
            }
        }
        String email = googleUserInfoDto.getEmail();
        String phoneNumber = googleUserInfoDto.getPhoneNumber();

        UserInfoDto userInfoDto = new UserInfoDto(firstName,lastName,email,phoneNumber);
        userInfoDto.setUsername(firstName+" "+lastName);
        return userInfoDto;
    }

    private void setCookies(JwtResponseDto jwtTokens, HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("accessToken", jwtTokens.getAccessToken());
        accessTokenCookie.setHttpOnly(false);
        accessTokenCookie.setSecure(false);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(3600);

        Cookie refreshTokenCookie = new Cookie("refreshToken", jwtTokens.getRefreshToken());
        refreshTokenCookie.setHttpOnly(false);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }


}
