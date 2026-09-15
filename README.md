# 🔍 AI 기반 데이터 표준 도메인 추천 시스템 (ai-data-domain-classifier)

레거시 데이터 거버넌스 솔루션의 **AI 도메인판별 모듈**을, 회사 소스코드를 사용하지 않고 아이디어·아키텍처 패턴만 참고해 현재 기술스택으로 **처음부터 재설계·재구현**한 개인 포트폴리오 프로젝트입니다.<br/>
"컬럼명을 분석해 AI가 표준 도메인(코드·날짜·금액·연락처 등)을 추천하고, 담당자가 검토·승인하면 표준으로 확정되는 시스템"을 목표로, AI 추천과 사람의 승인이 함께 맞물려 돌아가는 Human-in-the-loop 워크플로우를 구현했습니다.

> ✅ 원본: 파견 근무 중 담당했던 데이터 거버넌스 솔루션의 일부 기능<br/>
> ✅ 회사 소스코드는 사용하지 않고 아이디어·아키텍처 패턴만 재구현(테이블/클래스명 전부 새로 지음)<br/>
> ✅ 고객사명 등 식별정보는 포함하지 않음

---

## 1. 프로젝트 개요

담당했던 솔루션 전체(Java 약 19.8만 라인 · 5,200여 파일) 중 **AI 도메인판별 모듈**을 포트폴리오 소재로 선정했습니다.

- **프로젝트 성격**: 개인 재구현 프로젝트 (1인, 원본은 파견 근무 중 담당했던 데이터 거버넌스 솔루션의 일부)
- **도메인**: 데이터 거버넌스 — 컬럼 메타 등록 → AI 도메인 판별 → 승인 워크플로우 → 확정/리포트
- **대상 사용자**: 데이터 표준화 담당자(일반 사용자) / 승인자 / 관리자
- **역할 체계**: 3단계 (`ROLE_ADMIN` / `ROLE_REVIEWER` / `ROLE_USER`)

### 왜 이 모듈을 선택했는가

1. **현재 지향하는 기술스택과 맞닿아 있음** — 외부 API 호출(`RestClient`), API 키 관리(`spring-dotenv`), `OAuth2 Client`, `Redis`, `JWT`를 모두 자연스럽게 소비하는 유일한 후보였습니다.
2. **CRUD가 아니라 "판단"을 다루는 로직** — 컬럼 메타를 근거로 외부 AI에 판단을 요청하고, 확률 기반 Top-N 후보를 돌려받는 구조라 단순 게시판·CRUD 포트폴리오 대비 변별력이 있습니다.
3. **AI 추천 + 사람 승인, 이해하기 쉬운 스토리** — "AI 추천 → 담당자 확인 → 확정" 흐름은 비개발자에게도 한 문장으로 설명되면서, 내부적으로는 캐싱·인증·감사이력 같은 실무형 디테일이 채워져 있습니다.

### AS-IS → TO-BE 한눈에 보기

| 항목 | AS-IS (원본) | TO-BE (이번 재구현) |
|---|---|---|
| AI 연동 | 사내망에 상시 기동된 Python gRPC 분류 모델 | `RestClient`로 OpenAI Chat Completions 규격 외부 AI API 호출 (`.env`로 키 관리) |
| 배치 실행 | Quartz + `Runtime.exec()` 쉘 스크립트(별도 프로세스) | Spring 내장 `@Scheduled` + DB 상태 관리(템플릿 메서드 패턴) |
| 인증 | 세션 기반 폼 로그인만 지원 | JWT(Stateless) + Redis + OAuth2.0(Google·Kakao·Naver) |
| 프론트엔드 | JSP + IBSheet, 새로고침 기반 렌더링 | React 17 + Next.js 12 + Redux Toolkit + redux-saga + antd |
| 캐싱 | 동일 컬럼도 매번 모델 재호출 | Redis 캐시(`predict:{columnId}`, TTL 1시간) |
| 오류 원인 구분 | 로그 텍스트로만 구분 | `INTERNAL_ERROR` / `CALL_ERROR` 상태 분리 |

---

## 2. 개발 형태

- **개발 인원**: 1인 (개인 포트폴리오 프로젝트)
- **역할**: 기획 · 설계(리팩토링 설계서 작성) · 백엔드 · 프론트엔드 · DDL/DML 설계 전체를 단독 진행
- **산출물**: 리팩토링 설계서(`.md`), ERD, DDL/DML/DCL(`.sql`, Oracle 18c XE), 백엔드(Java 131개 파일 = 소스 123 + 테스트 8), 프론트엔드(JS 43개 파일)

---

## 3. 기술 스택

`build.gradle` / `package.json`에 실제로 등록되어 있고, 코드에서 실제로 쓰이는 버전을 그대로 반영했습니다.

### Backend

![Java](https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%204.0.8-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![JPA](https://img.shields.io/badge/Spring%20Data%20JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![JWT](https://img.shields.io/badge/JWT%20(jjwt%200.11.5)-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2.0%20Client-4285F4?style=for-the-badge&logo=oauth&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-BC0031?style=for-the-badge&logoColor=white)

### Frontend

![React](https://img.shields.io/badge/React%2017-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Next.js](https://img.shields.io/badge/Next.js%2012-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)
![Redux Toolkit](https://img.shields.io/badge/Redux%20Toolkit-764ABC?style=for-the-badge&logo=redux&logoColor=white)
![Redux Saga](https://img.shields.io/badge/Redux--Saga-999999?style=for-the-badge&logo=redux-saga&logoColor=white)
![Ant Design](https://img.shields.io/badge/antd%204.8-0170FE?style=for-the-badge&logo=antdesign&logoColor=white)
![Axios](https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white)

### Database / Infra

![Oracle](https://img.shields.io/badge/Oracle%2018c%20XE-F80000?style=for-the-badge&logo=oracle&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

### External API / 문서화

![RestClient](https://img.shields.io/badge/RestClient%20%E2%86%92%20외부%20AI%20API-412991?style=for-the-badge&logo=openai&logoColor=white)
![PDFBox](https://img.shields.io/badge/Apache%20PDFBox%203.0.5-D22128?style=for-the-badge&logo=apache&logoColor=white)
![springdoc](https://img.shields.io/badge/springdoc--openapi%203.1.0-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

---

## 4. 시스템 구조

### ERD 핵심 테이블

`APP_USER`를 중심으로 인증/분석대상/AI판별/승인/배치/알림 도메인이 연결되는 구조입니다.

- **AUTH**: `APP_USER`, `APP_ROLE`, `USER_ROLE`, `OAUTH_ACCOUNT`, `REFRESH_TOKEN`
- **분석대상**: `ANALYSIS_DATASET`, `ANALYSIS_COLUMN`
- **AI 판별**: `DOMAIN_CODE`(표준 도메인 후보 마스터), `DOMAIN_PREDICTION`(컬럼별 Top-N 스냅샷)
- **승인**: `STANDARD_DOMAIN_REQUEST`(신청), `STANDARD_DOMAIN`(확정본, `VERSION_NO` 관리)
- **배치**: `ANALYSIS_JOB`, `ANALYSIS_JOB_LOG`(5종 상태)
- **알림/리포트**: `NOTIFICATION_LOG`, `REPORT_EXPORT_LOG`
- **공통코드**: `COMMON_CODE_GROUP`, `COMMON_CODE`

### 인증/인가 — JWT + Redis + OAuth2.0

```
로그인(ID/PW 또는 Google/Kakao/Naver)
  → AccessToken(30분) + RefreshToken(14일) 발급
  → RefreshToken은 Redis에 refresh:{userId} 키로 1차 저장(TTL)
     + REFRESH_TOKEN 테이블에는 SHA-256 해시로 감사이력만 기록
  → AccessToken은 쿠키/localStorage가 아니라 Redux(메모리)에만 보관(XSS로 탈취돼도 새로고침 시 사라짐)
  → 401 발생 시 axios 인터셉터가 /auth/reissue 로 자동 재발급(동시 요청은 큐에 대기 후 재시도)
  → 로그아웃 시 Redis의 refresh:{userId} 삭제 + REFRESH_TOKEN.REVOKED_YN=true 로 폐기
```

- **역할 3단계**: `ROLE_ADMIN`(공통코드·사용자 권한 관리) / `ROLE_REVIEWER`(승인/반려) / `ROLE_USER`(등록·판별 요청·확정 신청) — 승인(REVIEW) API는 `@PreAuthorize("hasAnyRole('ADMIN','REVIEWER')")`로 메서드 단위 제어
- **솔직한 한계**: Refresh Token은 즉시 회수되지만, 이미 발급된 Access Token 자체를 즉시 무효화하는 jti 블랙리스트는 이번 구현 범위에는 없습니다. 로그아웃 후에도 Access Token은 남은 TTL(최대 30분) 동안은 유효합니다 — "다음 개선 방향" 참고.

### AI 도메인 판별 & 캐싱 플로우

`POST /api/domain-predictions/columns/{columnId}/predict` 요청 처리 시퀀스:

```
1. Service → Redis 캐시 조회 (predict:{columnId})
2. [캐시 HIT] 저장된 Top-N 결과를 즉시 반환(cacheHitYn=true로 표시만 하고 원본 데이터는 바꾸지 않음)
3. [캐시 MISS] 컬럼 메타 조회(JPA) → 물리명/한글명/영문명/데이터타입/특성을 프롬프트로 구성
4. [캐시 MISS] RestClient로 외부 AI API(/chat/completions, response_format=json_object) 호출
5. [캐시 MISS] 응답 파싱 → 후보 목록(DOMAIN_CODE) 밖의 코드는 방어적으로 제거(hallucination 방지)
6. [캐시 MISS] DOMAIN_PREDICTION을 컬럼당 스냅샷으로 통째로 교체(delete 후 insert) + Redis SETEX 1시간
```

- `DOMAIN_PREDICTION`은 판별 이력이 아니라 "현재 Top-N 스냅샷"이라, 같은 컬럼을 재판별하면 이전 결과를 지우고 새 결과로 교체합니다(그대로 insert만 하면 `UQ_DOMAIN_PREDICTION` 제약에 걸려 `ORA-00001`이 남 — 7. 트러블슈팅 참고).
- 외부 AI 호출 실패(네트워크 오류, 4xx/5xx, 파싱 실패, 유효 후보 0개)는 전부 `ExternalApiException`으로 변환해, "우리 로직 문제"와 "외부 API 문제"를 로그에서 바로 구분합니다.

### 승인 워크플로우

```
PENDING(AI 추천 채택 or 담당자 직접 신청)
  → 승인자 검토(ROLE_REVIEWER 이상)
     ├─ 승인 → STANDARD_DOMAIN 확정 반영(VERSION_NO 증가) + 이메일 알림
     └─ 반려(사유 필수) → 재신청 시 PENDING으로 복귀
```

- `STANDARD_DOMAIN_REQUEST.AI_SUGGESTED_YN`으로 "AI 추천을 그대로 채택했는지 / 다른 후보를 직접 골랐는지"를 구분 저장 → 추후 AI 추천 채택률을 집계할 수 있는 설계입니다.
- 이미 처리 대기(PENDING) 중인 신청이 있는 컬럼은 중복 신청을 막아, 승인자가 같은 컬럼에 대한 신청을 여러 건 보는 상황을 방지합니다.
- 알림 발송이 실패해도(SMTP 오류 등) 승인/반려 자체는 롤백하지 않고, 발송 실패라는 사실만 `NOTIFICATION_LOG`에 정상적으로 남깁니다.

### 배치 재판별

```
Spring @Scheduled(1분 주기) → READY 상태 ANALYSIS_JOB 조회
  → jobType=DOMAIN_PREDICT 인 작업만 실행
  → 데이터셋의 모든 컬럼을 순회하며 재판별(컬럼 하나 실패해도 배치 전체는 계속)
  → 5종 상태(READY/RUNNING/SUCCESS/INTERNAL_ERROR/CALL_ERROR)로 이력 기록
```

- 원본의 Quartz + `Runtime.exec()` 쉘 실행 방식을 애플리케이션 내장 스케줄러로 대체해, 별도 프로세스 기동 없이 컨테이너 환경에서도 그대로 동작합니다.
- 템플릿 메서드 패턴(`AbstractAnlJobExecutor`)으로 "실행 시작 기록 → 실제 실행 → 성공/실패 기록"을 공통 처리하고, 예외 종류에 따라 `CALL_ERROR`(외부 API 문제)와 `INTERNAL_ERROR`(내부 로직 문제)를 자동으로 구분합니다.

---

## 5. 주요 기능

| # | 기능 | 설명 |
|---|---|---|
| 1 | 분석 대상 등록 | 데이터셋(테이블) 등록 + 컬럼 메타(물리명/한글·영문 논리명/데이터타입 등) 일괄 등록 |
| 2 | AI 도메인 추천 | 컬럼 메타를 프롬프트로 구성해 외부 AI API에 질의, 확률 기반 Top-3 후보 제시(후보 목록 밖 응답은 방어적으로 제거) |
| 3 | 캐시 기반 응답 최적화 | 동일 컬럼 재판별은 Redis 캐시로 즉시 응답(TTL 1시간), `RESPONSE_MS`/`CACHE_HIT_YN` 기록 |
| 4 | 승인 워크플로우 | AI 추천 채택 또는 직접 후보 신청 → `ROLE_REVIEWER` 이상 승인/반려 → 확정 표준 반영 + 이메일 통보 |
| 5 | 배치 재판별 | 데이터셋 단위로 배치 등록(1회/CRON), 실행 이력을 5종 상태로 관리 |
| 6 | PDF 리포트 | 데이터셋 단위 판별 결과를 한글 폰트가 임베드된 PDF로 다운로드(Apache PDFBox), 다운로드 이력 자동 기록 |
| 7 | 인증/보안 | JWT(Access 30분/Refresh 14일) + Redis + OAuth2.0(Google·Kakao·Naver) 소셜 로그인, 역할 3단계 |

---

## 6. 화면 시연

시연 시나리오 흐름에 따라 실제 화면을 캡처(GIF/이미지)했습니다. 아래 각 항목의 이미지 경로만 실제 파일로 교체하면 됩니다(`docs/imgs/` 폴더에 넣고 파일명만 맞추는 걸 추천 — GitHub는 리포지토리 기준 상대경로를 그대로 렌더링합니다).

### 1. 일반사용자 — 회원가입 · 로그인
![회원가입 및 로그인](docs/imgs/01_signup_login.gif)

### 2. 데이터셋 및 컬럼 등록
![데이터셋 및 컬럼 등록](docs/imgs/02_dataset_column.gif)

### 3. AI 판별 실행
![AI 판별 실행](docs/imgs/03_ai_predict.gif)

### 4. 표준 도메인 확정 신청
![표준 도메인 확정 신청](docs/imgs/04_std_domain_request.gif)

### 5. [승인자] 승인 처리
![승인자 승인 처리](docs/imgs/05_reviewer_approve.gif)

### 6. [관리자] 공통코드 등록 및 역할 부여
![관리자 공통코드 등록 및 역할 부여](docs/imgs/06_admin_common_code_role.gif)

### 7. 소셜 로그인
![소셜 로그인](docs/imgs/07_social_login.gif)

---

## 7. 트러블슈팅

### 사례 1. PDF 리포트 다운로드 시 `LazyInitializationException`

| 구&nbsp;분 | 내용 |
|:---:|---|
| 문제 | 판별 결과가 있는 컬럼이 섞인 데이터셋만 PDF 다운로드가 500 에러로 실패(판별 결과가 없는 컬럼만 있으면 우연히 성공). "PDF 생성이 안 되는 것 같은데?"라는 증상만 있고 원인이 바로 안 보임 |
| 원인 | 리포지토리 메서드들이 각자 자기 `@Transactional(readOnly=true)`로 실행되고 끝나는 즉시 세션이 닫힘. `DmnPdt.dmnCd`는 `FetchType.LAZY`인데, 세션이 닫힌 뒤 반복문에서 `getDmnCd().getDomainNameKo()`를 호출해 `LazyInitializationException`("no Session")이 발생 |
| 해결 | `generateDatasetReport()` 메서드 전체를 하나의 읽기 전용 트랜잭션(세션)으로 묶어, 조회와 지연 로딩 접근이 같은 세션 안에서 이뤄지게 변경 |
| 성과 | 판별 결과 유무와 무관하게 모든 데이터셋에서 PDF 다운로드가 안정적으로 동작 |
| 학습 | "지연 로딩 예외가 랜덤하게 보인다"는 사실 자체가 트랜잭션 경계 문제일 가능성이 높다는 것, Service 메서드 하나가 여러 리포지토리 호출을 조합할 때는 트랜잭션 경계를 직접 챙겨야 한다는 것을 체득 |

### 사례 2. 한글 폰트 임베드 — OTF/CFF 서브셋 실패

| 구&nbsp;분 | 내용 |
|:---:|---|
| 문제 | PDFBox 내장 표준 폰트(Helvetica 등)는 한글 글리프가 없어 한글이 깨짐 → 한글 서브셋 폰트(`NotoSansKR-Regular.ttf`)를 붙였는데, 로딩 단계와 서브셋 단계에서 순서대로 다른 예외가 발생 |
| 원인 | 이 폰트는 확장자만 `.ttf`이고 실제로는 OpenType/CFF 외곽선 폰트(파일 시작 4바이트가 TrueType이 아닌 "OTTO"). `PDType0Font.load(document, InputStream)`은 무조건 TrueType 전용 임베더를 써서 "CFF outlines are not supported" 예외가 났고, 이를 고친 뒤에도 `embedSubset=true` 옵션이 PDFBox/FontBox의 TrueType 전용 서브셋 구현과 충돌해 `UnsupportedOperationException`이 발생 |
| 해결 | `OTFParser`로 직접 파싱해 `OpenTypeFont`로 인식시키고, `TrueTypeFont`를 받는 `PDType0Font.load()` 오버로드(CFF 지원 임베더 선택)를 사용. 서브셋은 `embedSubset=false`로 꺼서 이미 한글 완성형/영문/숫자로 한 번 추린 폰트(약 1.9MB) 전체를 그대로 임베드 |
| 성과 | 폰트 용량이 약간 늘긴 했지만, 서브셋 실패로 다운로드 자체가 안 되는 것보다 훨씬 안정적으로 동작 |
| 학습 | 폰트 파일의 확장자와 실제 내부 외곽선 포맷(TrueType glyf vs OpenType/CFF)이 다를 수 있고, 그 차이가 라이브러리의 어떤 오버로드/구현체를 타는지를 좌우한다는 것을 체득 |

### 사례 3. `jwt-decode` v4 마이그레이션 — 역할 기반 화면 분기 오작동

| 구&nbsp;분 | 내용 |
|:---:|---|
| 문제 | 관리자 계정으로 로그인해도 일반 사용자 화면만 보이는 버그. 콘솔에 에러가 찍히지 않아 한동안 원인을 못 찾음 |
| 원인 | `jwt-decode`가 v3→v4로 올라가며 default export가 사라짐. `import jwtDecode from "jwt-decode"`로 그대로 가져오면 `jwtDecode`가 `undefined`가 돼 호출 시 예외가 나고, `decodeRoles()`의 catch 블록이 그 예외를 조용히 삼켜 `roles=[]`로 빠짐 — 예외 없이 "조용히" 실패하는 유형의 버그 |
| 해결 | `import { jwtDecode } from "jwt-decode"` named import로 수정(axios 인터셉터·로그인 사가 양쪽 모두) |
| 성과 | 역할별 메뉴/화면 분기가 정상 동작, 이후 라이브러리 버전업 시 named/default export 변경 여부를 먼저 확인하는 체크리스트가 생김 |
| 학습 | 외부 라이브러리의 메이저 버전업이 "에러 없이 조용히" 동작을 바꿔버리는 경우가, 즉시 터지는 버그보다 오히려 찾기 어렵다는 것을 체감 |

### 사례 4. 동일 컬럼 재판별 시 UNIQUE 제약 충돌(`ORA-00001`)

| 구&nbsp;분 | 내용 |
|:---:|---|
| 문제 | `DOMAIN_PREDICTION`은 컬럼당 도메인코드 1건만 허용(`UQ_DOMAIN_PREDICTION`)하도록 설계했는데, 같은 컬럼을 두 번째로 판별하면 INSERT가 제약 위반으로 실패 |
| 원인 | `DOMAIN_PREDICTION`을 "판별 이력"이 아니라 "현재 Top-N 스냅샷"으로 설계했다는 걸 감안하지 않고, 재판별 결과를 기존 행에 그대로 추가(insert)하려 했음 |
| 해결 | 재판별 시 해당 컬럼의 기존 `DOMAIN_PREDICTION`을 전부 삭제(`deleteByAnlCol_ColumnId` + `flush()`)한 뒤 새 Top-N 결과를 insert하는 "스냅샷 교체" 방식으로 변경 |
| 성과 | 같은 컬럼을 몇 번을 재판별해도 제약 위반 없이 항상 최신 Top-N만 남도록 정리 |
| 학습 | 테이블을 "이력 테이블"로 설계했는지 "현재 상태 스냅샷"으로 설계했는지에 따라 갱신 전략(insert-only vs delete-then-insert)이 완전히 달라진다는 것, 설계 의도를 코드 주석으로 명확히 남겨야 다음에 헷갈리지 않는다는 것을 체득 |

---

## 8. 자체 평가

| 구&nbsp;&nbsp;분 | 내용 |
|:---:|---|
| ✅ **잘한 점** | 원본의 상시 기동 gRPC 서버를 OpenAI Chat Completions 규격 `RestClient` 호출로 대체하면서, JSON 강제 응답 + 방어적 필터링으로 AI hallucination을 억제<br>Redis 캐시로 재판별 응답을 최적화하고, `CACHE_HIT_YN`/`RESPONSE_MS`를 남겨 개선 효과를 정량적으로 추적할 수 있게 설계<br>JWT + Redis + OAuth2.0(Google·Kakao·Naver) 인증과 역할 3단계 `@PreAuthorize`를 실제로 붙여 완결된 인증/인가 흐름을 구현<br>Quartz + 쉘 실행을 Spring 내장 스케줄러로 대체해 컨테이너 이식성을 확보하고, 템플릿 메서드 패턴으로 `CALL_ERROR`/`INTERNAL_ERROR`를 구분 |
| ⚠️ **아쉬운 점** | `mybatis-spring-boot-starter` 의존성은 추가했지만 실제 Mapper 구현은 하지 않아 하이브리드 구조가 아니라 전량 JPA로만 구현됨<br>로그아웃 시 Refresh Token 회수는 구현했지만, Access Token 자체의 즉시 무효화(jti 블랙리스트)는 구현하지 않아 로그아웃 후에도 남은 TTL(최대 30분) 동안은 유효함<br>CoolSMS(SMS 알림)와 다국어(i18n)는 초기 설계·의존성에는 있었지만 실제 구현 범위에서는 의도적으로 제외<br>AI가 돌려주는 "확률" 값은 LLM에게 직접 물어본 자기평가 수치로, 별도로 학습된 분류 모델의 신뢰도와는 성격이 다름 |
| 💡 **배운 점** | "지연 로딩 예외가 랜덤하게 보인다"는 증상은 실제로는 트랜잭션 경계 문제일 가능성이 높다는 것<br>외부 라이브러리의 메이저 버전업이 예외 없이 "조용히" 동작을 바꿔버리는 경우가 찾기 더 어렵다는 것<br>테이블을 이력으로 설계했는지 스냅샷으로 설계했는지에 따라 갱신 전략이 완전히 달라진다는 것<br>계획(설계서)과 실제 구현 사이에는 항상 차이가 생기고, 그 차이(왜 뺐는지·왜 못했는지)를 정직하게 남기는 편이 오히려 설득력이 있다는 것 |
| 🚀 **다음 개선 방향** | 승인 대기열 통계 등 동적 조회에 MyBatis를 실제로 붙여 JPA+MyBatis 하이브리드 구조를 완성하기<br>Access Token jti를 Redis 블랙리스트에 등록해 로그아웃 즉시 무효화 구현하기<br>`AI_SUGGESTED_YN` 채택률을 집계하는 대시보드 화면 추가하기<br>AI 응답 실패(429/5xx) 시 재시도(backoff) 정책 적용하기 |

---

## 9. 회고

> 실무에서 다뤘던 도메인을 "내 손으로 처음부터 다시 설계하면 어떻게 짤까"를 검증해 본 프로젝트였습니다. 원본은 사내망에 상시 기동된 gRPC 서버와 Quartz 쉘 스크립트에 의존하고 있었는데, 그 구조를 그대로 베끼지 않고 "왜 그런 구조였는지"와 "지금 스택으로는 왜 다르게 짜야 하는지"를 설계서에 먼저 남긴 다음 코드를 짰습니다.
>
> 가장 크게 배운 건 트러블슈팅 사례들이었습니다. PDF 다운로드가 "가끔" 실패하는 버그, 관리자로 로그인해도 일반 사용자 화면만 보이는 버그처럼 눈에 잘 안 보이는 문제일수록 원인이 한 단계 더 안쪽(트랜잭션 경계, 라이브러리 export 방식)에 있었습니다. 그리고 설계서에 적었던 계획(MyBatis 하이브리드, SMS 알림, JWT 블랙리스트, 다국어)을 실제로 구현하다 다 못한 부분도 있었는데, 그걸 숨기지 않고 "왜 뺐는지"를 주석과 이 문서에 그대로 남기는 게 오히려 이 프로젝트를 더 정직하고 설명하기 쉬운 결과물로 만들어준다는 걸 느꼈습니다.

---

## 10. 주요 설정 파일

- `back/build.gradle` — Spring Boot 4.0.8(Gradle), Java 17 toolchain, `spring-boot-starter-web/data-jpa/validation`, `mybatis-spring-boot-starter` 3.0.3(미사용), `spring-boot-starter-security/data-redis/oauth2-client/restclient/mail`, JWT(`jjwt-api/impl/jackson` 0.11.5), `springdoc-openapi-starter-webmvc-ui` 3.1.0, `ojdbc11`(Oracle), `spring-dotenv` 3.0.0, `gson` 2.11.0, `pdfbox` 3.0.5
- `back/src/main/resources/application.yml` / `application-oauth.yml` — Oracle 18c XE 데이터소스, Redis 연결, JWT 발급자·만료시간(Access 30분/Refresh 14일), Google·Kakao·Naver OAuth2 클라이언트, 외부 AI API(`ai.api-base-url`/`ai.api-key`/`ai.model-name`) 등을 환경변수로 분리
- `front/package.json` / `.babelrc` / `.eslintrc` — Next.js 12, React 17, Redux Toolkit + redux-saga, antd 4.8, styled-components(ssr, displayName), ESLint(airbnb + babel-eslint, ecmaVersion 2020)
- `front/api/axios.js` — JWT 인터셉터: 요청 시 Redux의 accessToken을 헤더에 주입, 401 응답 시 `/auth/reissue`로 자동 재발급(동시 요청 큐잉 처리)
- `docs/DDL_DML_DCL_Oracle18cXE.sql` — 테이블스페이스/스키마 생성(DCL) → 테이블/제약조건(DDL) → 업무키 자동채번 트리거 → 공통코드/역할/도메인 후보/데모 데이터(DML)

---

## 📄 라이선스 / 고지

본 프로젝트는 개인 포트폴리오 목적의 재구현 프로젝트입니다. 원본 회사의 소스코드는 어떠한 형태로도 포함되어 있지 않으며, 테이블명·클래스명·비즈니스 로직은 모두 새로 설계했습니다. 고객사명 등 식별 정보는 포함하지 않습니다.
