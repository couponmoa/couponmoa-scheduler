package com.couponmoa.backend.couponmoascheduler.scheduler;

import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.CouponQuantityDto;
import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.CouponIdDto;
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

    @Scheduled(cron = "0 0/30 * * * *")
    public void deactivateAvailableCoupons() {
        List<CouponIdDto> coupons = couponJdbcRepository.findCouponsToDeActivate();
        couponRedisRepository.deleteUserSet(coupons);
        couponJdbcRepository.deactivateCoupons(coupons);
    }

    @Scheduled(fixedDelay = 60000)
    public void updateCouponIssuedQuantity() {
        List<CouponQuantityDto> coupons = couponJdbcRepository.findCouponsToUpdateIssuedQuantity();
        couponJdbcRepository.updateCouponIssuedQuantity(coupons);
    }
}
