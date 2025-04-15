package com.couponmoa.backend.couponmoascheduler.domain.usercoupon.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class UserCouponJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserCouponJdbcRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void expireUserCoupons() {
        String sql = """ 
                UPDATE user_coupons uc
                JOIN coupons c ON uc.coupon_id = c.id
                SET uc.status = 'EXPIRED', uc.modified_at = CURRENT_TIMESTAMP
                WHERE uc.status = 'UNUSED' AND c.expiry_date <= CURRENT_TIMESTAMP
        """;
        jdbcTemplate.update(sql);
    }
}
