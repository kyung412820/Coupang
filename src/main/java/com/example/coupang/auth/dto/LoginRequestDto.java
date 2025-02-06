package com.example.coupang.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor(force = true)  // 기본 생성자 강제 생성
@RequiredArgsConstructor
public class LoginRequestDto {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private final String email;

    @NotBlank(message = "Password cannot be blank")
    private final String password;
}

