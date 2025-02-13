package com.danal.batch.businessData;

import com.danal.batch.domain.businessData.BusinessData;
import com.danal.batch.job.buisnessData.CustomFieldSetMapper;
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
    @DisplayName("CSV 한 줄 읽기")
    void reader() throws Exception {
        // Given: Reader 설정
        FlatFileItemReader<BusinessData> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("test1.csv"));
        reader.setLinesToSkip(1); // 헤더 제외
        reader.setEncoding("EUC-KR");  // 인코딩 설정

        // Reader 초기화
        reader.setLineMapper(new DefaultLineMapper<>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(BusinessData.getFieldNames().toArray(String[]::new));
                setDelimiter(",");
                setQuoteCharacter('\"');  // CSV 파일 내 따옴표 처리
                setStrict(false);
            }});
            setFieldSetMapper(new CustomFieldSetMapper());
        }});
        reader.afterPropertiesSet();
        reader.open(MetaDataInstanceFactory.createStepExecution().getExecutionContext());

        // When: 데이터 한 줄 읽기
        BusinessData item = reader.read();

        // Then: 데이터 검증
        assertThat(item).isNotNull();
        assertThat(item.getServiceName()).isEqualTo("일반음식점");
        assertThat(item.getServiceId()).isEqualTo("07_24_04_P");
    }

}
