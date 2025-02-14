package com.danal.batch.job;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class ChunkSizeTrackingListener<T> implements ItemWriteListener<T> {

    private final ThreadLocal<AtomicLong> processedCount = ThreadLocal.withInitial(AtomicLong::new);

    @Setter
    private long totalLines;

    @Override
    public void beforeWrite(Chunk<? extends T> items) {
//        log.info("쓰기 시작: {}", items.size());
    }

    @Override
    public void afterWrite(Chunk<? extends T> items) {
//        log.info("쓰기 완료: {}", items.size());
        processedCount.get().addAndGet(items.size());
        printProgress();
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends T> items) {
//        log.error("쓰기 오류 발생: {}", exception.getMessage());
    }

    private void printProgress() {
        if (totalLines > 0) {
            long processedCount = this.processedCount.get().get();
            int progressPercentage = (int) Math.ceil((((double) processedCount / totalLines) * 100));
            if (processedCount >= totalLines) {
                progressPercentage = 100;
            }
            log.info("진행 상황: {}% ({} / {})", progressPercentage, processedCount, totalLines);
        }
    }
}
