package com.dhairya.expensetracker.model;


import com.dhairya.expensetracker.entity.UserInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
public class UserInfoDto extends UserInfo {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}