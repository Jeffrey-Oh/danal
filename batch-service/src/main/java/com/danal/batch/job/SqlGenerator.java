package com.danal.batch.job;

import java.lang.reflect.Field;
import java.util.StringJoiner;

public class SqlGenerator {

    public static String generateInsertSql(Class<?> clazz, String tableName, String key) {
        Field[] fields = clazz.getDeclaredFields();

        StringJoiner columns = new StringJoiner(", ");
        StringJoiner values = new StringJoiner(", ");

        for (Field field : fields) {
            if (!field.getName().equals(key)) {  // 기본 키 제외
                columns.add(field.getName());
                values.add(":" + field.getName());
            }
        }

        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
    }

}
