package com.danal.batch.domain.businessData;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "business_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long businessDataId;

    private String serviceName;  // 개방서비스명
    private String serviceId;  // 개방서비스아이디
    private String municipalityCode;  // 개방자치단체코드
    private String managementNumber;  // 관리번호

    @Column(columnDefinition = "DATE")
    private LocalDate approvalDate;  // 인허가일자

    @Column(columnDefinition = "DATE")
    private LocalDate approvalCancelDate;  // 인허가취소일자

    private Integer businessStatusCode;  // 영업상태구분코드
    private String businessStatusName;  // 영업상태명

    private Integer detailedBusinessStatusCode;  // 상세영업상태코드
    private String detailedBusinessStatusName;  // 상세영업상태명

    @Column(columnDefinition = "DATE")
    private LocalDate closureDate;  // 폐업일자

    @Column(columnDefinition = "DATE")
    private LocalDate suspensionStartDate;  // 휴업시작일자

    @Column(columnDefinition = "DATE")
    private LocalDate suspensionEndDate;  // 휴업종료일자

    @Column(columnDefinition = "DATE")
    private LocalDate reopeningDate;  // 재개업일자

    private String contactNumber;  // 소재지전화
    private Double areaSize;  // 소재지면적
    private String postalCode;  // 소재지우편번호
    private String fullAddress;  // 소재지전체주소
    private String roadFullAddress;  // 도로명전체주소
    private String roadPostalCode;  // 도로명우편번호
    private String businessName;  // 사업장명

    @Column(columnDefinition = "DATETIME(6)")
    private LocalDateTime lastModified;  // 최종수정시점

    private String dataUpdateType;  // 데이터갱신구분

    @Column(columnDefinition = "DATETIME(6)")
    private LocalDateTime dataUpdateDate;  // 데이터갱신일자

    private String businessCategory;  // 업태구분명
    private Double coordinateX;  // 좌표정보x(epsg5174)
    private Double coordinateY;  // 좌표정보y(epsg5174)

    private String sanitationBusinessType;  // 위생업태명
    private Integer maleEmployees;  // 남성종사자수
    private Integer femaleEmployees;  // 여성종사자수
    private String surroundingEnvironment;  // 영업장주변구분명
    private String grade;  // 등급구분명
    private String waterSupplyType;  // 급수시설구분명

    private Integer totalEmployees;  // 총직원수
    private Integer headOfficeEmployees;  // 본사직원수
    private Integer factoryOfficeEmployees;  // 공장사무직직원수
    private Integer factorySalesEmployees;  // 공장판매직직원수
    private Integer factoryProductionEmployees;  // 공장생산직직원수

    private String buildingOwnership;  // 건물소유구분명
    private Double securityDeposit;  // 보증액
    private Double monthlyRent;  // 월세액

    private String isMultiUseFacility;  // 다중이용업소여부
    private Double totalFacilitySize;  // 시설총규모
    private String traditionalBusinessNumber;  // 전통업소지정번호
    private String traditionalBusinessMainFood;  // 전통업소주된음식
    private String website;  // 홈페이지

    public static List<String> getFieldNames() {
        Field[] declaredFields = BusinessData.class.getDeclaredFields();
        List<String> result = new ArrayList<>();
        for (Field declaredField : declaredFields) {
            result.add(declaredField.getName());
        }

        return result;
    }

    @Override
    public String toString() {
        return "{"
            + "\"businessDataId\":" + businessDataId
            + ", \"serviceName\":\"" + serviceName + "\""
            + ", \"serviceId\":\"" + serviceId + "\""
            + ", \"municipalityCode\":\"" + municipalityCode + "\""
            + ", \"managementNumber\":\"" + managementNumber + "\""
            + ", \"approvalDate\":" + approvalDate
            + ", \"approvalCancelDate\":" + approvalCancelDate
            + ", \"businessStatusCode\":" + businessStatusCode
            + ", \"businessStatusName\":\"" + businessStatusName + "\""
            + ", \"detailedBusinessStatusCode\":" + detailedBusinessStatusCode
            + ", \"detailedBusinessStatusName\":\"" + detailedBusinessStatusName + "\""
            + ", \"closureDate\":" + closureDate
            + ", \"suspensionStartDate\":" + suspensionStartDate
            + ", \"suspensionEndDate\":" + suspensionEndDate
            + ", \"reopeningDate\":" + reopeningDate
            + ", \"contactNumber\":\"" + contactNumber + "\""
            + ", \"areaSize\":" + areaSize
            + ", \"postalCode\":\"" + postalCode + "\""
            + ", \"fullAddress\":\"" + fullAddress + "\""
            + ", \"roadFullAddress\":\"" + roadFullAddress + "\""
            + ", \"roadPostalCode\":\"" + roadPostalCode + "\""
            + ", \"businessName\":\"" + businessName + "\""
            + ", \"lastModified\":" + lastModified
            + ", \"dataUpdateType\":\"" + dataUpdateType + "\""
            + ", \"dataUpdateDate\":" + dataUpdateDate
            + ", \"businessCategory\":\"" + businessCategory + "\""
            + ", \"coordinateX\":" + coordinateX
            + ", \"coordinateY\":" + coordinateY
            + ", \"sanitationBusinessType\":\"" + sanitationBusinessType + "\""
            + ", \"maleEmployees\":" + maleEmployees
            + ", \"femaleEmployees\":" + femaleEmployees
            + ", \"surroundingEnvironment\":\"" + surroundingEnvironment + "\""
            + ", \"grade\":\"" + grade + "\""
            + ", \"waterSupplyType\":\"" + waterSupplyType + "\""
            + ", \"totalEmployees\":" + totalEmployees
            + ", \"headOfficeEmployees\":" + headOfficeEmployees
            + ", \"factoryOfficeEmployees\":" + factoryOfficeEmployees
            + ", \"factorySalesEmployees\":" + factorySalesEmployees
            + ", \"factoryProductionEmployees\":" + factoryProductionEmployees
            + ", \"buildingOwnership\":\"" + buildingOwnership + "\""
            + ", \"securityDeposit\":" + securityDeposit
            + ", \"monthlyRent\":" + monthlyRent
            + ", \"isMultiUseFacility\":\"" + isMultiUseFacility + "\""
            + ", \"totalFacilitySize\":" + totalFacilitySize
            + ", \"traditionalBusinessNumber\":\"" + traditionalBusinessNumber + "\""
            + ", \"traditionalBusinessMainFood\":\"" + traditionalBusinessMainFood + "\""
            + ", \"website\":\"" + website + "\""
            + "}";
    }
}
