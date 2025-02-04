package com.example.coupang.coupon.service;

import com.example.coupang.coupon.entity.Coupon;
import com.example.coupang.coupon.repository.CouponRepository;
import com.example.coupang.coupon.dto.CouponResponseDto;
import com.example.coupang.coupon.exception.CouponCustomException;
import com.example.coupang.user.entity.User;
import com.example.coupang.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PessimisticLockCouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    /**
     * 비관적 락을 이용한 쿠폰 발급 메서드
     *
     * @param couponName 발급할 쿠폰의 이름
     * @param off        쿠폰의 할인액
     * @param status     쿠폰의 상태
     * @param expDate    쿠폰의 만료 날짜
     * @return 발급된 쿠폰의 정보가 담긴 DTO
     * @throws CouponCustomException.CouponLimitExceededException 쿠폰이 모두 소진된 경우
     */
    @Transactional
    public CouponResponseDto issueCoupon(String couponName, Long off, String status, LocalDateTime expDate) {
        List<User> dummyUsers = userRepository.findAll();
        User user = dummyUsers.get((int) (Math.random() * dummyUsers.size())); // 무작위 유저 선택

        int maxCoupons = 100; // 전체 발급 가능한 쿠폰 수

        // 1. 쿠폰 발급 수 확인 및 락 걸기
        List<Coupon> coupons = couponRepository.findCouponsByStatusWithPessimisticLock("사용가능");

        long totalCouponsIssued = coupons.size();  // 현재 발급된 쿠폰 수 확인

        if (totalCouponsIssued >= maxCoupons) {
            // 100개 이상의 쿠폰이 발급된 경우 예외 발생
            throw new CouponCustomException.CouponLimitExceededException("100개 이상의 쿠폰이 발급되었습니다.");
        }

        // 2. 유저가 이미 발급받은 쿠폰 확인
        long userCouponCount = couponRepository.findAllByUserId(user.getId()).size();
        if (userCouponCount >= 1) {
            throw new CouponCustomException.CouponLimitExceededException("이 유저는 이미 쿠폰을 발급받았습니다.");
        }

        // 3. 새로운 쿠폰 발급
        Coupon coupon = new Coupon(user, couponName, off, status, expDate, 0L, 1L);  // 한 사용자당 한 쿠폰만 발급
        coupon.incrementUseCount();

        // 5. 발급된 쿠폰 정보 반환
        return new CouponResponseDto(coupon.getId(), coupon.getCouponName(), coupon.getOff(), coupon.getStatus(), coupon.getExpDate());
    }
}