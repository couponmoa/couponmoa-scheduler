package com.couponmoa.backend.couponmoascheduler.batch;

import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.CouponUsageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CouponUsageStatsJobConfig {

    public static final String COUPON_USAGE_STATS_JOB = "couponUsageStatsJob";
    private static final int CHUNK_SIZE = 1000;

    private final DataSource dataSource;

    @Bean
    public Job couponUsageStatsJob(JobRepository jobRepository, Step couponUsageStatsStep) {
        return new JobBuilder(COUPON_USAGE_STATS_JOB, jobRepository)
                .start(couponUsageStatsStep)
                .build();
    }

    @Bean
    public Step couponUsageStatsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("couponUsageStatsStep", jobRepository)
                .<CouponUsageDto, CouponUsageDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(couponUsageStatsReader(null))
                .writer(couponUsageStatsWriter())
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<CouponUsageDto> couponUsageStatsReader(
            @Value("#{jobParameters['statDate']}") LocalDate statDate
    ) {
        String sql = """
                SELECT coupon_id, COUNT(*) AS usage_count, :statDate AS stat_date
                FROM user_coupons
                WHERE status = 'USED' AND :startOfStatDate <= modified_at AND modified_at < :endOfStatDate
                GROUP BY coupon_id
        """;

        Map<String, Object> params = Map.of(
                "statDate", statDate,
                "startOfStatDate", statDate.atStartOfDay(),
                "endOfStatDate", statDate.plusDays(1).atStartOfDay()
        );

        return new JdbcCursorItemReaderBuilder<CouponUsageDto>()
                .dataSource(dataSource)
                .name("couponUsageStatsReader")
                .sql(NamedParameterUtils.substituteNamedParameters(sql, new MapSqlParameterSource(params)))
                .preparedStatementSetter(new ArgumentPreparedStatementSetter(NamedParameterUtils.buildValueArray(sql, params)))
                .rowMapper(new BeanPropertyRowMapper<>(CouponUsageDto.class))
                .fetchSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<CouponUsageDto> couponUsageStatsWriter() {
        String sql = """
                INSERT INTO coupon_daily_usage_stats (coupon_id, usage_count, stat_date)
                VALUES (:couponId, :usageCount, :statDate)
        """;

        return new JdbcBatchItemWriterBuilder<CouponUsageDto>()
                .dataSource(dataSource)
                .sql(sql)
                .beanMapped()
                .build();
    }
}
