package com.example.coupang.exception;

public class CouponLimitExceededException extends RuntimeException {
    public CouponLimitExceededException(String message) {
        super(message);
    }
}
