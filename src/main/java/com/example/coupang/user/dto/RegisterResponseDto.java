package com.example.coupang.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class RegisterResponseDto {

    private final String email;
}

