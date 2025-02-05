package com.example.coupang.coupon.controller;

import com.example.coupang.coupon.dto.CouponRequestDto;
import com.example.coupang.coupon.dto.CouponResponseDto;
import com.example.coupang.coupon.service.DistributedLockCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {

    private final DistributedLockCouponService distributedLockCouponService;

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
    @GetMapping
    public ResponseEntity<List<CouponResponseDto>> getUserCoupons(Principal principal) {
        // principal이 null인지 체크
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 인증되지 않은 경우
        }

        // principal.getName()에서 사용자 ID를 가져오기 (문자열을 Long으로 변환)
        String userId;
        try {
            // getName()이 반환하는 값이 ID 문자열이라고 가정
            userId = principal.getName(); // 문자열을 Long으로 변환
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 변환 실패 시 처리
        }

        // 사용자 ID로 쿠폰 조회
        List<CouponResponseDto> coupons = distributedLockCouponService.getCouponsByUserId(userId);

        return ResponseEntity.ok(coupons); // 쿠폰 반환
    }
}