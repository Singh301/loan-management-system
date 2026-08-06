package com.sudhanshu.loanmanagement.service;

import com.sudhanshu.loanmanagement.entity.RefreshToken;
import com.sudhanshu.loanmanagement.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyExpiration(RefreshToken token);

    RefreshToken findByToken(String token);

    void deleteByUser(User user);

}