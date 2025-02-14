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