package com.example.coupang.coupon.exception;

public class CouponCustomException {

    // 쿠폰 제한 초과 예외
    public static class CouponLimitExceededException extends RuntimeException {
        public CouponLimitExceededException(String message) {
            super(message);
        }
    }

    // 유효하지 않은 유저 ID 예외
    public static class InvalidUserIdException extends RuntimeException {
        public InvalidUserIdException(String message) {
            super(message);
        }
    }
}
