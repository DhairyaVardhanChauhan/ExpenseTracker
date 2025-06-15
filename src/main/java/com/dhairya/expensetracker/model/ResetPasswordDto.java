package com.dhairya.expensetracker.model;

import lombok.Data;

@Data
public class ResetPasswordDto {
    private String newPassword;
    private String confirmPassword;
}
