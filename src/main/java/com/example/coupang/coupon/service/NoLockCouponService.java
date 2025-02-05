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
public class NoLockCouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    private static final long MAX_COUPON_COUNT = 100L; // 최대 쿠폰 수

    /**
     * 선착순으로 쿠폰을 발급하는 메서드 (락 없이, 데이터베이스 기반으로 변경)
     *
     * @param couponName 발급할 쿠폰의 이름
     * @param off 쿠폰의 할인액
     * @param status 쿠폰의 상태
     * @param expDate 쿠폰의 만료 날짜
     * @return 발급된 쿠폰의 정보가 담긴 DTO
     * @throws RuntimeException 쿠폰이 모두 소진된 경우
     */
    @Transactional
    public CouponResponseDto issueCoupon(String couponName, Long off, String status, LocalDateTime expDate) {
        // 현재 발급된 쿠폰 개수 확인
        long couponCount = couponRepository.count();

        if (couponCount >= MAX_COUPON_COUNT) {
            throw new CouponCustomException.CouponLimitExceededException("쿠폰이 모두 소진되었습니다.");
        }

        // 유저 데이터 가져오기
        List<User> dummyUsers = userRepository.findAll();
        User user = dummyUsers.get((int) (Math.random() * dummyUsers.size())); // 무작위 유저 선택

        // 쿠폰 생성 및 저장
        Coupon coupon = new Coupon(user, couponName, off, status, expDate, 0L, 1L);
        couponRepository.save(coupon);

        return new CouponResponseDto(coupon.getId(), coupon.getCouponName(), coupon.getOff(), coupon.getStatus(), coupon.getExpDate());
    }
}
