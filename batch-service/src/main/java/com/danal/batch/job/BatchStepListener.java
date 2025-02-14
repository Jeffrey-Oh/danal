package com.danal.batch.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchStepListener implements StepExecutionListener {

    private StepExecution stepExecution;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    public synchronized void incrementSkipCount() {
        if (stepExecution != null) {
            int currentSkipCount = stepExecution.getExecutionContext().getInt("skipCount", 0);
            stepExecution.getExecutionContext().putInt("skipCount", currentSkipCount + 1);
        }
    }
}

