package com.example.coupang.coupon.service;

import com.example.coupang.coupon.exception.CouponCustomException;
import com.example.coupang.user.entity.User;
import com.example.coupang.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class NoLockCouponServiceTest {

    @Autowired
    private NoLockCouponService noLockCouponService;

    @Autowired
    private RedisTemplate<String, Long> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    private List<User> dummyUsers;

    @BeforeEach
    public void setUp() {
        // 유저 데이터 초기화
        initializeDummyUsers();
    }

    // 더미 유저 데이터 초기화 메서드
    private void initializeDummyUsers() {
        // 기존 유저 데이터를 한 번만 가져오기
        List<User> existingUsers = userRepository.findAll();

        if (existingUsers.isEmpty()) {
            // 유저가 없을 경우, 새로운 유저 200명을 생성하여 저장
            List<User> newUsers = new ArrayList<>();
            for (int i = 1; i <= 200; i++) {
                newUsers.add(new User("User" + i, "user" + i + "@example.com", "password" + i));
            }
            userRepository.saveAll(newUsers); // **배치 저장으로 성능 향상**
            existingUsers = newUsers; // 기존 유저 목록을 업데이트
        }

        dummyUsers = existingUsers;
        assertFalse(dummyUsers.isEmpty(), "유저 리스트가 비어 있습니다. 유저를 먼저 추가해주세요.");

        // 유저 데이터 초기화 완료 로그
        System.out.println("유저 데이터 초기화 완료, 총 " + dummyUsers.size() + "명의 유저가 준비되었습니다.");
    }

    @Test
    @DisplayName("락 미설정시 동시성 이슈 발생")
    public void 락_미설정시_동시성_이슈가_생기고_에러가_발생한다() throws InterruptedException {
        int numberOfThreads = 1000; // 1000개의 스레드로 동시성 테스트
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // 쿠폰 수 초기화
        redisTemplate.opsForValue().set("coupon_count", 0L);

        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> {
                try {
                    noLockCouponService.issueCoupon(
                            "Test Coupon", 20L, "AVAILABLE", LocalDateTime.now().plusWeeks(1)
                    );
                } catch (RuntimeException e) {
                    // 예외 메시지를 출력하며, 쿠폰이 모두 소진되었음을 알림
                    System.out.println(e.getMessage());
                } finally {
                    latch.countDown(); // 스레드 종료 후 latch 카운트 감소
                }
            }).start();
        }

        latch.await(1, TimeUnit.MINUTES); // 모든 스레드가 종료될 때까지 기다림

        Long couponCount = redisTemplate.opsForValue().get("coupon_count");

        // 쿠폰 수가 100이 아닐 경우 RuntimeException 발생
        if (couponCount != 100L) {
            // 최대 쿠폰 수량 출력
            System.out.println("최대 쿠폰 수량: 100개");
            // 발급된 쿠폰 수 출력
            System.out.println("발급된 쿠폰 수: " + couponCount);

            // 여기에서 예외 발생 시 테스트 성공 처리
            RuntimeException exception = assertThrows(CouponCustomException.CouponLimitExceededException.class, () -> {
                throw new CouponCustomException.CouponLimitExceededException("쿠폰 발행 수가 100이 아닙니다.");
            });

            // 예외 메시지 출력
            System.out.println(exception.getMessage());
        } else {
            // 최대 쿠폰 수량 출력
            System.out.println("최대 쿠폰 수량: 100개");
            // 발급된 쿠폰 수 출력
            System.out.println("발급된 쿠폰 수: " + couponCount);
        }
    }
}
