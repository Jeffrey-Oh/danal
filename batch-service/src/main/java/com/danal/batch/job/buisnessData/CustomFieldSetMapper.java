package com.danal.batch.job.buisnessData;

import com.danal.batch.domain.businessData.BusinessData;
import com.danal.batch.exception.FieldSetException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class CustomFieldSetMapper implements FieldSetMapper<BusinessData> {

    @Override
    public BusinessData mapFieldSet(FieldSet fieldSet) {
        BusinessData businessData = new BusinessData();

        // `BusinessData` 클래스의 필드 정보 가져오기
        Field[] fields = BusinessData.class.getDeclaredFields();

        for (Field field : fields) {
            // 필드 이름과 타입
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();

            // 필드를 accessible 하게 설정 (private 필드도 접근 가능)
            field.setAccessible(true);

            try {
                // 해당 필드가 `LocalDate`, `LocalDateTime` 타입인 경우
                if (fieldType.equals(LocalDate.class) || fieldType.equals(LocalDateTime.class)) {
                    String dateStr = fieldSet.readString(fieldName);
                    if (!dateStr.isEmpty()) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        try {
                            LocalDate dateValue = LocalDate.parse(dateStr, formatter);
                            field.set(businessData, dateValue);
                        } catch (DateTimeParseException e) {
                            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
                            try {
                                LocalDateTime dateValue = LocalDateTime.parse(dateStr, formatter);
                                field.set(businessData, dateValue);
                            } catch (DateTimeParseException e1) {
                                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                LocalDateTime dateValue = LocalDateTime.parse(dateStr, formatter);
                                field.set(businessData, dateValue);
                            }
                        }
                    }
                }
                // `Double` 타입인 경우
                else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
                    String valueStr = fieldSet.readString(fieldName);
                    if (!valueStr.isEmpty()) {
                        Double value = Double.parseDouble(valueStr);
                        field.set(businessData, value);
                    }
                }
                // `Integer` 또는 `int` 타입인 경우
                else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                    String valueStr = fieldSet.readString(fieldName);
                    if (!valueStr.isEmpty()) {
                        Integer value = Integer.parseInt(valueStr);
                        field.set(businessData, value);
                    }
                }
                // `String` 타입인 경우
                else if (fieldType.equals(String.class)) {
                    String valueStr = fieldSet.readString(fieldName);
                    field.set(businessData, valueStr);
                }

            } catch (Exception e) {
                // 필드에 대한 변환에 실패할 경우 예외 처리
                log.error("fieldName = {}, fieldType = {}, message = {}", fieldName, fieldType, e.getMessage());
                throw new FieldSetException(e.getMessage());
            }
        }

        return businessData;
    }
}