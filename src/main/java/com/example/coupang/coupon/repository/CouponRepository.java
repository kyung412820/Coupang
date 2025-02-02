package com.example.coupang.coupon.repository;

import com.example.coupang.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    // 유저 ID를 기준으로 모든 쿠폰 조회
    List<Coupon> findAllByUserId(Long userId);
}