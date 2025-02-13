package com.danal.batch.businessData;

import com.danal.batch.config.BatchTestConfig;
import com.danal.batch.job.ReaderJobListener;
import com.danal.batch.job.buisnessData.BusinessDataJobConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBatchTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = {BatchTestConfig.class, BusinessDataJobConfig.class})
public class BusinessDataStepTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ReaderJobListener readerJobListener;

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
        ClassPathResource resource = new ClassPathResource("test1.csv");
        readerJobListener.setResource(resource);
        readerJobListener.setTotalLines(readerJobListener.calcTotalLines(resource));

        JobExecution jobExecution = jobLauncherTestUtils.launchStep("processStep");
        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();

        // Then: Step이 정상 종료되었는지 검증
        assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
        assertEquals(0, stepExecution.getFailureExceptions().size());
    }

    @Test
    @DisplayName("Skip 테스트")
    void givenInvalidData_whenStepExecuted_thenSkipApplied() {
        // Given
        ClassPathResource resource = new ClassPathResource("test2.csv");
        readerJobListener.setResource(resource);
        readerJobListener.setTotalLines(readerJobListener.calcTotalLines(resource));

        // When: Step 실행
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("processStep");
        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();

        // Then: Skip 검증
        assertEquals(stepExecution.getSkipCount(), 1);
        assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
    }

}
