package com.example.coupang.coupon.service;

import com.example.coupang.coupon.repository.CouponRepository;
import com.example.coupang.coupon.exception.CouponCustomException;
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
    private OptimisticLockCouponService optimisticLockCouponService;

    @Autowired
    private CouponRepository couponRepository;

    private static final int MAX_RETRIES = 5; // 낙관적 락 충돌 시 최대 재시도 횟수

    @Test
    @DisplayName("낙관적 락을 이용하여 동시성 제어")
    public void 동시성_이슈_낙관적락_제어_성공() throws InterruptedException {
        int threadCount = 1000;  // 테스트 스레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(5);  // 스레드 개수 제한
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                int retries = 0;
                while (retries < MAX_RETRIES) { // 최대 5번 재시도
                    try {
                        // 쿠폰 개수 초과 여부를 미리 체크하여 불필요한 요청 방지
                        long currentCoupons = couponRepository.countCouponsByStatus("사용가능");
                        if (currentCoupons >= 100) {
                            System.out.println("쿠폰 발급 한도 초과! 현재 개수: " + currentCoupons);
                            break;  // 초과하면 더 이상 시도하지 않음
                        }

                        optimisticLockCouponService.issueCoupon(
                                "할인쿠폰", 20L, "사용가능", LocalDateTime.now().plusDays(7)
                        );
                        successCount.incrementAndGet();
                        break;  // 성공하면 루프 종료
                    } catch (CouponCustomException.CouponLimitExceededException e) {
                        failureCount.incrementAndGet();
                        System.out.println("쿠폰 발급 한도 초과: " + e.getMessage());
                        break;  // 한도 초과 시 재시도 없이 종료
                    } catch (OptimisticLockException e) {
                        retries++;  // 충돌 시 재시도 증가
                        System.out.println("Optimistic Lock 충돌 발생 (재시도 " + retries + "): " + e.getMessage());
                        if (retries >= MAX_RETRIES) {
                            failureCount.incrementAndGet();
                            System.out.println("최대 재시도 횟수 초과: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.out.println("예외 발생: " + e.getMessage());
                        break;
                    }
                }
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        // 발급된 쿠폰 개수 확인
        long totalCouponsIssued = couponRepository.countCouponsByStatus("사용가능");

        System.out.println("발급된 쿠폰 개수: " + totalCouponsIssued);
        System.out.println("성공한 요청 수: " + successCount.get());
        System.out.println("실패한 요청 수: " + failureCount.get());

        // 쿠폰이 100개 초과되지 않도록 확인
        assertThat(totalCouponsIssued).isLessThanOrEqualTo(100);
    }
}