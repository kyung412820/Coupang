package com.example.coupang.coupon.controller;

import com.example.coupang.coupon.dto.CouponRequestDto;
import com.example.coupang.coupon.dto.CouponResponseDto;
import com.example.coupang.coupon.service.DistributedLockCouponService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    private final DistributedLockCouponService distributedLockCouponService;

    public CouponController(DistributedLockCouponService distributedLockCouponService) {
        this.distributedLockCouponService = distributedLockCouponService;
    }

    /**
     * 쿠폰 발급 메서드
     *
     * @param couponRequestDto 발급할 쿠폰의 정보가 담긴 DTO
     * @return 발급된 쿠폰의 정보가 담긴 DTO
     */
    @PostMapping("/issue")
    public CouponResponseDto issueCoupon(@RequestBody CouponRequestDto couponRequestDto) {
        return distributedLockCouponService.issueCoupon(
                couponRequestDto.getCouponName(),
                couponRequestDto.getOff(),
                couponRequestDto.getStatus(),
                couponRequestDto.getExpDate()
        );
    }

//    /**
//     * 로그인한 유저의 모든 쿠폰을 조회하는 메서드
//     *
//     * @param userDetails 로그인한 유저의 세부 정보
//     * @return 로그인한 유저가 보유한 쿠폰 목록
//     */
//    @GetMapping
//    public List<CouponResponseDto> getCoupons(@AuthenticationPrincipal UserDetails userDetails) {
//        Long userId = userDetails.getId();
//        return distributedLockCouponService.getCoupons(userId); // 로그인한 유저의 쿠폰 조회
//    }
  }