### 사전준비
하기 링크(공공데이터포털) "전국일반음식점표준데이터" 파일을 다운로드하고, CSV 파일 압축을 해제합니다.
데이터 항목으로는 "번호", "개방서비스명", "개방서비스아이디", "개방자치단체코드", "관리번호" 등이 있습니다.
- https://www.data.go.kr/data/15096283/standard.do

압축 해제한 파일을 `/danal/batch-service/src/main/resources` 로 옮깁니다.

---

### 개발Stack
- 필수 조건
  - JDK 17
  - Spring Framework 6.1.5
  - Spring Batch 5.0.0
  - mysql 8.0.33
  - gradle 8.8
- 선택사항
  - docker

멀티 모듈로 환경을 구성하였습니다. 추후 필요 시 해당 모듈을 재사용하기 위함입니다. `docker` 를 추가하였으며 `MySQL` 호스트를 특정할 수 없어서 선택하였습니다.

---

### 환경변수 및 실행방법

- localhost 실행 시 
  - Batch 를 실행시킬 때 환경변수로 `SPRING_PROFILES_ACTIVE` 가 필요합니다. 기본값은 `local` 이며 `application-local.properties` 파일을 로드합니다. localhost 에 `MySQL` 이 구동되고 있어야 합니다.
  - 구동을 위한 `MySQL` 서버가 별도로 필요하며 해당 호스트의 값을 `application-local.properties` 에서 변경 후 로컬 실행바랍니다. (docker 사용 시 변경 불필요)
- docker 실행 시
  - 프로젝트 root 경로에서 터미널을 사용하여 아래의 방법으로 실행합니다.
    - make SW 있는 경우
      - 해당 프로젝트는 `Makefile` 을 이용하여 쉽게 구동할 수 있게 구성하였으며 `make all` 명령어를 통해 `MySQL` 과 `batch-service` 를 `docker` 에서 구동합니다.
    - make SW 없는 경우
      - 구동을 위한 `MySQL` 서버가 별도로 필요하며 해당 호스트의 값을 `application-local.properties` 에서 변경 후 로컬 실행바랍니다.

---

### DDL

DDL은 다음 경로에 있습니다.
- `db/initdb.d/01_create_table.sql`

---

# 정리

## Table of Contents

- [프로젝트 개요](#프로젝트-개요)
- [주요 기능](#주요-기능)
- [아키텍처 및 구성](#아키텍처-및-구성)
  - [Job 및 Step 구성](#job-및-step-구성)
  - [파티셔닝 및 동적 범위 처리](#파티셔닝-및-동적-범위-처리)
- [주요 컴포넌트 설명](#주요-컴포넌트-설명)
  - [ItemReader](#itemreader)
  - [ItemWriter](#itemwriter)
  - [Listener 구성](#listener-구성)
  - [Partitioner](#partitioner)
- [오류 처리 및 무결성 검증](#오류-처리-및-무결성-검증)
- [운영 및 배포](#운영-및-배포)
- [참고 자료](#참고-자료)

---

## 프로젝트 개요

이 애플리케이션은 CSV 파일에 저장된 비즈니스 데이터를 읽어와,  
데이터 변환 및 검증 후 데이터베이스에 저장하는 배치 작업을 수행합니다.  
주요 특징은 다음과 같습니다:

- **Fault Tolerance**: 스킵 및 재시도 정책을 통해 일부 레코드에서 오류가 발생해도 전체 배치 작업은 중단되지 않습니다.
- **파티셔닝**: 전체 데이터 범위를 Partitioning하여 병렬로 처리하고, 각 Partition 별로 지정된 범위(시작/종료 라인)를 기반으로 데이터를 읽습니다.
- **Listener 활용**: Step 및 Chunk 실행, 스킵 이벤트 등에 대해 커스텀 Listener를 등록하여 배치 진행 상황과 오류를 로깅합니다.
- **동적 SQL 생성**: 도메인(BusinessData) 정보를 기반으로 SQL을 동적으로 생성하여 Writer에 적용합니다.

---

## 주요 기능

- **CSV 파일 입력**: JobExecutionContext에서 지정된 파일(resource)과, StepExecutionContext의 startLine, endLine을 기준으로 데이터를 읽어옵니다.
- **데이터베이스 저장**: JdbcBatchItemWriter를 사용하여 데이터를 DB에 저장하며, SQL은 `SqlGenerator`를 통해 동적으로 생성됩니다.
- **파티셔닝 처리**: `CustomPartitioner`를 사용하여 전체 입력 데이터의 총 라인 수(totalLines)를 기준으로 Partition을 생성하고, 각 Partition 별로 처리 범위를 관리합니다.
- **오류 및 스킵 처리**: Fault Tolerant 설정과 함께 FlatFileParseException 및 IllegalAccessException 발생 시 스킵 처리하며, BatchSkipListener를 통해 스킵 이벤트를 로깅합니다.
- **멀티스레드 실행**: ThreadPoolTaskExecutor를 사용하여 Partition 별로 병렬로 Step을 실행합니다.

---

## 아키텍처 및 구성

### Job 및 Step 구성

- **Job**: `importBusinessDataJob`
  - Job은 `partitionedStep`으로 시작하며, JobListener를 통해 전체 Job 실행 전후 이벤트를 처리합니다.
- **Master Step (partitionedStep)**:
  - 파티셔닝을 사용하여 전체 데이터를 여러 Partition으로 분할합니다.
  - 각 Partition은 CustomPartitioner를 통해 생성되며, Partition의 범위(startLine, endLine)와 총 라인 수(totalLines)가 Job/Step ExecutionContext에 저장됩니다.
- **Slave Step (chunkStep)**:
  - 실제 CSV 파일을 읽어 데이터를 처리하는 Step입니다.
  - Chunk 크기는 고정 10,000으로 설정되어 있으며, Fault Tolerant 기능을 통해 스킵/재시도 정책을 적용합니다.
  - 이 Step에는 BatchStepListener, ChunkSizeTrackingListener, BatchSkipListener가 등록되어 있습니다.

### 파티셔닝 및 동적 범위 처리

- **CustomPartitioner**:
  - JobExecutionContext의 `totalLines` 값을 기반으로 Partition을 생성합니다.
  - 각 Partition의 ExecutionContext에는 Partition의 시작 라인(`partitionStart`), 종료 라인(`partitionEnd`)이 저장됩니다.
- **StepScope**:
  - Reader 및 Slave Step에 `@StepScope`를 적용하여, 각 Partition 실행 시점에 실행 컨텍스트에 맞게 빈이 생성되도록 합니다.

---

## 주요 컴포넌트 설명

### ItemReader

- **구현**: `FlatFileItemReader<BusinessData>`
- **구성**:
  - 리소스와 인코딩(EUC-KR)을 설정합니다.
  - `linesToSkip`를 통해 `startLine` 만큼의 라인을 건너뜁니다.
  - 커스텀 `LineMapper`는 각 라인을 BusinessDataFieldSetMapper를 이용해 도메인 객체(BusinessData)로 매핑하며, `endLine`을 초과하면 더 이상 읽지 않습니다.

### ItemWriter

- **구현**: `JdbcBatchItemWriter<BusinessData>`
- **구성**:
  - BeanPropertyItemSqlParameterSourceProvider를 사용하여 BusinessData 객체의 프로퍼티를 매핑합니다.
  - SqlGenerator를 통해 생성한 INSERT SQL을 사용합니다.
  - DataSource를 설정하고, 업데이트 확인(assertUpdates=false)을 비활성화합니다.

### Listener 구성

- **BatchStepListener**:
  - Step 실행 전후의 이벤트를 로깅합니다.
- **ChunkSizeTrackingListener**:
  - 각 Chunk의 진행 상황(처리 건수 등)을 모니터링하여 로깅합니다.
- **BatchSkipListener**:
  - 스킵 발생 시(FlatFileParseException, IllegalAccessException)에 대해 로깅합니다.
- **JobListener**:
  - Job 실행 전후의 이벤트를 처리합니다.

### Partitioner

- **CustomPartitioner**:
  - 전체 레코드 수(`totalLines`)를 기준으로 Partition을 생성합니다.
  - 각 Partition에 대해 시작 라인과 종료 라인을 계산하여 ExecutionContext에 저장합니다.

---

## 오류 처리 및 무결성 검증

- **Fault Tolerance**:
  - Slave Step에서는 `.faultTolerant()`를 적용하고, `FlatFileParseException`과 `IllegalAccessException`에 대해 `.skip()` 및 `.skipLimit(10)`을 설정하여 오류가 발생한 아이템을 스킵 처리합니다.
- **데이터 무결성 검증**:
  - JobExecution 및 StepExecution의 ExecutionContext에 `totalLines`, `processedCount`, `skipCount` 등을 저장하고, 최종 DB에 저장된 데이터 수(businessData) 및 에러 로그(errorCount)와 비교하여 무결성을 점검합니다.