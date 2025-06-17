package com.dhairya.expensetracker.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleUserInfoDto {
    private String id;
    private String email;

    @JsonProperty("verified_email")
    private boolean verifiedEmail;

    private String name;

    @JsonProperty("given_name")
    private String givenName;

    private String picture;
    private String phoneNumber;
}
