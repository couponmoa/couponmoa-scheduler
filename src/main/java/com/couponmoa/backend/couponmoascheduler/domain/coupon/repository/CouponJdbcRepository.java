package com.couponmoa.backend.couponmoascheduler.domain.coupon.repository;

import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.CouponQuantityDto;
import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.CouponIdDto;
import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.CouponStockDto;
import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.HasCouponId;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
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
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CouponStockDto.class));
    }

    public List<CouponIdDto> findCouponsToDeActivate() {
        String sql = """
                SELECT id FROM coupons
                WHERE status = 'IN_PROGRESS' AND end_date <= CURRENT_TIMESTAMP
                AND deleted_at IS NULL
        """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CouponIdDto.class));
    }

    public List<CouponQuantityDto> findCouponsToUpdateIssuedQuantity() {
        String sql = """
                SELECT c.id, c.issued_quantity, COUNT(uc.coupon_id) AS actual_quantity
                FROM coupons c
                LEFT JOIN user_coupons uc ON c.id = uc.coupon_id
                WHERE c.status = 'IN_PROGRESS' AND c.deleted_at IS NULL
                GROUP BY c.id
                HAVING c.issued_quantity != COUNT(uc.coupon_id)
        """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CouponQuantityDto.class));
    }

    public void activateCoupons(List<CouponStockDto> coupons) {
        String sql = "UPDATE coupons SET status = 'IN_PROGRESS', modified_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.batchUpdate(sql, coupons, 100, couponIdSetter());
    }

    public void deactivateCoupons(List<CouponIdDto> coupons) {
        String sql = "UPDATE coupons SET status = 'ENDED', modified_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.batchUpdate(sql, coupons, 100, couponIdSetter());
    }

    public void updateCouponIssuedQuantity(List<CouponQuantityDto> coupons) {
        String sql = "UPDATE coupons SET issued_quantity = ?, modified_at = CURRENT_TIMESTAMP WHERE id = ?";
        jdbcTemplate.batchUpdate(sql, coupons, 100, (ps, coupon) -> {
            ps.setLong(1, coupon.getActualQuantity());
            ps.setLong(2, coupon.getId());
        });
    }

    private <T extends HasCouponId> ParameterizedPreparedStatementSetter<T> couponIdSetter() {
        return (ps, coupon) -> ps.setLong(1, coupon.getId());
    }
}
