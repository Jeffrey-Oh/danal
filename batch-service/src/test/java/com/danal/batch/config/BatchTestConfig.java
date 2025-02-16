package com.danal.batch.config;

import com.danal.batch.domain.businessData.BusinessDataJpaRepository;
import com.danal.batch.domain.errorLog.ErrorLogJpaRepository;
import com.danal.batch.job.listener.BatchSkipListener;
import com.danal.batch.job.listener.BatchStepListener;
import com.danal.batch.job.listener.ChunkSizeTrackingListener;
import com.danal.batch.job.listener.JobListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableBatchProcessing
@Import(value = {TestDataSourceConfig.class, TestJpaConfig.class})
public class BatchTestConfig {

    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils() {
        return new JobLauncherTestUtils();
    }

    @Bean
    public JobListener jobListener(
        BusinessDataJpaRepository businessDataJpaRepository,
        ErrorLogJpaRepository errorLogJpaRepository
    ) {
        return new JobListener(businessDataJpaRepository, errorLogJpaRepository);
    }

    @Bean
    public ChunkSizeTrackingListener<?> chunkSizeTrackingListener(
        BatchStepListener batchStepListener
    ) {
        return new ChunkSizeTrackingListener<>(batchStepListener);
    }

    @Bean
    public BatchStepListener batchStepListener() {
        return new BatchStepListener();
    }

    @Bean
    public BatchSkipListener<?, ?> batchSkipListener(
        ErrorLogJpaRepository errorLogJpaRepository,
        BatchStepListener batchStepListener
    ) {
        return new BatchSkipListener<>(errorLogJpaRepository, batchStepListener);
    }

}
