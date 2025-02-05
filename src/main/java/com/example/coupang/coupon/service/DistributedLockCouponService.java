package com.example.coupang.coupon.service;

import com.example.coupang.coupon.entity.Coupon;
import com.example.coupang.coupon.repository.CouponRepository;
import com.example.coupang.coupon.dto.CouponResponseDto;
import com.example.coupang.coupon.exception.CouponCustomException;
import com.example.coupang.user.entity.User;
import com.example.coupang.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockCouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate; // ValueOperations 타입을 String으로 변경

    // Redis 분산 락을 위한 키
    private static final String COUPON_LOCK_KEY = "coupon_lock";
    private static final String COUPON_COUNT_KEY = "coupon_count"; // 쿠폰 수량을 저장할 Redis 키
    private static final long MAX_COUPON_COUNT = 100L; // 최대 쿠폰 수

    /**
     * 분산 락을 이용한 쿠폰 발급 메서드
     *
     * 이 메서드는 쿠폰을 발급하기 위해 Redis를 사용하여 동시성 문제를 해결합니다.
     * 먼저, 쿠폰 발급 수량을 확인하고, 쿠폰이 모두 소진된 경우 예외를 발생시킵니다.
     * 발급된 쿠폰은 데이터베이스에 저장되며, 발급 수량은 Redis에 저장된 값이 증가합니다.
     *
     * @param couponName 발급할 쿠폰의 이름
     * @param off 쿠폰의 할인액
     * @param status 쿠폰의 상태
     * @param expDate 쿠폰의 만료 날짜
     * @return 발급된 쿠폰의 정보가 담긴 DTO
     * @throws RuntimeException 쿠폰이 모두 소진된 경우 또는 다른 프로세스에서 쿠폰 발급을 처리 중일 때
     */
    @Transactional
    public CouponResponseDto issueCoupon(String couponName, Long off, String status, LocalDateTime expDate) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();

        // 재시도 설정
        final int maxRetries = 10;           // 최대 재시도 횟수
        final long retryDelayMillis = 100L;    // 재시도 간격 (밀리초)
        int retryCount = 0;
        Boolean acquired = false;

        // 재시도 루프: 최대 maxRetries번 시도하면서 락을 획득
        while (retryCount < maxRetries) {
            acquired = operations.setIfAbsent(COUPON_LOCK_KEY, "locked", 1, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(acquired)) {
                break; // 락 획득 성공하면 루프 종료
            }
            retryCount++;
            try {
                Thread.sleep(retryDelayMillis); // 재시도 전 대기
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("락 획득 도중 인터럽트 발생", ex);
            }
        }

        // 락을 획득하지 못한 경우 예외 발생
        if (!Boolean.TRUE.equals(acquired)) {
            throw new RuntimeException("다른 프로세스에서 이미 쿠폰 발급을 처리 중입니다. 재시도 실패");
        }

        try {
            // 쿠폰 발급 수량 확인
            Long couponCount = redisTemplate.opsForValue().get(COUPON_COUNT_KEY) != null
                    ? Long.valueOf(operations.get(COUPON_COUNT_KEY))
                    : 0L;

            if (couponCount >= MAX_COUPON_COUNT) {
                // 쿠폰 수량이 100개 이상이면 더 이상 발급 불가
                throw new CouponCustomException.CouponLimitExceededException("쿠폰이 모두 소진되었습니다.");
            }

            // 유저 데이터 초기화: 데이터베이스에서 모든 유저 가져오기
            List<User> dummyUsers = userRepository.findAll();

            // 발급받을 유저 선택: 쿠폰 수를 유저 수로 나눈 나머지를 인덱스로 사용하여 순환적으로 할당
            User user = dummyUsers.get((int) (couponCount % dummyUsers.size()));

            // 쿠폰 생성 (useCount는 0, maxCount는 1로 설정)
            Coupon coupon = new Coupon(user, couponName, off, status, expDate, 0L, 1L);
            couponRepository.save(coupon);

            // 발급된 쿠폰 수량을 Redis에서 증가시키기 (원자적 증가)
            operations.increment(COUPON_COUNT_KEY, 1);

            // 생성된 쿠폰 정보를 DTO로 반환
            return new CouponResponseDto(coupon.getId(), coupon.getCouponName(), coupon.getOff(), coupon.getStatus(), coupon.getExpDate());
        } finally {
            // 락 해제: Redis에서 락 키 삭제
            redisTemplate.delete(COUPON_LOCK_KEY);
        }
    }

    /**
     * 특정 유저의 모든 쿠폰을 조회하는 메서드
     *
     * @param userId 쿠폰을 조회할 유저의 ID
     * @return 유저가 보유한 쿠폰 목록
     */
    public List<CouponResponseDto> getCouponsByUserId(String userId) {
        if (userId == null || !userRepository.existsById(userId)) {
            throw new CouponCustomException.InvalidUserIdException("유효하지 않은 유저 ID입니다.");
        }
        List<Coupon> coupons = couponRepository.findAllByUserId(userId);
        return coupons.stream()
                .map(coupon -> new CouponResponseDto(coupon.getId(), coupon.getCouponName(), coupon.getOff(), coupon.getStatus(), coupon.getExpDate()))
                .collect(Collectors.toList());
    }
}
