package com.example.coupang.user.controller;

import com.example.coupang.user.dto.MeResponseDto;
import com.example.coupang.user.dto.RegisterRequestDto;
import com.example.coupang.user.dto.RegisterResponseDto;
import com.example.coupang.user.entity.User;
import com.example.coupang.user.service.UserService;
import com.example.coupang.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> registerUser(@RequestBody RegisterRequestDto request) {
        // 회원가입 처리
        User user = userService.registerUser(request.getEmail(), request.getPassword());

        // 응답 반환
        RegisterResponseDto response = RegisterResponseDto.of(user.getEmail());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    public ResponseEntity<MeResponseDto> getAuthenticatedUserInfo() {
        // 인증된 사용자의 ID와 역할 목록을 가져옵니다.
        Long userId = AuthUtil.getId();

        // DTO를 사용하여 응답 반환
        MeResponseDto response = MeResponseDto.of(userId);

        return ResponseEntity.ok(response);
    }
}
