package com.example.coupang.coupon.service;

import com.example.coupang.coupon.exception.CouponCustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimisticLockCouponService2 {

    private final OptimisticLockCouponService1 optimisticLockCouponService1;

    private final int retryCount = 5;  // 최대 5번 재시도
    private final long retrySleepTime = 200L;  // 재시도 간 대기 시간 (200ms)

    public void issueCoupons(String couponName, Long off, String couponStatus, LocalDateTime expDate) throws InterruptedException {
        int attemptCount = 0;

        while (attemptCount <= retryCount) {
            try {
                // 쿠폰 발급 시도
                optimisticLockCouponService1.tryOptimisticUpdate(couponName, off, couponStatus, expDate);
                return;  // 성공적으로 쿠폰 발급 후 종료

            } catch (ObjectOptimisticLockingFailureException e) {
                // 낙관적 락 충돌 예외가 발생하면 재시도
                log.warn("쿠폰 발급 실패. 재시도 횟수: {}", attemptCount + 1);
                attemptCount++;
                if (attemptCount <= retryCount) {
                    // 지정된 시간만큼 대기 후 재시도
                    Thread.sleep(retrySleepTime);
                } else {
                    // 재시도 횟수 초과 시 예외 던짐
                    throw new CouponCustomException.CouponLimitExceededException("쿠폰 발급 실패: 재시도 횟수 초과");
                }
            } catch (CouponCustomException.CouponLimitExceededException e) {
                // 쿠폰 소진 시 예외 던짐
                throw new CouponCustomException.CouponLimitExceededException("쿠폰이 모두 소진되었습니다.");
            }
        }

        // 재시도 횟수 초과 후 실패 처리
        throw new CouponCustomException.CouponLimitExceededException("쿠폰 발급 실패");
    }
}
