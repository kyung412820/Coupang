package com.example.coupang.user.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor(staticName = "of") // 팩토리 메서드 생성
public class MeResponseDto {
    private final Long id; // final 필드
    private final List<String> roles; // final 필드
}
