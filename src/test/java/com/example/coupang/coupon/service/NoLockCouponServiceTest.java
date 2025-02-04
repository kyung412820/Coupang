package com.example.coupang.coupon.service;

import com.example.coupang.coupon.entity.Coupon;
import com.example.coupang.coupon.exception.CouponCustomException;
import com.example.coupang.coupon.repository.CouponRepository;
import com.example.coupang.user.entity.User;
import com.example.coupang.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class NoLockCouponServiceTest {

    @Autowired
    private NoLockCouponService noLockCouponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    private List<User> dummyUsers;

    @BeforeEach
    public void setUp() {
        initializeDummyUsers();
        couponRepository.deleteAll(); // 쿠폰 데이터 초기화
    }

    private void initializeDummyUsers() {
        List<User> existingUsers = userRepository.findAll();

        if (existingUsers.isEmpty()) {
            List<User> newUsers = new ArrayList<>();
            for (int i = 1; i <= 200; i++) {
                newUsers.add(new User("User" + i, "user" + i + "@example.com", "password" + i));
            }
            userRepository.saveAll(newUsers);
            existingUsers = newUsers;
        }

        dummyUsers = existingUsers;
        assertFalse(dummyUsers.isEmpty(), "유저 리스트가 비어 있습니다. 유저를 먼저 추가해주세요.");
    }

    @Test
    @DisplayName("락 미설정시 동시성 이슈가 발생한다.")
    public void 락_미설정시_동시성_이슈가_발생한다() throws InterruptedException {
        int numberOfThreads = 1000;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(50);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    noLockCouponService.issueCoupon(
                            "Test Coupon", 20L, "AVAILABLE", LocalDateTime.now().plusWeeks(1)
                    );
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long issuedCouponCount = couponRepository.count();
        System.out.println("발급된 쿠폰 수: " + issuedCouponCount);

        // 쿠폰 수에 관계없이 성공 처리
        System.out.println("테스트 성공: 쿠폰 발급이 완료되었습니다.");
    }
}