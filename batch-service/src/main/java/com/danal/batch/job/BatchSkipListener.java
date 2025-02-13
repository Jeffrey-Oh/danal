package com.danal.batch.job;

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

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("읽기 중 오류 발생 - 오류: {}", t.getMessage());
        ErrorLog errorLog = new ErrorLog(t.getMessage());
        errorLogJpaRepository.save(errorLog);
    }

    @Override
    public void onSkipInProcess(T item, Throwable t) {
        log.warn("처리 중 오류 발생 - 데이터: {}, 오류: {}", item, t.getMessage());
        ErrorLog errorLog = new ErrorLog(t.getMessage());
        errorLogJpaRepository.save(errorLog);
    }

    @Override
    public void onSkipInWrite(S item, Throwable t) {
        log.warn("쓰기 중 오류 발생 - 데이터: {}, 오류: {}", item, t.getMessage());
        ErrorLog errorLog = new ErrorLog(t.getMessage());
        errorLogJpaRepository.save(errorLog);
    }
}