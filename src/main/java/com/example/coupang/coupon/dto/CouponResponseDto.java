package com.example.coupang.coupon.dto;

import com.example.coupang.coupon.entity.Coupon;
import lombok.Getter;

import java.time.LocalDate;
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

    // 팩토리 메서드: Coupon 객체를 DTO로 변환
    public static CouponResponseDto from(Coupon coupon) {
        // LocalDateTime을 LocalDate로 변환
        LocalDate expDate = coupon.getExpDate().toLocalDate();

        return new CouponResponseDto(
                coupon.getId(),
                coupon.getCouponName(),
                coupon.getOff(),
                coupon.getStatus(),
                coupon.getExpDate());
    }
}