package com.example.coupang.securiy.token.service;


import com.example.coupang.securiy.token.entity.RefreshToken;
import com.example.coupang.securiy.token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
  private final RefreshTokenRepository RefreshTokenRepository;

  public RefreshToken findByRefreshToken(String refreshToken) {

    RefreshToken reFreshToken = RefreshTokenRepository.findByRefreshToken(refreshToken);

    if (reFreshToken == null) {
      throw new IllegalArgumentException("User not found with refreshToken: " + refreshToken);
    }
    return reFreshToken;
  }
}
