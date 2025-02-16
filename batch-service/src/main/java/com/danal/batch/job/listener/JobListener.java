package com.danal.batch.job.listener;

import com.danal.batch.domain.businessData.BusinessDataJpaRepository;
import com.danal.batch.domain.errorLog.ErrorLogJpaRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;

@Slf4j
@Getter
@Setter
@Component
@RequiredArgsConstructor
public class JobListener implements JobExecutionListener {

    private final BusinessDataJpaRepository businessDataJpaRepository;
    private final ErrorLogJpaRepository errorLogJpaRepository;

    private int totalLines;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String fileName = jobExecution.getJobParameters().getString("fileName");
        if (fileName != null) {
            totalLines = calcTotalLines(new ClassPathResource(fileName));
            jobExecution.getExecutionContext().put("fileName", fileName);
            jobExecution.getExecutionContext().putInt("totalLines", totalLines);
        } else throw new RuntimeException("CSV 파일명이 필요합니다.");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        int totalLines = jobExecution.getExecutionContext().getInt("totalLines", 0);
        int processedCount = jobExecution.getExecutionContext().getInt("processedCount", 0);
        int skipCount = jobExecution.getStepExecutions()
            .stream()
            .mapToInt(se -> se.getExecutionContext().getInt("skipCount", 0))
            .sum();

        long businessDataCount = businessDataJpaRepository.count();
        long errorCount = errorLogJpaRepository.count();

        NumberFormat numberFormat = NumberFormat.getInstance();

        log.info("Batch Summary: Total Lines = {}, Processed = {}, Skipped = {}",
            numberFormat.format(totalLines),
            numberFormat.format(processedCount),
            numberFormat.format(skipCount)
        );

        log.info("Database Status: Business Data Count = {}, Error Log Count = {}",
            numberFormat.format(businessDataCount),
            numberFormat.format(errorCount)
        );
    }

    public int calcTotalLines(ClassPathResource resource) {
        log.info("파일 라인 수 계산 중 ...");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()), 8192)) {
            return Math.toIntExact(br.lines().count() - 1);
        } catch (Exception e) {
            log.error("파일 라인 수 계산 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("CSV 파일을 읽을 수 없습니다.");
        }
    }

}