package com.example.coupang.coupon.dto;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CouponResponseDto {

    private Long couponId;
    private String couponName;
    private Long off;
    private String status;
    private LocalDateTime expDate;

    public CouponResponseDto(Long couponId, String couponName, Long off, String status, LocalDateTime expDate) {
        this.couponId = couponId;
        this.couponName = couponName;
        this.off = off;
        this.status = status;
        this.expDate = expDate;
    }
}