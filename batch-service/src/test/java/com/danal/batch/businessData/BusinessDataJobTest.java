package com.danal.batch.businessData;

import com.danal.batch.config.BatchTestConfig;
import com.danal.batch.job.buisnessData.BusinessDataJobConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BatchTestConfig.class, BusinessDataJobConfig.class})
class BusinessDataJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Job importBusinessDataJob;

    @AfterEach
    void deleteAll() {
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        jdbcTemplate.update(
            "DELETE FROM business_data WHERE serviceId = :serviceId",
            new MapSqlParameterSource("serviceId", "testId")
        );
    }

    @Test
    @DisplayName("Job 실행하기")
    void job() throws Exception {
        // Given: Job 파라미터 설정
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())  // 중복 실행 방지용 타임스탬프
            .addString("fileName", "test1.csv")
            .toJobParameters();

        // When: Job 실행
        jobLauncherTestUtils.setJob(importBusinessDataJob);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then: Job이 정상 종료되었는지 검증
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }
}
