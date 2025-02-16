package com.danal.batch.job.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;

@Slf4j
@StepScope
public class BatchStepListener implements StepExecutionListener {

    private StepExecution stepExecution;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    public synchronized void incrementSkipCount() {
        int currentSkipCount = stepExecution.getExecutionContext().getInt("skipCount", 0);
        stepExecution.getExecutionContext().putInt("skipCount", currentSkipCount + 1);
    }

    public int getProcessedCount() {
        ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
        return executionContext.getInt("processedCount", 0);
    }

    public synchronized void incrementProcessedCount(int addCount) {
        ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
        int processedCount = executionContext.getInt("processedCount", 0);
        executionContext.putInt("processedCount", processedCount + addCount);
    }
}

