package com.example.coupang.coupon.service;

import com.example.coupang.user.entity.User;
import com.example.coupang.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CouponServiceTest {

    @Autowired
    private DistributedLockCouponService distributedLockCouponService;

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    private List<User> dummyUsers;

    @BeforeEach
    public void setUp() {

        // 쿠폰 수 초기화 (Redis에 저장된 coupon_count를 0으로 설정)
        redisTemplate.opsForValue().set("coupon_count", 0L);

        // 유저 데이터 초기화
        dummyUsers = userRepository.findAll();
        if (dummyUsers.isEmpty()) {
            throw new IllegalStateException("유저 리스트가 비어 있습니다. 유저를 먼저 추가해주세요.");
        }
    }

    @Test
    public void 쿠폰_발급_한도_초과시_예외_발생() {
        // Redis에서 쿠폰 수가 100일 때의 동작을 mock
        redisTemplate.opsForValue().set("coupon_count", 100L);

        // 101번째 쿠폰 발급 시도, 예외가 발생하는지 확인
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            distributedLockCouponService.issueCoupon(
                    "Coupon 101", 20L, "AVAILABLE", LocalDateTime.now().plusWeeks(1)
            );
        });

        // 예외 메시지가 올바르게 나오는지 검증
        assertEquals("쿠폰이 모두 소진되었습니다.", exception.getMessage());  // "쿠폰이 모두 소진되었습니다."라는 예외 메시지를 확인
    }

    @Test
    public void 동시성_이슈_검증() throws InterruptedException {
        int numberOfThreads = 200;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> {
                try {
                    distributedLockCouponService.issueCoupon(
                            "Test Coupon", 20L, "AVAILABLE", LocalDateTime.now().plusWeeks(1)
                    );
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(1, TimeUnit.MINUTES);

        Long couponCount = redisTemplate.opsForValue().get("coupon_count");
        assertEquals(100L, couponCount);  // 쿠폰 수가 100이어야 한다
        System.out.println("발급된 쿠폰 수: " + couponCount);
    }

    @Test
    public void 동시성_이슈_제어_성공() throws InterruptedException {
        // 200명의 유저가 동시에 쿠폰을 요청하는 상황을 시뮬레이션
        int numberOfThreads = 200;

        // 100개 이상의 쿠폰 발급을 막기 위한 CountDownLatch
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // 여러 스레드가 동시에 쿠폰을 발급 받으려고 시도
        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> {
                try {
                    distributedLockCouponService.issueCoupon(
                            "Test Coupon", 20L, "AVAILABLE", LocalDateTime.now().plusWeeks(1)
                    );
                } catch (Exception e) {
                } finally {
                    latch.countDown();  // 스레드 종료 후 latch 카운트 감소
                }
            }).start();
        }

        // 모든 스레드가 종료될 때까지 기다림
        latch.await(1, TimeUnit.MINUTES);

        // 쿠폰 발급이 100개로 제한된 것이 맞는지 확인
        Long couponCount = redisTemplate.opsForValue().get("coupon_count");
        assertEquals(100L, couponCount);
        System.out.println("발급된 쿠폰 수: " + couponCount);

        // 쿠폰 발급이 완료된 후 Redis에 저장된 쿠폰 수가 100개여야만 함
        Long finalCouponCount = redisTemplate.opsForValue().get("coupon_count");
        assertEquals(100L, finalCouponCount);
        System.out.println("Redis에 저장된 쿠폰 수: " + finalCouponCount);
    }
}
