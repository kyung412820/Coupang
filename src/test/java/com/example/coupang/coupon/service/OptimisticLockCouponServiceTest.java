package com.example.coupang.coupon.service;

import com.example.coupang.coupon.exception.CouponCustomException;
import com.example.coupang.coupon.repository.CouponRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OptimisticLockCouponServiceTest {

    @Autowired
    private OptimisticLockCouponService2 optimisticLockCouponService;

    @Autowired
    private CouponRepository couponRepository;

    private final int maxCoupons = 100; // 최대 발급 가능한 쿠폰 수

    @Test
    @DisplayName("낙관적 락을 이용하여 동시성 제어")
    public void 동시성_이슈_낙관적락_제어_성공() throws InterruptedException {
        int threadCount = 1000; // 테스트 스레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(1); // 스레드 개수 제한
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 쿠폰 발급 시도
                    optimisticLockCouponService.issueCoupons("할인쿠폰", 20L, "사용가능", LocalDateTime.now().plusDays(7));
                    successCount.incrementAndGet(); // 성공한 요청 수 증가
                } catch (CouponCustomException.CouponLimitExceededException e) {
                    failureCount.incrementAndGet(); // 실패한 요청 수 증가
                    System.out.println("쿠폰 발급 한도 초과: " + e.getMessage());
                } catch (OptimisticLockException e) {
                    failureCount.incrementAndGet(); // 실패한 요청 수 증가
                    System.out.println("Optimistic Lock 충돌 발생: " + e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet(); // 실패한 요청 수 증가
                    System.out.println("예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown(); // 스레드가 끝날 때 카운트 다운
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기
        executorService.shutdown(); // ExecutorService 종료

        // 발급된 쿠폰 개수 확인
        long totalCouponsIssued = couponRepository.countCouponsByStatus("사용가능");

        System.out.println("발급된 쿠폰 개수: " + totalCouponsIssued);
        System.out.println("성공한 요청 수: " + successCount.get());
        System.out.println("실패한 요청 수: " + failureCount.get());

        // 쿠폰이 100개 초과되지 않도록 확인
        assertThat(totalCouponsIssued).isLessThanOrEqualTo(100);
    }
}
