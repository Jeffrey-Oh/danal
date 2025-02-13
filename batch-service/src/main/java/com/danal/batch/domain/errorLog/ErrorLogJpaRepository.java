package com.danal.batch.domain.errorLog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogJpaRepository extends JpaRepository<ErrorLog, Long> {
}
