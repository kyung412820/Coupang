package com.example.coupang.user.dto.request;

import lombok.Getter;

@Getter
public class DeleteUserRequestDto {
    private final String email;

    public DeleteUserRequestDto(String email) {
        this.email = email;
    }
}
