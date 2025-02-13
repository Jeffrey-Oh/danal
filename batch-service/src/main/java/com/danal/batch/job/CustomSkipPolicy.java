package com.danal.batch.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;

@Slf4j
public class CustomSkipPolicy implements SkipPolicy {
    private final int maxSkips;
    private int skippedCount = 0;

    public CustomSkipPolicy(int maxSkips) {
        this.maxSkips = maxSkips;
    }

    @Override
    public boolean shouldSkip(Throwable throwable, long l) throws SkipLimitExceededException {
        // 특정 예외만 건너뛰기
        if (throwable instanceof FlatFileParseException) {
            if (skippedCount < maxSkips) {
                skippedCount++;
                log.warn("스킵 가능 예외 발생 ({}번째): {}", skippedCount, throwable.getMessage());
                return true;
            } else {
                log.error("스킵 한도를 초과하여 실패", throwable);
                return false;
            }
        }
        return false;
    }
}
