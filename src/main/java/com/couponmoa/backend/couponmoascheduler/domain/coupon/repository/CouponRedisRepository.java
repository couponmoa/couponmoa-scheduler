package com.couponmoa.backend.couponmoascheduler.domain.coupon.repository;

import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.CouponIdDto;
import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.CouponStockDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CouponRedisRepository {

    private static final String COUPON_KEY_PREFIX = "coupon:";
    private static final String STOCK_KEY_SUFFIX = ":stock";
    private final StringRedisTemplate redisTemplate;

    public void saveStock(List<CouponStockDto> coupons) {
        Map<String, String> couponStockMap = coupons.stream()
                .collect(Collectors.toMap(
                        coupon -> COUPON_KEY_PREFIX + coupon.getId() + STOCK_KEY_SUFFIX,
                        coupon -> String.valueOf(coupon.getStock())
                ));
        redisTemplate.opsForValue().multiSet(couponStockMap);
    }

    public void deleteUserSet(List<CouponIdDto> coupons) {
        List<String> keys = coupons.stream()
                .map(coupon -> COUPON_KEY_PREFIX + coupon.getId())
                .toList();
        redisTemplate.delete(keys);
    }
}
