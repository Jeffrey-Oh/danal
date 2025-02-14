package com.danal.batch.businessData;

import com.danal.batch.config.BatchTestConfig;
import com.danal.batch.job.CustomPartitioner;
import com.danal.batch.job.buisnessData.BusinessDataJobConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BatchTestConfig.class, BusinessDataJobConfig.class})
public class PartitionerTest {

    @Test
    @DisplayName("gridSize 기준으로 범위 나누기")
    void partitioner() {
        // given
        CustomPartitioner partitioner = new CustomPartitioner(100);

        // when
        Map<String, ExecutionContext> executionContextMap = partitioner.partition(5);

        // then
        ExecutionContext partition0 = executionContextMap.get("partition0");
        assertThat(partition0.getInt("startLine")).isEqualTo(1);
        assertThat(partition0.getInt("endLine")).isEqualTo(20);

        ExecutionContext partition4 = executionContextMap.get("partition4");
        assertThat(partition4.getInt("startLine")).isEqualTo(81);
        assertThat(partition4.getInt("endLine")).isEqualTo(100);
    }

}
