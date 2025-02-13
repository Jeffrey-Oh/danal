package com.danal.batch.domain.businessData;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessDataJpaRepository extends JpaRepository<BusinessData, Long> {
    void deleteAllByServiceId(String serviceId);
}
