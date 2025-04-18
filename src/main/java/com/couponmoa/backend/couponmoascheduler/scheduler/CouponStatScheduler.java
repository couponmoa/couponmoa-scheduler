package com.couponmoa.backend.couponmoascheduler.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.couponmoa.backend.couponmoascheduler.batch.CouponUsageStatsJobConfig.COUPON_USAGE_STATS_JOB;

@Component
@RequiredArgsConstructor
public class CouponStatScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @Scheduled(cron = "0 0 6 * * *")
    public void runCouponUsageStatsJob() throws NoSuchJobException, JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        Job job = jobRegistry.getJob(COUPON_USAGE_STATS_JOB);
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("statDate", LocalDate.now().minusDays(1))
                .toJobParameters();

        jobLauncher.run(job, jobParameters);
    }
}
