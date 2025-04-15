package com.couponmoa.backend.couponmoascheduler.domain.coupon.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CouponStockDto implements HasCouponId {
    private Long id;
    private Integer stock;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
