package com.danal.batch.businessData;

import com.danal.batch.config.BatchTestConfig;
import com.danal.batch.job.listener.JobListener;
import com.danal.batch.job.buisnessData.BusinessDataJobConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBatchTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BatchTestConfig.class, BusinessDataJobConfig.class})
public class BusinessDataStepTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JobListener jobListener;

    @AfterEach
    void deleteAll() {
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        jdbcTemplate.update(
            "DELETE FROM business_data WHERE serviceId = :serviceId",
            new MapSqlParameterSource("serviceId", "testId")
        );
    }

    @Test
    @DisplayName("Step 정상 실행")
    void givenValidData_whenStepExecuted_thenSuccess() {
        // Given
        JobInstance jobInstance = new JobInstance(1L, "test1");
        JobExecution jobExecution = new JobExecution(jobInstance, new JobParameters());

        ExecutionContext executionContext = jobExecution.getExecutionContext();
        executionContext.put("fileName", "test1.csv");
        executionContext.put("totalLines", 1);

        JobExecution launchJobExecution = jobLauncherTestUtils.launchStep("partitionedStep", executionContext);
        StepExecution stepExecution = launchJobExecution.getStepExecutions().iterator().next();

        // Then: Step이 정상 종료되었는지 검증
        assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
        assertEquals(0, stepExecution.getFailureExceptions().size());
    }

    @Test
    @DisplayName("Skip 테스트")
    void givenInvalidData_whenStepExecuted_thenSkipApplied() {
        // Given
        JobInstance jobInstance = new JobInstance(2L, "test2");
        JobExecution jobExecution = new JobExecution(jobInstance, new JobParameters());

        ExecutionContext executionContext = jobExecution.getExecutionContext();
        executionContext.put("fileName", "test2.csv");
        executionContext.put("totalLines", 2);

        // When: Step 실행
        JobExecution launchJobExecution = jobLauncherTestUtils.launchStep("partitionedStep", executionContext);

        int skipCount = launchJobExecution.getStepExecutions().stream().mapToInt(stepExecution -> stepExecution.getExecutionContext().getInt("skipCount", 0)).sum();
        boolean allStepsCompleted = launchJobExecution.getStepExecutions().stream().allMatch(step -> step.getStatus().equals(BatchStatus.COMPLETED));

        // Then: 검증
        assertEquals(1, skipCount);
        assertTrue(allStepsCompleted);
    }


}
