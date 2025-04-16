package com.couponmoa.backend.couponmoascheduler.common.scheduler;

import com.couponmoa.backend.couponmoascheduler.domain.usercoupon.repository.UserCouponJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCouponScheduler {

    private final UserCouponJdbcRepository userCouponJdbcRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void expireUserCoupons() {
        userCouponJdbcRepository.expireUserCoupons();
    }
}
