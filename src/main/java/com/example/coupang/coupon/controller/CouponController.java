package com.example.coupang.coupon.controller;

import com.example.coupang.coupon.dto.CouponRequestDto;
import com.example.coupang.coupon.dto.CouponResponseDto;
import com.example.coupang.coupon.service.CouponService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    // 쿠폰 발급 (요청 본문에서 CouponRequestDto 받기)
    @PostMapping("/issue")
    public CouponResponseDto issueCoupon(@RequestBody CouponRequestDto couponRequestDto) {
        // userDetails를 통해 로그인된 유저 정보 접근 가능
        // Long userId = userDetails.getId();
        // 하드코딩된 더미 유저 ID (예: 유저 ID = 1)
        Long dummyUserId = 1L;

        return couponService.issueCoupon(
                couponRequestDto.getCouponName(),
                couponRequestDto.getOff(),
                couponRequestDto.getStatus(),
                couponRequestDto.getExpDate()
        );
    }

    // 로그인한 유저의 모든 쿠폰 조회 (하드코딩된 유저 ID 사용)
    @GetMapping
    public List<CouponResponseDto> getCoupons() {
        // 하드코딩된 더미 유저 ID (예: 유저 ID = 1)
        Long dummyUserId = 1L;

        return couponService.getCoupons(dummyUserId); // 더미 유저의 쿠폰 조회
    }

//    // 로그인한 유저의 모든 쿠폰 조회
//    @GetMapping
//    public List<CouponResponseDto> getCoupons(@AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = userDetails.getId();
//        return couponService.getCoupons(userId); // 로그인한 유저의 쿠폰 조회
//    }
}