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
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

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
    public Partitioner partitioner(ReaderJobListener readerJobListener) {
        return gridSize -> {
            Map<String, ExecutionContext> partitionMap = new HashMap<>();

            int totalLines = readerJobListener.getTotalLines();
            int linesPerPartition = (int) Math.ceil((double) totalLines / gridSize);

            for (int i = 0; i < gridSize; i++) {
                ExecutionContext context = new ExecutionContext();
                int startLine = i * linesPerPartition + 1;  // 첫 번째 줄부터 시작 (1-based index)
                int endLine = Math.min((i + 1) * linesPerPartition, totalLines);

                context.putInt("partitionIndex", i);
                context.putInt("startLine", startLine);
                context.putInt("endLine", endLine);
                partitionMap.put("partition" + i, context);
            }

            return partitionMap;
        };
    }

    @Bean
    @StepScope
    public FlatFileItemReader<BusinessData> reader(
        ChunkSizeTrackingListener<BusinessData> chunkSizeTrackingListener,
        ReaderJobListener readerJobListener,
        @Value("#{stepExecutionContext['startLine']}") int startLine,
        @Value("#{stepExecutionContext['endLine']}") int endLine
    ) {
        chunkSizeTrackingListener.setTotalLines(readerJobListener.getTotalLines());

        FlatFileItemReader<BusinessData> reader = new FlatFileItemReader<>() {
            private int currentLine = startLine - 1; // 현재 라인 추적용 변수

            @Override
            public BusinessData read() throws Exception {
                BusinessData item = super.read();
                currentLine++;

                // endLine을 넘으면 읽기 중단
                if (currentLine > endLine) {
                    return null; // Spring Batch에서 null을 만나면 읽기 종료
                }
                return item;
            }
        };
        reader.setResource(readerJobListener.getResource());

        int linesToSkip = (startLine <= 1) ? 1 : (startLine - 1); // 헤더 스킵
        reader.setLinesToSkip(linesToSkip);
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
    public Job importBusinessDataJob(
        @Qualifier("partitionedStep") Step partitionedStep,
        ReaderJobListener readerJobListener
    ) {
        return new JobBuilder("importBusinessDataJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(partitionedStep)
            .listener(readerJobListener)
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
        FlatFileItemReader<BusinessData> reader,
        JdbcBatchItemWriter<BusinessData> writer
    ) {
        return new StepBuilder("chunkStep", jobRepository)
            .<BusinessData, BusinessData>chunk(10000, transactionManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new CustomSkipPolicy(10))
            .listener(new BatchSkipListener<>(errorLogJpaRepository))
            .listener(chunkSizeTrackingListener)
            .transactionManager(transactionManager)
            .build();
    }

}