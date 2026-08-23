# 네이밍/레이어 가이드

패키지 루트: `pf.cyj.sys`
레이어 순서: `entity → dto(request/response) → repository → service → controller`
파일명 규칙: 단어 단위로 3~5자 약어 + PascalCase (예: `AnalysisDataset` → `AnlDset`)

## 약어 사전

| 원어         | 약어 | 원어     | 약어 |
| ------------ | ---- | -------- | ---- |
| Analysis     | Anl  | Dataset  | Dset |
| Column       | Col  | Domain   | Dmn  |
| Prediction   | Pdt  | Standard | Std  |
| Request      | Req  | Common   | Cmn  |
| Code         | Cd   | Group    | Grp  |
| User         | Usr  | Account  | Acnt |
| Refresh      | Rfsh | Token    | Tkn  |
| Notification | Noti | Report   | Rpt  |
| Export       | Exp  | Status   | Stat |

## 테이블 ↔ 엔티티 매핑

| DB 테이블               | 엔티티 클래스            | PK 전략                            |
| ----------------------- | ------------------------ | ---------------------------------- |
| APP_USER                | `AppUsr`                 | IDENTITY                           |
| APP_ROLE                | `AppRole`                | IDENTITY                           |
| USER_ROLE               | `UsrRole` (+`UsrRoleId`) | 복합키(@IdClass)                   |
| OAUTH_ACCOUNT           | `OauthAcnt`              | IDENTITY                           |
| REFRESH_TOKEN           | `RfshTkn`                | IDENTITY                           |
| COMMON_CODE_GROUP       | `CmnCdGrp`               | 자연키(String)                     |
| COMMON_CODE             | `CmnCd` (+`CmnCdId`)     | 복합키(@IdClass)                   |
| ANALYSIS_DATASET        | `AnlDset`                | 업무키(String, 서비스단 채번 예정) |
| ANALYSIS_COLUMN         | `AnlCol`                 | IDENTITY                           |
| DOMAIN_CODE             | `DmnCd`                  | 자연키(String)                     |
| DOMAIN_PREDICTION       | `DmnPdt`                 | IDENTITY                           |
| STANDARD_DOMAIN_REQUEST | `StdDmnReq`              | IDENTITY                           |
| STANDARD_DOMAIN         | `StdDmn`                 | `AnlCol`과 PK 공유(@MapsId)        |
| ANALYSIS_JOB            | `AnlJob`                 | IDENTITY                           |
| ANALYSIS_JOB_LOG        | `AnlJobLog`              | IDENTITY                           |
| NOTIFICATION_LOG        | `NotiLog`                | IDENTITY                           |
| REPORT_EXPORT_LOG       | `RptExpLog`              | IDENTITY                           |

## 이번 단계에서 내린 설계 결정

1. **Y/N 플래그 처리** — DB는 `CHAR(1)`(Y/N)을 유지하되, 엔티티는 Java `boolean` 필드로 두고
   `entity/converter/YnConverter`(`AttributeConverter<Boolean,String>`)로 자동 변환합니다.
2. **상태값 Enum화** — `USER_STATUS`, `DATASET_STATUS`, `REQUEST_STATUS`, `JOB_STATUS`,
   `EXEC_STATUS`, `SCHEDULE_TYPE`, `CHANNEL`, `SEND_STATUS`는 `entity/type/*` 패키지의
   Java enum + `@Enumerated(EnumType.STRING)`으로 매핑해 오타/잘못된 상태값 삽입을 컴파일 타임에 방지합니다.
   (단, `JOB_TYPE`/`DBMS_TYPE_CODE`처럼 DB에 CHECK 제약이 없는 개방형 코드는 String으로 유지)
3. **타임스탬프** — `CREATED_AT/REQUESTED_AT/PREDICTED_AT/...`류는 Hibernate의
   `@CreationTimestamp`/`@UpdateTimestamp`로 애플리케이션이 직접 채웁니다. DB의 `DEFAULT SYSTIMESTAMP`는
   MyBatis 등 JPA를 거치지 않는 경로를 위한 안전장치로 그대로 남겨둡니다.
4. **업무키(문자열 PK)** — `ANALYSIS_DATASET.DATASET_ID`/`REQUEST_NO`는 DB 트리거가 아니라
   (다음 단계인) Service 계층의 채번 유틸이 `SEQ_DATASET_ID`/`SEQ_REQUEST_NO`를 직접 조회해 채웁니다.
   JPA는 INSERT 이전에 PK 값을 알아야 영속성 컨텍스트를 관리할 수 있기 때문입니다. DB 트리거는
   MyBatis 등 다른 경로로 직접 INSERT하는 경우를 위한 안전장치로 유지합니다.
5. **연관관계** — FK는 원시 타입(Long) 대신 `@ManyToOne`/`@OneToOne` 객체 참조로 매핑했습니다.
   `STANDARD_DOMAIN`은 `ANALYSIS_COLUMN`과 PK를 공유하는 1:1이라 `@MapsId`를 사용했습니다.
   `USER_ROLE`, `COMMON_CODE`는 복합키 조인 테이블이라 `@IdClass` + 별도 ID 클래스를 사용했습니다.
6. **JPA/MyBatis 병행** — `build.gradle`에 두 스타터가 모두 있는 이유는, 엔티티/연관관계가 명확한
   CRUD는 JPA로, 승인 대기열처럼 조건이 자주 바뀌는 동적 조회·대시보드 집계는 이후 MyBatis Mapper로
   분리할 계획이기 때문입니다(설계서 3-2절 참고).
