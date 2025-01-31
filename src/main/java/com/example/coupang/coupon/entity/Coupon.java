package com.example.coupang.coupon.entity;

import com.example.coupang.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 255)
    @NotBlank
    private String couponName;

    @Max(100)
    private Long off;

    @NotBlank
    private String status;

    @NotBlank
    private LocalDateTime exrp_date;

    @NotNull
    private Long useCount;

    @NotNull
    private Long maxCount;



    public Coupon(User user, String couponName, Long off, String status, LocalDateTime exrp_date, Long useCount, Long maxCount){
        this.user = user;
        this.couponName = couponName;
        this.off = off;
        this.status = status;
        this.exrp_date = exrp_date;
        this.useCount = useCount;
        this.maxCount = maxCount;
    }
}
