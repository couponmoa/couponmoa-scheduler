package com.couponmoa.backend.couponmoascheduler.domain.usercoupon.repository;

import com.couponmoa.backend.couponmoascheduler.domain.usercoupon.dto.CouponIdRangeDto;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public CouponIdRangeDto getCouponIdRange(LocalDate statDate) {
        String sql = """
                SELECT MIN(coupon_id) AS min_id, MAX(coupon_id) AS max_id
                FROM user_coupons
                WHERE status = 'USED' AND ? <= modified_at AND modified_at < ?
        """;
        LocalDateTime startDate = statDate.atStartOfDay();
        LocalDateTime endDate = statDate.plusDays(1).atStartOfDay();

        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(CouponIdRangeDto.class), startDate, endDate);
    }
}
