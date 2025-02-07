package com.example.coupang.auth.controller;


import com.example.coupang.auth.dto.LoginRequestDto;
import com.example.coupang.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequestDto authRequest) {
        // AuthService를 통해 인증 및 토큰 생성
        String token = authService.authenticate(authRequest);

        // Authorization 헤더에 토큰 추가
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        // 헤더만 포함하고 본문은 비움
        return ResponseEntity.ok()
                .headers(headers)
                .build();
    }
}

