package com.dhairya.expensetracker.request;

import lombok.Data;

@Data
public class AuthTokenDto {
    private String username;
    private String password;
}
