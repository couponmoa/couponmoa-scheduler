package com.couponmoa.backend.couponmoascheduler.batch;

import com.couponmoa.backend.couponmoascheduler.domain.usercoupon.dto.CouponIdRangeDto;
import com.couponmoa.backend.couponmoascheduler.domain.usercoupon.repository.UserCouponJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest
@Sql(scripts = "/sql/coupon_usage_stats_job_setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CouponUsageStatsJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private Job couponUsageStatsJob;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @MockitoBean
    private UserCouponJdbcRepository userCouponJdbcRepository;

    private static final int userCouponCount = 1000;
    private static final int couponCount = 50;

    @BeforeEach
    void setUp() {
        String sql = "INSERT INTO user_coupons (coupon_id, status, modified_at) VALUES (?, ?, ?)";
        List<String> statuses = List.of("USED", "UNUSED", "EXPIRED");
        Random random = new Random();

        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = 0; i < userCouponCount; i++) {
            int couponId = i % couponCount + 1;
            String status = statuses.get(random.nextInt(3));
            LocalDateTime modifiedAt = LocalDateTime.now().minusDays(random.nextInt(3));
            batchArgs.add(new Object[]{couponId, status, modifiedAt});
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Test
    void 쿠폰_사용량_집계_성공() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        LocalDate statDate = LocalDate.now().minusDays(1);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("statDate", statDate)
                .toJobParameters();

        when(userCouponJdbcRepository.getCouponIdRange(statDate))
                .thenReturn(new CouponIdRangeDto(1L, (long) userCouponCount));

        JobExecution jobExecution = jobLauncherTestUtils.getJobLauncher()
                .run(couponUsageStatsJob, jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Integer expectedCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(DISTINCT coupon_id)
            FROM user_coupons
            WHERE status = 'USED'
            AND modified_at >= ? AND modified_at < ?
        """, Integer.class, statDate.atStartOfDay(), statDate.plusDays(1).atStartOfDay());

        Integer actualCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM coupon_daily_usage_stats
            WHERE stat_date = ?
        """, Integer.class, statDate);

        assertThat(actualCount).isEqualTo(expectedCount);
    }
}