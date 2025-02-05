package com.example.coupang.securiy.token.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@RedisHash("RefreshToken") // Redis에 저장될 해시 이름 (논리적인 테이블명 역할)
public class RefreshToken implements Serializable {

  @Id
  private String id;

  private String userId;

  private String refreshToken;

  // 기본 생성자
  public RefreshToken() {}

  // 생성자: 새로운 RefreshToken 객체 생성 시 UUID로 id 생성
  public RefreshToken(String userId, String refreshToken) {
    this.id = UUID.randomUUID().toString();
    this.userId = userId;
    this.refreshToken = refreshToken;
  }

  // refreshToken 갱신 메서드
  public RefreshToken update(String newRefreshToken) {
    this.refreshToken = newRefreshToken;
    return this;
  }
}
