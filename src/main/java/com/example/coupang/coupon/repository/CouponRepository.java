package com.example.coupang.coupon.repository;

import com.example.coupang.coupon.entity.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT c FROM Coupon c WHERE c.user.id = :userId AND c.status = :status")
    Coupon findByUserIdAndStatusWithOptimistic(Long userId, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.status = :status")
    List<Coupon> findCouponsByStatusWithPessimisticLock(@Param("status") String status);

    long countCouponsByStatus(String status);

    // 유저 ID를 기준으로 모든 쿠폰 조회
    List<Coupon> findAllByUserId(Long userId);
}