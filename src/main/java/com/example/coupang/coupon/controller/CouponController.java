package com.example.coupang.coupon.controller;

import com.example.coupang.coupon.dto.CouponRequestDto;
import com.example.coupang.coupon.dto.CouponResponseDto;
import com.example.coupang.coupon.service.DistributedLockCouponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /**
     * 로그인한 유저의 모든 쿠폰을 조회하는 메서드
     *
     * @return 로그인한 유저가 보유한 쿠폰 목록
     */
//    @GetMapping
//    public ResponseEntity<List<CouponResponseDto>> getUserCoupons() {
//        Long userId = AuthUtil.getId(); // 현재 로그인한 사용자의 ID 가져오기
//        if (userId == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 인증되지 않은 경우
//        }
//        List<CouponResponseDto> coupons = distributedLockCouponService.getCouponsByUserId(userId); // 사용자 ID로 쿠폰 조회
//        return ResponseEntity.ok(coupons); // 쿠폰 반환
//    }
  }