package com.couponmoa.backend.couponmoascheduler.domain.coupon.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponQuantityDto {
    private Long id;
    private Long issuedQuantity;
    private Long actualQuantity;
}
