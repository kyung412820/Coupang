package com.example.coupang.coupon.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CouponRequestDto {

    private String couponName;
    private Long off;
    private String status;
    private LocalDateTime expDate;

    public CouponRequestDto(String couponName, Long off, String status, LocalDateTime expDate) {
        this.couponName = couponName;
        this.off = off;
        this.status = status;
        this.expDate = expDate;
    }
}
