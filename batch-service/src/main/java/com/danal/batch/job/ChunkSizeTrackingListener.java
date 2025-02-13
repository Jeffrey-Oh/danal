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

    private final AtomicLong processedCount = new AtomicLong();

    @Setter
    private long totalLines;

    @Override
    public void beforeWrite(Chunk<? extends T> items) {
        // 쓰기 전에 호출됨 (필요시 로그 출력)
    }

    @Override
    public void afterWrite(Chunk<? extends T> items) {
        processedCount.addAndGet(items.size());
        printProgress();
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends T> items) {
        log.error("쓰기 오류 발생: {}", exception.getMessage());
    }

    private void printProgress() {
        if (totalLines > 0) {
            int progressPercentage = (int) (((double) processedCount.get() / totalLines) * 100);
            log.info("진행 상황: {}% ({} / {})", progressPercentage, processedCount.get(), totalLines);
        }
    }
}
