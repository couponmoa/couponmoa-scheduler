package com.couponmoa.backend.couponmoascheduler.batch;

import com.couponmoa.backend.couponmoascheduler.domain.coupon.dto.CouponUsageDto;
import com.couponmoa.backend.couponmoascheduler.domain.usercoupon.dto.CouponIdRangeDto;
import com.couponmoa.backend.couponmoascheduler.domain.usercoupon.repository.UserCouponJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CouponUsageStatsJobConfig {

    public static final String COUPON_USAGE_STATS_JOB = "couponUsageStatsJob";
    private static final int CHUNK_SIZE = 1000;
    private static final int GRID_SIZE = 10;
    private static final String PARAM_STAT_DATE = "statDate";
    private static final String PARAM_START_ID = "startId";
    private static final String PARAM_END_ID = "endId";

    private final DataSource dataSource;
    private final UserCouponJdbcRepository userCouponJdbcRepository;

    @Bean
    public Job couponUsageStatsJob(JobRepository jobRepository, Step couponUsageStatsPartitionStep) {
        return new JobBuilder(COUPON_USAGE_STATS_JOB, jobRepository)
                .start(couponUsageStatsPartitionStep)
                .build();
    }

    @Bean
    public Step couponUsageStatsPartitionStep(
            JobRepository jobRepository,
            Partitioner couponIdRangePartitioner,
            Step couponUsageStatsStep
    ) {
        return new StepBuilder("couponUsageStatsPartitionStep", jobRepository)
                .partitioner("CouponIdRangePartitioner", couponIdRangePartitioner)
                .step(couponUsageStatsStep)
                .gridSize(GRID_SIZE)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    @StepScope
    public Partitioner couponIdRangePartitioner(
            @Value("#{jobParameters['" + PARAM_STAT_DATE + "']}") LocalDate statDate
    ) {
        return gridSize -> {
            CouponIdRangeDto couponIdRange = userCouponJdbcRepository.getCouponIdRange(statDate);
            long minId = couponIdRange.getMinId();
            long maxId = couponIdRange.getMaxId();
            long rangeSize = (maxId - minId + 1) / gridSize;

            Map<String, ExecutionContext> partitions = new HashMap<>(gridSize);
            for (int i = 0; i < gridSize; i++) {
                long startId = minId + i * rangeSize;
                long endId = (i == gridSize - 1) ? maxId : minId + ((i + 1) * rangeSize) - 1;

                ExecutionContext context = new ExecutionContext();
                context.putLong(PARAM_START_ID, startId);
                context.putLong(PARAM_END_ID, endId);
                partitions.put("partition" + i, context);
            }
            return partitions;
        };
    }

    @Bean
    public Step couponUsageStatsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("couponUsageStatsStep", jobRepository)
                .<CouponUsageDto, CouponUsageDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(couponUsageStatsPagingItemReader(null, null, null))
                .writer(couponUsageStatsWriter())
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<CouponUsageDto> couponUsageStatsPagingItemReader(
            @Value("#{jobParameters['" + PARAM_STAT_DATE + "']}") LocalDate statDate,
            @Value("#{stepExecutionContext['" + PARAM_START_ID + "']}") Long startId,
            @Value("#{stepExecutionContext['" + PARAM_END_ID + "']}") Long endId
    ) throws Exception {
        Map<String, Object> params = Map.of(
                PARAM_STAT_DATE, statDate,
                "startDate", statDate.atStartOfDay(),
                "endDate", statDate.plusDays(1).atStartOfDay(),
                PARAM_START_ID, startId,
                PARAM_END_ID, endId
        );

        return new JdbcPagingItemReaderBuilder<CouponUsageDto>()
                .name("couponUsageStatsPagingItemReader")
                .dataSource(dataSource)
                .queryProvider(couponUsageStatsPagingQueryProvider())
                .parameterValues(params)
                .rowMapper(new BeanPropertyRowMapper<>(CouponUsageDto.class))
                .pageSize(CHUNK_SIZE)
                .build();
    }

    private PagingQueryProvider couponUsageStatsPagingQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT coupon_id, COUNT(*) AS usage_count, :statDate AS stat_date");
        queryProvider.setFromClause("FROM user_coupons");
        queryProvider.setWhereClause("WHERE status = 'USED' AND :startDate <= modified_at AND modified_at < :endDate AND coupon_id BETWEEN :startId AND :endId");
        queryProvider.setGroupClause("GROUP BY coupon_id");
        queryProvider.setSortKey("coupon_id");
        return queryProvider.getObject();
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
                .assertUpdates(true)
                .build();
    }
}
