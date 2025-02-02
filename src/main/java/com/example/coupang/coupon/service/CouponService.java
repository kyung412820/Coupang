package com.example.coupang.coupon.service;

import com.example.coupang.coupon.entity.Coupon;
import com.example.coupang.coupon.repository.CouponRepository;
import com.example.coupang.coupon.dto.CouponResponseDto;
import com.example.coupang.user.entity.User;
import com.example.coupang.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;  // 의존성 주입을 위한 필드
    private final RedisTemplate<String, Long> redisTemplate;

    private static final String COUPON_COUNT_KEY = "coupon_count"; // 쿠폰 수량을 저장할 Redis 키
    private static final long MAX_COUPON_COUNT = 100L; // 최대 쿠폰 수

    private final Lock lock = new ReentrantLock(); // 동시성 제어를 위한 Lock

    // 더미 유저 리스트
    private List<User> dummyUsers;

    // 더미 유저 데이터 초기화 (한 번만 호출되도록)
    public void initializeDummyUsers() {
        // 유저 리스트가 비어있으면 UserDataInit 클래스를 통해 유저 데이터를 추가
        if (dummyUsers == null || dummyUsers.isEmpty()) {
            dummyUsers = userRepository.findAll();  // 이미 생성된 유저 목록 가져오기
            if (dummyUsers.isEmpty()) {
                throw new IllegalStateException("유저 리스트가 비어 있습니다. 유저를 먼저 추가해주세요.");
            }
        }
        log.info("유저 데이터 초기화 완료, 총 {}명의 유저가 준비되었습니다.", dummyUsers.size());
    }

    // 선착순으로 쿠폰 발급
    public CouponResponseDto issueCoupon(String couponName, Long off, String status, LocalDateTime expDate) {
        lock.lock();  // 락을 걸어 동시성 문제 해결

        try {
            // 쿠폰 발급 수량 확인
            Long couponCount = redisTemplate.opsForValue().get(COUPON_COUNT_KEY);

            if (couponCount == null) {
                couponCount = 0L; // 초기화
            }

            if (couponCount >= MAX_COUPON_COUNT) {
                // 쿠폰 수량이 100개 이상이면 더 이상 발급 불가
                throw new RuntimeException("쿠폰이 모두 소진되었습니다.");
            }

            initializeDummyUsers();

            // dummyUsers가 비어있지 않은지 체크
            if (dummyUsers.isEmpty()) {
                throw new IllegalStateException("유저 리스트가 비어 있습니다. 유저를 먼저 추가해주세요.");
            }

            // 유저가 발급을 받았을 때
            User user = dummyUsers.get((int) (couponCount % dummyUsers.size())); // 유저를 순차적으로 할당

            // 쿠폰 생성
            Coupon coupon = new Coupon(user, couponName, off, status, expDate, 0L, 1L);
            couponRepository.save(coupon);

            // 발급된 쿠폰 수량을 Redis에 저장하고 증가
            redisTemplate.opsForValue().increment(COUPON_COUNT_KEY, 1);

            return new CouponResponseDto(coupon.getId(), coupon.getCouponName(), coupon.getOff(), coupon.getStatus(), coupon.getExpDate());
        } finally {
            lock.unlock(); // 락 해제
        }
    }

    // 유저의 모든 쿠폰 조회
    public List<CouponResponseDto> getCoupons(Long userId) {
        List<Coupon> coupons = couponRepository.findAllByUserId(userId);
        return coupons.stream()
                .map(coupon -> new CouponResponseDto(coupon.getId(), coupon.getCouponName(), coupon.getOff(), coupon.getStatus(), coupon.getExpDate()))
                .collect(Collectors.toList());
    }
}
