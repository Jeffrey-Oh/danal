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
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BusinessDataJobConfig {

    private final int POOL_SIZE = 5;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ErrorLogJpaRepository errorLogJpaRepository;
    private final ChunkSizeTrackingListener<BusinessData> chunkSizeTrackingListener;

    @Bean
    @StepScope
    public FlatFileItemReader<BusinessData> reader(
        ChunkSizeTrackingListener<BusinessData> chunkSizeTrackingListener,
        JobListener jobListener,
        @Value("#{stepExecutionContext['startLine']}") int startLine,
        @Value("#{stepExecutionContext['endLine']}") int endLine
    ) throws Exception {
        chunkSizeTrackingListener.setTotalLines(jobListener.getTotalLines());

        FlatFileItemReader<BusinessData> reader = new FlatFileItemReader<>();
        reader.setResource(jobListener.getResource());

        int linesToSkip = (startLine <= 1) ? 1 : (startLine - 1); // 헤더 스킵
        reader.setLinesToSkip(linesToSkip);
        reader.setEncoding("EUC-KR");

        reader.setLineMapper(new DefaultLineMapper<>() {
            @Override
            public BusinessData mapLine(String line, int lineNumber) throws Exception {
                // endLine 초과하면 더 이상 읽지 않음
                if (lineNumber - 1 > endLine) {
                    return null;
                }

                return new DefaultLineMapper<BusinessData>() {{
                    setLineTokenizer(new DelimitedLineTokenizer() {{
                        setNames(BusinessData.getFieldNames().toArray(String[]::new));
                        setDelimiter(",");
                        setQuoteCharacter('\"');
                        setStrict(false);
                    }});
                    setFieldSetMapper(new CustomFieldSetMapper());
                }}.mapLine(line, lineNumber);
            }
        });

        reader.afterPropertiesSet();

        return reader;
    }

    @Bean
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
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(POOL_SIZE);
        executor.setMaxPoolSize(POOL_SIZE);
        executor.setThreadNamePrefix("partition-thread");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskExecutorPartitionHandler partitionHandler(
        @Qualifier("chunkStep") Step chunkStep
    ) {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(chunkStep);
        partitionHandler.setTaskExecutor(executor());
        partitionHandler.setGridSize(POOL_SIZE);
        return partitionHandler;
    }

    @Bean
    @StepScope
    public Partitioner partitioner(JobListener jobListener) {
        return new CustomPartitioner(jobListener.getTotalLines());
    }

    @Bean
    public Job importBusinessDataJob(
        @Qualifier("partitionedStep") Step partitionedStep,
        JobListener jobListener
    ) {
        return new JobBuilder("importBusinessDataJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(partitionedStep)
            .listener(jobListener)
            .build();
    }

    @Bean
    public Step partitionedStep(
        @Qualifier("chunkStep") Step chunkStep,
        Partitioner partitioner
    ) {
        return new StepBuilder("partitionedStep", jobRepository)
            .partitioner("chunkStep", partitioner)
            .step(chunkStep)
            .partitionHandler(partitionHandler(chunkStep))
            .build();
    }


    @Bean
    public Step chunkStep(
        ItemReader<BusinessData> reader,
        JdbcBatchItemWriter<BusinessData> writer,
        BatchStepListener batchStepListener
    ) {
        return new StepBuilder("chunkStep", jobRepository)
            .<BusinessData, BusinessData>chunk(10000, transactionManager)
            .reader(reader)
            .writer(writer)
            .listener(batchStepListener)
            .faultTolerant()
            .skipLimit(10)
            .skip(FlatFileParseException.class)
            .listener(new BatchSkipListener<>(errorLogJpaRepository, batchStepListener))
            .listener(chunkSizeTrackingListener)
            .transactionManager(transactionManager)
            .build();
    }

}