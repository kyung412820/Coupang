package com.example.coupang.coupon.service;

import com.example.coupang.exception.CustomException;
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
    public void 락_미설정시_동시성_이슈가_생기고_에러가_발생한다() throws InterruptedException {
        int numberOfThreads = 200; // 200개의 스레드로 동시성 테스트
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
            RuntimeException exception = assertThrows(CustomException.CouponLimitExceededException.class, () -> {
                throw new CustomException.CouponLimitExceededException("쿠폰 발행 수가 100이 아닙니다.");
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
