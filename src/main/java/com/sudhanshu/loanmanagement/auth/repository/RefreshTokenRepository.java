package com.sudhanshu.loanmanagement.auth.repository;

import com.sudhanshu.loanmanagement.auth.entity.RefreshToken;
import com.sudhanshu.loanmanagement.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);



    @Modifying
    @Transactional
    int deleteByUser(User user);
}




