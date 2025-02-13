package com.danal.batch.config;

import com.danal.batch.job.ChunkSizeTrackingListener;
import com.danal.batch.job.ReaderJobListener;
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
    public ReaderJobListener readerStepListener() {
        return new ReaderJobListener();
    }

    @Bean
    public ChunkSizeTrackingListener<?> chunkSizeTrackingListener() {
        return new ChunkSizeTrackingListener<>();
    }

}
