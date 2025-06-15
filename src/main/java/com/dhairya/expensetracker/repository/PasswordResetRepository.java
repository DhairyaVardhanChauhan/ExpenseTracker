package com.dhairya.expensetracker.repository;

import com.dhairya.expensetracker.entity.PasswordResetToken;
import com.dhairya.expensetracker.entity.UserExtraInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordResetToken,String> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUserExtraInfo_UserId(String userId);
}
