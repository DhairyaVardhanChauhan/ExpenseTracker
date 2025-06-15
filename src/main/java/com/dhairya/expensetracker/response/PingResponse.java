package com.dhairya.expensetracker.response;

import com.dhairya.expensetracker.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

@Data
@AllArgsConstructor
public class PingResponse {
    private String userId;
    private Set<UserRole> authorities;
}
