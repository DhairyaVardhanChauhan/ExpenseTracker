package com.dhairya.expensetracker.service;

import com.dhairya.expensetracker.entity.RefreshToken;
import com.dhairya.expensetracker.entity.UserInfo;
import com.dhairya.expensetracker.repository.RefreshTokenRepository;
import com.dhairya.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;

    public RefreshToken createRefreshToken(String username) {
        UserInfo extractedUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<RefreshToken> existingTokenOpt = refreshTokenRepository.findByUserInfo_UserId(extractedUser.getUserId());

        RefreshToken refreshToken = existingTokenOpt.orElseGet(RefreshToken::new);

        refreshToken.setUserInfo(extractedUser);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plus(Duration.ofHours(1)));

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if(token.getExpiryDate().compareTo(Instant.now()) < 0){
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " expired");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

}
