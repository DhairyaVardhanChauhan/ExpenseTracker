package com.dhairya.expensetracker.service;

import com.dhairya.expensetracker.entity.PasswordResetToken;
import com.dhairya.expensetracker.entity.UserExtraInfo;
import com.dhairya.expensetracker.model.PasswordResetDto;
import com.dhairya.expensetracker.model.UserInfoDto;
import com.dhairya.expensetracker.repository.PasswordResetRepository;
import com.dhairya.expensetracker.repository.UserExtraInfoRepository;
import com.dhairya.expensetracker.repository.UserRepository;
import com.dhairya.expensetracker.request.GoogleUserInfoDto;
import com.dhairya.expensetracker.response.GoogleTokenResponse;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import com.dhairya.expensetracker.utils.Constants;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {

    private final MailingService mailingService;
    private final PasswordEncoder  passwordEncoder;
    private final UserExtraInfoRepository userInfoDtoRepository;
    private final PasswordResetRepository passwordResetRepository;

    @Value("${google.client.secret}")
    private String clientSecret;
    @Value("${google.client.id}")
    private String clientId;
    @Value("${google.app.key}")
    private String appKey;
    @Value("${google.app.mail}")
    private String appMail;

    public PasswordResetDto createToken(String email){
        UserExtraInfo user = userInfoDtoRepository.findByEmail(email);
        if(user == null){
            throw new RuntimeException("User not found");
        }
        Optional<PasswordResetToken> existingTokenOpt = passwordResetRepository.findByUserExtraInfo_UserId(user.getUserId());
        PasswordResetToken passwordResetToken = existingTokenOpt.orElseGet(PasswordResetToken::new);
        passwordResetToken.setToken(passwordEncoder.encode(UUID.randomUUID().toString()));
        passwordResetToken.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));
        passwordResetToken.setUserExtraInfo(user);
        passwordResetRepository.save(passwordResetToken);
        return new PasswordResetDto(user.getUserId(),passwordResetToken.getToken());

    }

    public void sendForgotPasswordMail(String toEmail,PasswordResetDto passwordResetDto){
        log.info("Sending forgot password mail");
        Properties props = System.getProperties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(appMail, appKey);
            }
        });
        mailingService.sendEmail(session,toEmail,Constants.GET_FORGOT_PASSWORD_SUBJECT, getForgotPasswordBody(passwordResetDto));
    }

    String getForgotPasswordBody(PasswordResetDto passwordResetDto) {
        String encodedToken = URLEncoder.encode(passwordResetDto.getToken(), StandardCharsets.UTF_8);

        return """
    <html>
        <body>
            <p>Hello,</p>

            <p>We received a request to reset the password for your account.</p>

            <p>
                Click the link below to reset your password:
                <br />
                <a href="http://localhost:5173/resetPassword?token=%s" target="_blank">
                    Reset Your Password
                </a>
            </p>

            <p>If you did not request this, you can safely ignore this email.</p>

            <p>Thank you,<br/>
            The Expense Tracker Team</p>
        </body>
    </html>
    """.formatted(encodedToken);
    }


    public String getOauthAccessTokenGoogle(String code){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("redirect_uri", "http://localhost:9898/auth/v1/oauth/callback");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("scope", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile");
        params.add("scope", "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email");
        params.add("scope", "https://www.googleapis.com/auth/user.phonenumbers.read");
        params.add("scope", "https://www.googleapis.com/auth/contacts.readonly");
        params.add("scope", "openid");
        params.add("grant_type", "authorization_code");
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, httpHeaders);
        String url = "https://oauth2.googleapis.com/token";
        ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(url, requestEntity, GoogleTokenResponse.class);
        return Objects.requireNonNull(response.getBody()).getAccessToken();
    }

    public GoogleUserInfoDto getProfileDetailsGoogle(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        String url = "https://www.googleapis.com/oauth2/v2/userinfo";
        ResponseEntity<GoogleUserInfoDto> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, GoogleUserInfoDto.class);
        return response.getBody();
    }

}
