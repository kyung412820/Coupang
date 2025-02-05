package com.example.coupang.securiy.token.repository;

import com.example.coupang.securiy.token.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

  // refreshToken 필드로 검색하는 메서드
  RefreshToken findByRefreshToken(String refreshToken);

  // userId 필드로 검색하는 메서드
  RefreshToken findByUserId(String userId);
}
