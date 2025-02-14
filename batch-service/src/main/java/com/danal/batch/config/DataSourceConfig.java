package com.danal.batch.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:application-${SPRING_PROFILES_ACTIVE:local}.properties")
public class DataSourceConfig {

    @Value("${datasource.mysql.jdbc-url}")
    private String jdbcUrl;

    @Value("${datasource.mysql.username}")
    private String username;

    @Value("${datasource.mysql.password}")
    private String password;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver"); // MySQL 드라이버
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        // HikariCP 성능 관련 옵션 설정 (필요에 따라 조절 가능)
        config.setMaximumPoolSize(10); // 최대 커넥션 개수
        config.setMinimumIdle(2); // 최소 유휴 커넥션 개수
        config.setIdleTimeout(30000); // 유휴 커넥션 최대 유지 시간 (ms)
        config.setMaxLifetime(1800000); // 커넥션 최대 생명주기 (ms)
        config.setConnectionTimeout(3000); // 커넥션 최대 대기 시간 (ms)

        return new HikariDataSource(config);
    }

}
