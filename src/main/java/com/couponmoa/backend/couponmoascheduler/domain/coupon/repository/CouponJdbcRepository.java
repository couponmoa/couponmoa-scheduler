package com.couponmoa.backend.couponmoascheduler.domain.coupon.repository;

import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.CouponIdDto;
import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.CouponStockDto;
import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.HasCouponId;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class CouponJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public CouponJdbcRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<CouponStockDto> findCouponsToActivate() {
        String sql = """
                SELECT id, total_quantity AS stock, start_date, end_date FROM coupons
                WHERE status = 'UPCOMING' AND start_date <= CURRENT_TIMESTAMP
                AND deleted_at IS NULL
        """;
        return jdbcTemplate.query(sql, couponStockDtoRowMapper());
    }

    public List<CouponIdDto> findCouponsToDeActivate() {
        String sql = """
                SELECT id FROM coupons
                WHERE status = 'IN_PROGRESS' AND end_date <= CURRENT_TIMESTAMP
                AND deleted_at IS NULL
        """;
        return jdbcTemplate.query(sql, couponIdDtoRowMapper());
    }

    public void activateCoupons(List<CouponStockDto> coupons) {
        String sql = "UPDATE coupons SET status = 'IN_PROGRESS', modified_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.batchUpdate(sql, coupons, 100, couponIdSetter());
    }

    public void deactivateCoupons(List<CouponIdDto> coupons) {
        String sql = "UPDATE coupons SET status = 'ENDED', modified_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.batchUpdate(sql, coupons, 100, couponIdSetter());
    }

    private RowMapper<CouponStockDto> couponStockDtoRowMapper() {
        return BeanPropertyRowMapper.newInstance(CouponStockDto.class);
    }

    private RowMapper<CouponIdDto> couponIdDtoRowMapper() {
        return BeanPropertyRowMapper.newInstance(CouponIdDto.class);
    }

    private <T extends HasCouponId> ParameterizedPreparedStatementSetter<T> couponIdSetter() {
        return (ps, coupon) -> ps.setLong(1, coupon.getId());
    }
}
