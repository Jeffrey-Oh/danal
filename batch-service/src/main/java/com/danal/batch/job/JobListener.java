package com.danal.batch.job;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
@Getter
@Setter
@Component
public class JobListener implements JobExecutionListener {

    private ClassPathResource resource;
    private int totalLines;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String fileName = jobExecution.getJobParameters().getString("fileName");
        if (fileName != null) {
            resource = new ClassPathResource(fileName);
            totalLines = calcTotalLines(resource);
        } else throw new RuntimeException("CSV 파일명이 필요합니다.");
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