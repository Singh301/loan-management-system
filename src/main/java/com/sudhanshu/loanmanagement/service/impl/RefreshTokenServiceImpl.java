package com.sudhanshu.loanmanagement.service.impl;

import com.sudhanshu.loanmanagement.entity.RefreshToken;
import com.sudhanshu.loanmanagement.entity.User;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.repository.RefreshTokenRepository;
import com.sudhanshu.loanmanagement.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByUser(user)
                .orElse(new RefreshToken());

        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(
                LocalDateTime.now().plusSeconds(refreshExpiration / 1000)
        );

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {

            refreshTokenRepository.delete(token);

            throw new RuntimeException("Refresh token expired. Please login again.");
        }

        return token;
    }

    @Override
    public RefreshToken findByToken(String token) {

        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Refresh token not found."));
    }

    @Override
    public void deleteByUser(User user) {

        refreshTokenRepository.deleteByUser(user);
    }
}