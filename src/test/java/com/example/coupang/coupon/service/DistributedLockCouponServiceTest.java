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
public class DistributedLockCouponServiceTest {

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
        initializeDummyUsers(); // 유저 데이터 추가 메서드 호출
    }

    // 더미 유저 데이터 초기화 메서드
    private void initializeDummyUsers() {
        // 유저 리스트가 비어있으면 UserRepository를 통해 유저 데이터를 추가
        if (dummyUsers == null || dummyUsers.isEmpty()) {
            dummyUsers = userRepository.findAll();  // 이미 생성된 유저 목록 가져오기
            if (dummyUsers.isEmpty()) {
                // 유저가 없다면 더미 유저 200명 생성
                for (int i = 1; i <= 200; i++) {
                    User user = new User("User" + i, "user" + i + "@example.com", "password" + i);
                    userRepository.save(user); // 유저 저장
                }
                // 새로 생성된 유저 목록을 가져오기
                dummyUsers = userRepository.findAll();
            }
            assertFalse(dummyUsers.isEmpty(), "유저 리스트가 비어 있습니다. 유저를 먼저 추가해주세요.");
        }
        // 유저 데이터 초기화 완료 로그
        System.out.println("유저 데이터 초기화 완료, 총 " + dummyUsers.size() + "명의 유저가 준비되었습니다.");
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
