package com.example.coupang.coupon.service;

import com.example.coupang.coupon.repository.CouponRepository;
import com.example.coupang.coupon.exception.CouponCustomException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class PessimisticLockCouponServiceTest {

    @Autowired
    private PessimisticLockCouponService pessimisticLockCouponService;

    @Autowired
    private CouponRepository couponRepository;

    @AfterEach
    public void tearDown() {
        couponRepository.deleteAll();
    }

    @Test
    @DisplayName("비관적 락을 이용하여 쿠폰 발급 동시성 제어")
    public void 동시성_이슈_비관적락_제어_성공() throws InterruptedException {
        int threadCount = 1000;  // 테스트 스레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(1000);  // 스레드 풀 크기
        CountDownLatch latch = new CountDownLatch(threadCount);  // 스레드 동기화
        AtomicInteger successCount = new AtomicInteger();  // 성공한 요청 수
        AtomicInteger failureCount = new AtomicInteger();  // 실패한 요청 수

        // 각 스레드가 쿠폰 발급을 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pessimisticLockCouponService.issueCoupon(
                            "할인쿠폰", 20L, "사용가능", LocalDateTime.now().plusDays(7)
                    );
                    successCount.incrementAndGet();  // 성공 시 증가
                } catch (CouponCustomException.CouponLimitExceededException e) {
                    failureCount.incrementAndGet();  // 실패 시 증가
                    System.out.println("쿠폰 발급 한도 초과: " + e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet();  // 기타 예외 발생 시
                    System.out.println("예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown();  // 카운트다운
                }
            });
        }

        latch.await();  // 모든 스레드가 완료될 때까지 대기
        executorService.shutdown();

        // 발급된 쿠폰 수 확인
        long totalCouponsIssued = couponRepository.countCouponsByStatus("사용가능");  // 전체 발급된 쿠폰 수

        System.out.println("전체 조회된 쿠폰 개수: " + totalCouponsIssued);
        assertThat(totalCouponsIssued).isEqualTo(100);  // 정상적으로 100개 쿠폰이 발급되었는지 확인
        System.out.println("성공한 요청 수: " + successCount.get());
        System.out.println("실패한 요청 수: " + failureCount.get());
    }
}