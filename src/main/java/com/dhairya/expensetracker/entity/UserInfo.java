package com.dhairya.expensetracker.entity;

import com.dhairya.expensetracker.utils.Constants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class UserInfo {

    @Id
    @Column(name = "user_id", columnDefinition = "VARCHAR(36)")
    private String userId;
    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private Constants.AuthProvider authProvider = Constants.AuthProvider.LOCAL;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<UserRole> roles = new HashSet<>();

}
