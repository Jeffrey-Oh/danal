package com.danal.batch.job.buisnessData;

import com.danal.batch.domain.businessData.BusinessData;
import com.danal.batch.domain.errorLog.ErrorLogJpaRepository;
import com.danal.batch.job.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BusinessDataJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ErrorLogJpaRepository errorLogJpaRepository;

    @Bean
    @StepScope
    public FlatFileItemReader<BusinessData> reader(
        ChunkSizeTrackingListener<BusinessData> chunkSizeTrackingListener,
        ReaderJobListener readerJobListener
    ) {
        chunkSizeTrackingListener.setTotalLines(readerJobListener.getTotalLines());

        FlatFileItemReader<BusinessData> reader = new FlatFileItemReader<>();
        reader.setResource(readerJobListener.getResource());
        reader.setLinesToSkip(1);  // 헤더 스킵
        reader.setEncoding("EUC-KR");

        reader.setLineMapper(new DefaultLineMapper<>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(BusinessData.getFieldNames().toArray(String[]::new));
                setDelimiter(",");
                setQuoteCharacter('\"');  // CSV 파일 내 따옴표 처리
                setStrict(false);
            }});
            setFieldSetMapper(new CustomFieldSetMapper());
        }});

        return reader;
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<BusinessData> writer(DataSource dataSource) {
        JdbcBatchItemWriter<BusinessData> writer = new JdbcBatchItemWriter<>();

        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());

        String insertSql = SqlGenerator.generateInsertSql(BusinessData.class, "business_data", "businessDataId");
        writer.setSql(insertSql);

        writer.setDataSource(dataSource);
        writer.afterPropertiesSet();
        writer.setAssertUpdates(false);

        return writer;
    }

    @Bean
    public Job importBusinessDataJob(
        DataSource dataSource,
        ChunkSizeTrackingListener<BusinessData> chunkSizeTrackingListener,
        ReaderJobListener readerJobListener
    ) {
        return new JobBuilder("importBusinessDataJob", jobRepository)
            .start(processStep(dataSource, chunkSizeTrackingListener, readerJobListener))
            .incrementer(new RunIdIncrementer())
            .listener(readerJobListener)
            .build();
    }

    @Bean
    public Step processStep(
        DataSource dataSource,
        ChunkSizeTrackingListener<BusinessData> chunkSizeTrackingListener,
        ReaderJobListener readerJobListener
    ) {
        return new StepBuilder("processStep", jobRepository)
            .<BusinessData, BusinessData>chunk(10000, transactionManager)
            .reader(reader(chunkSizeTrackingListener, readerJobListener))
            .writer(writer(dataSource))
            .faultTolerant()
            .skipPolicy(new CustomSkipPolicy(10))
            .listener(new BatchSkipListener<>(errorLogJpaRepository))  // 오류 데이터 로깅 및 저장
            .listener(chunkSizeTrackingListener)  // 진행 상황 출력 리스너
            .transactionManager(transactionManager)
            .build();
    }

}