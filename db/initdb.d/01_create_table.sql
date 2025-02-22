DROP DATABASE IF EXISTS danal_batch;

CREATE DATABASE danal_batch;
USE danal_batch;

-- batch
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_CONTEXT;
DROP TABLE IF EXISTS BATCH_STEP_EXECUTION;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_PARAMS;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION;
DROP TABLE IF EXISTS BATCH_JOB_INSTANCE;

DROP TABLE IF EXISTS BATCH_STEP_EXECUTION_SEQ;
DROP TABLE IF EXISTS BATCH_JOB_EXECUTION_SEQ;
DROP TABLE IF EXISTS BATCH_JOB_SEQ;

CREATE TABLE BATCH_JOB_INSTANCE  (
    JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
    VERSION BIGINT ,
    JOB_NAME VARCHAR(100) NOT NULL,
    JOB_KEY VARCHAR(32) NOT NULL,
    constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION  (
    JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
    VERSION BIGINT  ,
    JOB_INSTANCE_ID BIGINT NOT NULL,
    CREATE_TIME DATETIME(6) NOT NULL,
    START_TIME DATETIME(6) DEFAULT NULL ,
    END_TIME DATETIME(6) DEFAULT NULL ,
    STATUS VARCHAR(10) ,
    EXIT_CODE VARCHAR(2500) ,
    EXIT_MESSAGE VARCHAR(2500) ,
    LAST_UPDATED DATETIME(6),
    constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
        references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
    JOB_EXECUTION_ID BIGINT NOT NULL ,
    PARAMETER_NAME VARCHAR(100) NOT NULL ,
    PARAMETER_TYPE VARCHAR(100) NOT NULL ,
    PARAMETER_VALUE VARCHAR(2500) ,
    IDENTIFYING CHAR(1) NOT NULL ,
    constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION  (
    STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
    VERSION BIGINT NOT NULL,
    STEP_NAME VARCHAR(100) NOT NULL,
    JOB_EXECUTION_ID BIGINT NOT NULL,
    CREATE_TIME DATETIME(6) NOT NULL,
    START_TIME DATETIME(6) DEFAULT NULL ,
    END_TIME DATETIME(6) DEFAULT NULL ,
    STATUS VARCHAR(10) ,
    COMMIT_COUNT BIGINT ,
    READ_COUNT BIGINT ,
    FILTER_COUNT BIGINT ,
    WRITE_COUNT BIGINT ,
    READ_SKIP_COUNT BIGINT ,
    WRITE_SKIP_COUNT BIGINT ,
    PROCESS_SKIP_COUNT BIGINT ,
    ROLLBACK_COUNT BIGINT ,
    EXIT_CODE VARCHAR(2500) ,
    EXIT_MESSAGE VARCHAR(2500) ,
    LAST_UPDATED DATETIME(6),
    constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
    STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT ,
    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
        references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
    JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT ,
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_SEQ (
    ID BIGINT NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_STEP_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_EXECUTION_SEQ (
    ID BIGINT NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_JOB_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_SEQ (
    ID BIGINT NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE=InnoDB;

INSERT INTO BATCH_JOB_SEQ (ID, UNIQUE_KEY) select * from (select 0 as ID, '0' as UNIQUE_KEY) as tmp where not exists(select * from BATCH_JOB_SEQ);

-- business_data
DROP TABLE IF EXISTS business_data;

CREATE TABLE business_data (
    businessDataId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '번호',
    serviceName VARCHAR(255) COMMENT '개방서비스명',
    serviceId VARCHAR(255) COMMENT '개방서비스아이디',
    municipalityCode VARCHAR(100) COMMENT '개방자치단체코드',
    managementNumber VARCHAR(100) COMMENT '관리번호',

    approvalDate DATE COMMENT '인허가일자',
    approvalCancelDate DATE COMMENT '인허가취소일자',

    businessStatusCode INT COMMENT '영업상태구분코드',
    businessStatusName VARCHAR(100) COMMENT '영업상태명',

    detailedBusinessStatusCode INT COMMENT '상세영업상태코드',
    detailedBusinessStatusName VARCHAR(100) COMMENT '상세영업상태명',

    closureDate DATE COMMENT '폐업일자',
    suspensionStartDate DATE COMMENT '휴업시작일자',
    suspensionEndDate DATE COMMENT '휴업종료일자',
    reopeningDate DATE COMMENT '재개업일자',

    contactNumber VARCHAR(50) COMMENT '소재지전화',
    areaSize DOUBLE COMMENT '소재지면적',
    postalCode VARCHAR(20) COMMENT '소재지우편번호',
    fullAddress VARCHAR(500) COMMENT '소재지전체주소',
    roadFullAddress VARCHAR(500) COMMENT '도로명전체주소',
    roadPostalCode VARCHAR(20) COMMENT '도로명우편번호',
    businessName VARCHAR(255) COMMENT '사업장명',

    lastModified DATETIME(6) COMMENT '최종수정시점',
    dataUpdateType VARCHAR(50) COMMENT '데이터갱신구분',
    dataUpdateDate DATETIME(6) COMMENT '데이터갱신일자',

    businessCategory VARCHAR(100) COMMENT '업태구분명',
    coordinateX DOUBLE COMMENT '좌표정보x(epsg5174)',
    coordinateY DOUBLE COMMENT '좌표정보y(epsg5174)',

    sanitationBusinessType VARCHAR(100) COMMENT '위생업태명',
    maleEmployees INT COMMENT '남성종사자수',
    femaleEmployees INT COMMENT '여성종사자수',
    surroundingEnvironment VARCHAR(100) COMMENT '영업장주변구분명',
    grade VARCHAR(50) COMMENT '등급구분명',
    waterSupplyType VARCHAR(100) COMMENT '급수시설구분명',

    totalEmployees INT COMMENT '총직원수',
    headOfficeEmployees INT COMMENT '본사직원수',
    factoryOfficeEmployees INT COMMENT '공장사무직직원수',
    factorySalesEmployees INT COMMENT '공장판매직직원수',
    factoryProductionEmployees INT COMMENT '공장생산직직원수',

    buildingOwnership VARCHAR(100) COMMENT '건물소유구분명',
    securityDeposit DOUBLE COMMENT '보증액',
    monthlyRent DOUBLE COMMENT '월세액',

    isMultiUseFacility VARCHAR(10) COMMENT '다중이용업소여부',
    totalFacilitySize DOUBLE COMMENT '시설총규모',
    traditionalBusinessNumber VARCHAR(100) COMMENT '전통업소지정번호',
    traditionalBusinessMainFood VARCHAR(255) COMMENT '전통업소주된음식',
    website VARCHAR(255) COMMENT '홈페이지'
);


-- test
CREATE INDEX business_data_serviceId_IDX USING BTREE ON danal_batch.business_data (serviceId);

-- error_log

DROP TABLE IF EXISTS error_log;

CREATE TABLE error_log (
    errorLogId BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '번호',
    message TEXT COMMENT '에러 내용'
);