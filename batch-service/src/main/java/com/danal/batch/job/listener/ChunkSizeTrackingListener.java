package com.danal.batch.job.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@StepScope
@RequiredArgsConstructor
public class ChunkSizeTrackingListener<T> implements ItemWriteListener<T> {

    private final BatchStepListener batchStepListener;

    @Value("#{jobExecutionContext['totalLines']}")
    private int totalLines;

    @Override
    public void beforeWrite(Chunk<? extends T> items) {
//        log.info("쓰기 시작: {}", items.size());
    }

    @Override
    public void afterWrite(Chunk<? extends T> items) {
//        log.info("쓰기 완료: {}", items.size());
        batchStepListener.incrementProcessedCount(items.size());
        int processedCount = batchStepListener.getProcessedCount();
        printProgress(processedCount);
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends T> items) {
        log.error("쓰기 오류 발생: {}", exception.getMessage());
    }

    private void printProgress(int processedCount) {
        if (totalLines > 0) {
            int progressPercentage = (int) Math.ceil((((double) processedCount / totalLines) * 100));
            if (processedCount >= totalLines) {
                progressPercentage = 100;
            }
            log.info("진행 상황: {}% ({} / {})", progressPercentage, processedCount, totalLines);
        }
    }
}
