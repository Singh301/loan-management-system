package com.sudhanshu.loanmanagement.auth.service;

import com.sudhanshu.loanmanagement.auth.entity.RefreshToken;
import com.sudhanshu.loanmanagement.user.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyExpiration(RefreshToken token);

    RefreshToken findByToken(String token);

    void deleteByUser(User user);

}




