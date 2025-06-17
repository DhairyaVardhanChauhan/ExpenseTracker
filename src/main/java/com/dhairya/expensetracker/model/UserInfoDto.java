package com.dhairya.expensetracker.model;


import com.dhairya.expensetracker.entity.UserInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDto extends UserInfo {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}