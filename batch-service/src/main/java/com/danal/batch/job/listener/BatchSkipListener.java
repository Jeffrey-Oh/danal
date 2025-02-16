package com.danal.batch.job.listener;

import com.danal.batch.domain.errorLog.ErrorLog;
import com.danal.batch.domain.errorLog.ErrorLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchSkipListener<T, S> implements SkipListener<T, S> {

    private final ErrorLogJpaRepository errorLogJpaRepository;
    private final BatchStepListener batchStepListener;

    @Override
    public void onSkipInRead(Throwable t) {
        batchStepListener.incrementSkipCount();
        log.warn("읽기 중 오류 발생 - 오류: {}", t.getMessage());
        errorLogJpaRepository.save(new ErrorLog(t.getMessage()));
    }

    @Override
    public void onSkipInProcess(T item, Throwable t) {
        batchStepListener.incrementSkipCount();
        log.warn("처리 중 오류 발생 - 데이터: {}, 오류: {}", item, t.getMessage());
        errorLogJpaRepository.save(new ErrorLog(t.getMessage()));
    }

    @Override
    public void onSkipInWrite(S item, Throwable t) {
        batchStepListener.incrementSkipCount();
        log.warn("쓰기 중 오류 발생 - 데이터: {}, 오류: {}", item, t.getMessage());
        errorLogJpaRepository.save(new ErrorLog(t.getMessage()));
    }

}