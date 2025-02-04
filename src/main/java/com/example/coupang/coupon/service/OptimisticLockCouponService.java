package com.example.coupang.coupon.service;

import com.example.coupang.coupon.entity.Coupon;
import com.example.coupang.coupon.repository.CouponRepository;
import com.example.coupang.coupon.exception.CouponCustomException;
import com.example.coupang.user.entity.User;
import com.example.coupang.user.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimisticLockCouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final PlatformTransactionManager transactionManager;  // 수동 트랜잭션 관리

    private final int retryCount = 5;  // 최대 5번 재시도
    private final long retrySleepTime = 200L;  // 재시도 간 대기 시간 (200ms)

    public Coupon issueCoupon(String couponName, Long off, String couponStatus, LocalDateTime expDate) throws InterruptedException {
        final int maxCoupons = 100;  // 최대 발급 가능한 쿠폰 수
        long totalCouponsIssued = couponRepository.countCouponsByStatus("사용가능");

        if (totalCouponsIssued >= maxCoupons) {
            throw new CouponCustomException.CouponLimitExceededException("100개 이상의 쿠폰이 발급되었습니다.");
        }

        // 더미 유저 조회
        List<User> allUsers = userRepository.findAll();  // 모든 유저 조회
        if (allUsers.isEmpty()) {
            throw new CouponCustomException.CouponLimitExceededException("유저가 존재하지 않습니다.");
        }
        User user = allUsers.get((int) (Math.random() * allUsers.size())); // 무작위 유저 선택

        // 해당 유저가 이미 쿠폰을 발급받았는지 체크
        List<Coupon> issuedCoupons = couponRepository.findAllByUserId(user.getId());
        if (!issuedCoupons.isEmpty()) {
            throw new CouponCustomException.CouponLimitExceededException("이 유저는 이미 쿠폰을 발급받았습니다.");
        }

        Coupon coupon = null;
        int attemptCount = 0;

        while (attemptCount <= retryCount) {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                // 낙관적 락을 사용하여 기존 쿠폰을 조회
                coupon = couponRepository.findByUserIdAndStatusWithOptimistic(user.getId(), "사용가능");

                // 쿠폰이 있으면 발급된 쿠폰의 useCount를 증가시킴
                if (coupon != null) {
                    if (coupon.getUseCount() >= coupon.getMaxCount()) {
                        throw new CouponCustomException.CouponLimitExceededException("쿠폰이 모두 소진되었습니다.");
                    }
                    coupon.incrementUseCount();
                    couponRepository.saveAndFlush(coupon);  // 즉시 저장
                } else {
                    // 새로운 쿠폰 발급
                    coupon = new Coupon(user, couponName, off, couponStatus, expDate, 0L, 1L);
                    coupon.incrementUseCount();
                    couponRepository.saveAndFlush(coupon);  // 즉시 저장
                }

                // 발급 후 전체 쿠폰 수 다시 체크
                totalCouponsIssued = couponRepository.countCouponsByStatus("사용가능");
                if (totalCouponsIssued > maxCoupons) {
                    throw new CouponCustomException.CouponLimitExceededException("100개 이상의 쿠폰이 발급되었습니다.");
                }

                transactionManager.commit(transactionStatus);  // 트랜잭션 커밋
                return coupon;  // 성공적으로 쿠폰 발급 후 종료

            } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
                // 낙관적 락 예외가 발생하면 재시도
                log.warn("낙관적 락 충돌 발생. 재시도 횟수: {}", attemptCount + 1);
                transactionManager.rollback(transactionStatus);  // 트랜잭션 롤백
                attemptCount++;
                if (attemptCount <= retryCount) {
                    // 지정된 시간만큼 대기 후 재시도
                    Thread.sleep(retrySleepTime);
                } else {
                    // 재시도 횟수 초과 시 예외 던짐
                    throw new CouponCustomException.CouponLimitExceededException("낙관적 락 충돌로 인한 재시도 횟수 초과");
                }
            } catch (Exception e) {
                // 일반 예외 처리
                transactionManager.rollback(transactionStatus);  // 예외 발생 시 롤백
                throw e;  // 예외를 던짐
            }
        }

        // 재시도 횟수 초과 후 실패 처리
        throw new CouponCustomException.CouponLimitExceededException("쿠폰 발급 실패");
    }
}