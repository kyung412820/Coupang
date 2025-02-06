package com.example.coupang.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor(force = true)  // 기본 생성자 강제 생성
@RequiredArgsConstructor(staticName = "of") // 팩토리 메서드 생성
public class MeResponseDto {
    private final Long id; // final 필드
}
