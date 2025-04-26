package com.couponmoa.backend.couponmoascheduler.domain.usercoupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CouponIdRangeDto {
    private Long minId;
    private Long maxId;
}
