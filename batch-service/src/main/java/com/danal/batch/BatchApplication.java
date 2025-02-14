package com.danal.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
public class BatchApplication {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.danal.batch");

        try (context) {
            JobLauncher jobLauncher = context.getBean(JobLauncher.class);
            Job job = context.getBean(Job.class);

            log.info("배치 작업 시작");
            long start = System.currentTimeMillis();
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())  // 중복 실행 방지용 타임스탬프
                .addString("fileName", "fulldata_07_24_04_P_일반음식점.csv")
                .toJobParameters();
            jobLauncher.run(job, jobParameters);
            log.info("배치 작업 완료");
            long end = System.currentTimeMillis();
            log.info("실행 시간 : {} ms", (end - start));
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("배치 작업이 이미 실행 중입니다.", e);
        } catch (JobRestartException e) {
            log.error("배치 작업을 재시작하는 중 오류가 발생했습니다.", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn("배치 작업이 이미 완료되었습니다.", e);
        } catch (Exception e) {
            log.error("배치 작업 실행 중 예상치 못한 오류 발생", e);
        }
    }
}
