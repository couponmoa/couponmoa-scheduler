package com.couponmoa.backend.couponmoascheduler.domain.coupon.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CouponUsageDto {
    private Long couponId;
    private Long usageCount;
    private LocalDate statDate;
}
