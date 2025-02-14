package com.danal.batch.job;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class CustomPartitioner implements Partitioner {

    private final int totalLines;

    public CustomPartitioner(int totalLines) {
        this.totalLines = totalLines;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitionMap = new HashMap<>();

        int linesPerPartition = (int) Math.ceil((double) totalLines / gridSize);

        for (int i = 0; i < gridSize; i++) {
            int startLine = i * linesPerPartition + 1;  // 첫 번째 줄부터 시작 (1-based index)
            int endLine = Math.min((i + 1) * linesPerPartition, totalLines);

            if (startLine > totalLines) {
                continue;
            }

            ExecutionContext context = new ExecutionContext();
            context.putInt("partitionIndex", i);
            context.putInt("startLine", startLine);
            context.putInt("endLine", endLine);
            partitionMap.put("partition" + i, context);
        }

        return partitionMap;
    }
}
