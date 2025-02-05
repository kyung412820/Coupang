package com.example.coupang.securiy.token.controller;

import com.example.coupang.securiy.token.dto.CreateAccessTokenRequest;
import com.example.coupang.securiy.token.dto.CreateAccessTokenResponse;
import com.example.coupang.securiy.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

public class TokenController {
    @RequiredArgsConstructor
    @RestController
    public class TokenApiController {
        private  final TokenService tokenService;

        @PostMapping("/api/token")
        public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken
                (@RequestBody CreateAccessTokenRequest request) {
            String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CreateAccessTokenResponse(newAccessToken));
        }
    }
}
