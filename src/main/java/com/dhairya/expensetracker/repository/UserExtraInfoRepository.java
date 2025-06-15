package com.dhairya.expensetracker.repository;

import com.dhairya.expensetracker.entity.UserExtraInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserExtraInfoRepository extends JpaRepository<UserExtraInfo, String> {

    UserExtraInfo findByEmail(String email);

}
