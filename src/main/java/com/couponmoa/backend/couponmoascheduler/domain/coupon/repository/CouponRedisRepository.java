package com.couponmoa.backend.couponmoascheduler.domain.coupon.repository;

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
        Map<String, String> couponMap = coupons.stream()
                .collect(Collectors.toMap(
                        dto -> COUPON_KEY_PREFIX + dto.getId() + STOCK_KEY_SUFFIX,
                        dto -> String.valueOf(dto.getStock())
                ));
        redisTemplate.opsForValue().multiSet(couponMap);
    }
}
