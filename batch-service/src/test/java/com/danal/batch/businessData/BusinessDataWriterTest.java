package com.danal.batch.businessData;

import com.danal.batch.config.TestDataSourceConfig;
import com.danal.batch.config.TestJpaConfig;
import com.danal.batch.domain.businessData.BusinessData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestDataSourceConfig.class, TestJpaConfig.class})
public class BusinessDataWriterTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("MySQL 연결")
    void testDatabaseConnection() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
            System.out.println("MySQL 연결 성공!");
        }
    }

    @Test
    @Transactional
    @DisplayName("데이터 저장")
    void writer() throws Exception {
        // Given: JdbcBatchItemWriter 설정
        JdbcBatchItemWriter<BusinessData> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(item -> {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("serviceName", item.getServiceName());
            params.addValue("serviceId", item.getServiceId());
            return params;
        });

        writer.setSql("INSERT INTO business_data (serviceName, serviceId) VALUES (:serviceName, :serviceId)");
        writer.setDataSource(dataSource);
        writer.afterPropertiesSet();

        // When: 데이터 저장
        BusinessData data = new BusinessData();
        data.setServiceName("테스트 일반음식점");
        data.setServiceId("테스트 07_24_04_P");

        writer.write(new Chunk<>(Collections.singletonList(data)));

        // Then: 데이터 검증
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM business_data WHERE serviceId = :serviceId",
            new MapSqlParameterSource("serviceId", "테스트 07_24_04_P"),
            Integer.class
        );

        assertThat(count).isEqualTo(1);
    }

}
