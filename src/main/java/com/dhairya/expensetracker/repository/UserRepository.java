package com.dhairya.expensetracker.repository;

import com.dhairya.expensetracker.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserInfo,String> {

    public Optional<UserInfo> findByUsername(String username);

}
