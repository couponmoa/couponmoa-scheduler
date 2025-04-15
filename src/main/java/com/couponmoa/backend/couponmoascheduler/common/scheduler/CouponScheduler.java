package com.couponmoa.backend.couponmoascheduler.common.scheduler;

import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.CouponStockDto;
import com.couponmoa.backend.couponmoascheduler.domain.coupon.repository.CouponJdbcRepository;
import com.couponmoa.backend.couponmoascheduler.domain.coupon.repository.CouponRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponScheduler {

    private final CouponJdbcRepository couponJdbcRepository;
    private final CouponRedisRepository couponRedisRepository;

    @Scheduled(cron = "0 0/30 * * * *")
    public void activateIssuableCoupons() {
        List<CouponStockDto> coupons = couponJdbcRepository.findCouponsToActivate();
        couponRedisRepository.saveStock(coupons);
        couponJdbcRepository.activateCoupons(coupons);
    }
}
