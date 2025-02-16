package com.danal.batch.businessData;

import com.danal.batch.domain.businessData.BusinessData;
import com.danal.batch.job.buisnessData.BusinessDataFieldSetMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

public class BusinessDataReaderTest {

    @Test
    @DisplayName("CSV 읽기")
    void reader() throws Exception {
        // Given: Reader 설정
        FlatFileItemReader<BusinessData> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("test1.csv"));

        int startLine = 1;
        int endLine = 1;

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
                    setFieldSetMapper(new BusinessDataFieldSetMapper());
                }}.mapLine(line, lineNumber);
            }
        });

        reader.afterPropertiesSet();

        // When: 데이터 한 줄 읽기
        reader.open(MetaDataInstanceFactory.createStepExecution().getExecutionContext());
        BusinessData item = reader.read();

        // Then: 데이터 검증
        assertThat(item).isNotNull();
        assertThat(item.getServiceName()).isEqualTo("일반음식점");
        assertThat(item.getServiceId()).isEqualTo("testId");
    }

}
