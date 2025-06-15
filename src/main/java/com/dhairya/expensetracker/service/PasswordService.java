package com.dhairya.expensetracker.service;

import com.dhairya.expensetracker.entity.PasswordResetToken;
import com.dhairya.expensetracker.entity.UserExtraInfo;
import com.dhairya.expensetracker.model.PasswordResetDto;
import com.dhairya.expensetracker.model.UserInfoDto;
import com.dhairya.expensetracker.repository.PasswordResetRepository;
import com.dhairya.expensetracker.repository.UserExtraInfoRepository;
import com.dhairya.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import com.dhairya.expensetracker.utils.Constants;

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
                return new PasswordAuthentication("trash93119@gmail.com", "jjukcwtnxgrvjdcp");
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


}
