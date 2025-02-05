package com.example.coupang.coupon.service;

import com.example.coupang.coupon.entity.Coupon;
import com.example.coupang.coupon.exception.CouponCustomException;
import com.example.coupang.coupon.repository.CouponRepository;
import com.example.coupang.user.entity.User;
import com.example.coupang.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimisticLockCouponService1 {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    private final int maxCoupons = 100; // 최대 발급 가능한 쿠폰 수

    @Transactional
    public Coupon tryOptimisticUpdate(String couponName, Long off, String couponStatus, LocalDateTime expDate) {
        long totalCouponsIssued = couponRepository.countCouponsByStatus("사용가능");

        if (totalCouponsIssued >= maxCoupons) {
            throw new CouponCustomException.CouponLimitExceededException("100개 이상의 쿠폰이 발급되었습니다.");
        }

        // 유저 데이터 가져오기
        List<User> allUsers = userRepository.findAll();
        User user = allUsers.get((int) (Math.random() * allUsers.size())); // 무작위 유저 선택

        // 해당 유저가 이미 쿠폰을 발급받았는지 체크
        Coupon coupon = couponRepository.findByUserIdAndStatusWithOptimistic(Long.valueOf(user.getId()), "사용가능");
        if (coupon != null) {
            if (coupon.getUseCount() >= coupon.getMaxCount()) {
                throw new CouponCustomException.CouponLimitExceededException("쿠폰이 모두 소진되었습니다.");
            }
            coupon.incrementUseCount();
            return couponRepository.save(coupon); // 쿠폰 저장
        } else {
            // 새로운 쿠폰 발급
            coupon = new Coupon(user, couponName, off, couponStatus, expDate, 0L, 1L);
            coupon.incrementUseCount();
            return couponRepository.save(coupon); // 새로운 쿠폰 저장
        }
    }
}