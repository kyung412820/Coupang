package com.example.coupang.refreshtoken.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("RefreshToken")  // Redis에 저장할 때 사용할 해시 이름 (테이블 이름과 유사한 개념)
public class RefreshToken implements Serializable {

  @Id
  private String id;
  private String userId;
  private String refreshToken;

  public RefreshToken(String userId, String refreshToken) {
    this.id = UUID.randomUUID().toString();
    this.userId = userId;
    this.refreshToken = refreshToken;
  }

  public RefreshToken update(String newRefreshToken) {
    this.refreshToken = newRefreshToken;
    return this;
  }
}
