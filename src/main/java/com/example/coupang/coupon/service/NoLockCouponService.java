package com.example.coupang.coupon.service;

import com.example.coupang.coupon.entity.Coupon;
import com.example.coupang.coupon.repository.CouponRepository;
import com.example.coupang.coupon.dto.CouponResponseDto;
import com.example.coupang.exception.CustomException;
import com.example.coupang.user.entity.User;
import com.example.coupang.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoLockCouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Long> redisTemplate;

    private static final String COUPON_COUNT_KEY = "coupon_count"; // 쿠폰 수량을 저장할 Redis 키
    private static final long MAX_COUPON_COUNT = 100L; // 최대 쿠폰 수

    /**
     * 선착순으로 쿠폰을 발급하는 메서드 (락 없이)
     *
     * @param couponName 발급할 쿠폰의 이름
     * @param off 쿠폰의 할인액
     * @param status 쿠폰의 상태
     * @param expDate 쿠폰의 만료 날짜
     * @return 발급된 쿠폰의 정보가 담긴 DTO
     * @throws RuntimeException 쿠폰이 모두 소진된 경우
     */
    @Transactional
    public CouponResponseDto issueCoupon(String couponName, Long off, String status, LocalDateTime expDate) {
        // 쿠폰 발급 수량 확인
        Long couponCount = redisTemplate.opsForValue().get(COUPON_COUNT_KEY);

        if (couponCount == null) {
            couponCount = 0L; // 초기화
        }

        if (couponCount >= MAX_COUPON_COUNT) {
            // 쿠폰 수량이 100개 초과하면 더 이상 발급 불가
            throw new CustomException.CouponLimitExceededException("쿠폰이 모두 소진되었습니다.");
        }

        // 유저 데이터 초기화
        List<User> dummyUsers = userRepository.findAll(); // 유저 리스트 가져오기

        // 유저가 발급을 받았을 때
        User user = dummyUsers.get((int) (couponCount % dummyUsers.size())); // 유저를 순차적으로 할당

        // 쿠폰 생성
        Coupon coupon = new Coupon(user, couponName, off, status, expDate, 0L, 1L);
        couponRepository.save(coupon);

        // 발급된 쿠폰 수량을 Redis에 저장하고 증가
        redisTemplate.opsForValue().increment(COUPON_COUNT_KEY, 1);

        return new CouponResponseDto(coupon.getId(), coupon.getCouponName(), coupon.getOff(), coupon.getStatus(), coupon.getExpDate());
    }
}
