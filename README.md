# 방탈출 예약 관리

## 📋 목차
- [개요](#-개요)
- [실행 방법](#-실행-방법)
- [구현 기능 목록](#-구현-기능-목록)
- [API 명세](#-api-명세)
- [커밋 컨벤션](#-커밋-컨벤션)

---

## 1️⃣ 개요

방탈출 카페 관리자가 전화·현장 예약을 직접 등록·관리할 수 있는 예약 관리 시스템이다.
총 4단계에 걸쳐 메모리 저장 → DB 연동 → 시간 관리 → 계층 분리 순서로 구현한다.

---

## 2️⃣ 실행 방법

### 요구 사항

| 항목        | 버전               |
|-------------|-------------------|
| Java        | 21                |
| Gradle      | 8.x (Wrapper 포함) |
| Spring Boot | 3.4.4             |

### 실행

```bash
./gradlew bootRun
```

### 테스트 실행

```bash
./gradlew test
```

---

## 방탈출 예약 관리 : 구현 기능 목록

### ✅ 1단계: 웹 요청-응답

> 별도의 데이터베이스 없이 메모리(List + AtomicLong)로 예약 상태를 관리한다.

- [x] **예약 조회 API** (`GET /reservations`)
    - [x] 전체 예약 목록을 반환한다.
- [x] **예약 추가 API** (`POST /reservations`)
    - [x] 이름, 날짜, 시간을 입력받아 예약을 생성한다.
    - [x] 생성된 예약의 id(AtomicLong 자동 증가)를 응답에 포함한다.
- [x] **예약 삭제 API** (`DELETE /reservations/{id}`)
    - [x] id에 해당하는 예약을 삭제한다.

### ✅ 2단계: 데이터베이스 연동

> H2 인메모리 데이터베이스로 전환하여 서버 재시작 전까지 데이터를 유지한다.

- [x] **환경 설정**
    - [x] `spring-boot-starter-jdbc`, `h2` 의존성을 추가한다.
    - [x] `application.properties`에 H2 콘솔 및 datasource URL을 설정한다.
- [x] **스키마 정의** (`resources/schema.sql`)
    - [x] `reservation` 테이블을 정의한다.
- [ ] **API 전환**
    - [x] 예약 조회를 JdbcTemplate 기반으로 전환한다.
    - [x] 예약 추가를 JdbcTemplate 기반으로 전환하고, DB가 생성한 id를 응답에 담는다.
    - [x] 예약 삭제를 JdbcTemplate 기반으로 전환한다.
    - [x] 기존의 `List<Reservation>`, `AtomicLong`을 제거한다.

### ✅ 3단계: 시간 관리

> 정해진 시간 슬롯을 관리하고, 예약과 시간을 연결한다.

- [x] **시간 관리 API**
    - [x] 시간 추가 (`POST /times`): `startAt`을 입력받아 시간 슬롯을 생성한다.
    - [x] 시간 조회 (`GET /times`): 전체 시간 슬롯 목록을 반환한다.
    - [x] 시간 삭제 (`DELETE /times/{id}`): id에 해당하는 시간 슬롯을 삭제한다.
- [x] **스키마 추가** (`resources/schema.sql`)
    - [x] `reservation_time` 테이블을 추가한다.
- [x] **예약과 시간 연결**
    - [x] `reservation` 테이블의 `time` 컬럼을 `time_id (FK → reservation_time.id)`로 변경한다.
    - [x] `Reservation` 클래스의 `time` 필드를 `String → ReservationTime` 객체로 변경한다.
    - [x] 예약 추가 요청 본문을 `time → timeId`로 변경한다.
    - [x] 예약 조회 응답의 `time` 필드를 객체(`{id, startAt}`)로 변경한다.
    - [x] 예약 조회 쿼리에 INNER JOIN을 적용한다.

### ✅ 4단계: 계층 분리

> 레이어드 아키텍처로 레이어별 책임에 따라 코드를 분리한다.

| 레이어 | 책임 |
| :--- | :--- |
| Controller | 웹 요청·응답 |
| Service | 비즈니스 플로우 |
| DAO (Repository) | DB 접근 |
| Domain | 비즈니스 규칙 |

- [x] **레이어 분리**
    - [x] `ReservationController`에서 비즈니스 로직과 DB 접근 코드를 분리한다.
    - [x] DB 접근 책임을 DAO(repository)에 위임한다.
    - [x] 비즈니스 플로우 책임을 Service에 위임한다.
    - [x] 비즈니스 규칙 책임을 Domain에 위임한다.
    - [x] `ReservationController`에 `JdbcTemplate` 필드가 남아있지 않아야 한다.
- [x] **Spring Bean 등록**
    - [x] 분리한 클래스를 `@Component` · `@Service` · `@Repository` 등으로 등록한다.

---

## 테마 + 사용자 예약 cycle 1 : 구현 기능 목록

### 관리자 기능
- [x] 테마 정보 추가 기능 (관리자)
  - 테마는 이름, 설명, 썸네일 이미지 URL을 가진다.
  - 모든 테마의 시작 시간과 소요 시간은 동일하다고 가정한다.
- [x] 테마 삭제 기능 (관리자)
- [x] 관리자 예약 조회 기능
  - [x] response에 테마 추가할 것. 


### 사용자 기능
- [x] 테마 조회 기능
- [x] 사용자 테마 시간 조회 기능
- [x] 사용자 예약 조회 기능(수정)
  - 사용자의 예약 목록을 보여준다.
  - 예약 id, 테마 이름, 테마 썸네일 url, 날짜, 시간
- [x] 사용자 예약 기능 수정
  - 예약 기능 request는 이름, 날짜, 시간 id, 테마 id를 포함한다.
  - 같은 날짜·시간이라도 테마가 다르면 각각 예약 가능하다.
- [x] 인기 테마 조회 기능
  - 최근 1주 동안 예약이 많았던 테마 상위 10개를 조회한다.
    - ex) 오늘이 5월 8일이면, 게임 날짜가 5월 1일~5월 7일인 예약을 집계해 인기 순서대로 10개를 응답한다.

---

## 예약 변경·취소 & 에러 처리 cycle 2 : 구현 기능 목록

### 1단계 - 서비스 정책
- [x] 지나간 날짜·시간으로 예약 불가 (날짜가 오늘이면 현재 시각 이전 시간도 거부)
- [x] 유효하지 않은 입력값 거부 (`@Valid` 적용 — 빈 이름, null 날짜 등)
- [x] 이미 지난 예약 취소·변경 불가

### 2단계 - 에러 응답 설계
- [x] 에러 응답 형식 통일: `{"message": "..."}`
- [x] `GlobalExceptionHandler` 보강
  - `MethodArgumentNotValidException` → 400
  - `HttpMessageNotReadableException` (잘못된 JSON/날짜 형식) → 400
  - 존재하지 않는 엔드포인트 → 404
  - 그 외 예외 fallback → 500 (`{"message": "서버 오류가 발생했습니다."}`)

### 3단계 - 예약 변경/취소
- [x] 예약 변경 API (`PATCH /reservations/{id}`) — 날짜·시간 변경
  - 변경 대상이 존재해야 함
  - 변경하려는 날짜+시간+테마에 중복 예약 없어야 함
  - 변경하려는 날짜·시간이 과거이면 거부
- [x] 예약 변경 화면 (`user.html` — 날짜·시간 변경 UI 추가)

## 방탈출 예약 대기 cycle 1 : 구현 기능 목록

### 1단계 - 예약 대기 신청/취소
- [x] **이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에 대기를 신청할 수 있다.**
  - 같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다.
  - 같은 사용자가 같은 슬롯에 중복 대기할 수 없다.
- [x] **사용자는 본인의 대기를 취소할 수 있다.** 

### 2단계 - 내 예약 목록 조회 (상태 구분)
- [x] **이전 미션의 내 예약 목록 조회를 확장한다.**
  - 사용자의 예약과 대기가 상태로 구분되어 함께 표시된다.
  - 대기에는 본인의 대기 순번도 함께 보여준다.
- [x] **api에 맞추어 프론트 페이지를 수정한다.**

## 방탈출 예약 대기 cycle 2 : 구현 기능 목록

### 1단계 - 예약 대기 자동 승인
- [x] **예약 취소 시 첫 번째 대기를 예약으로 자동 전환한다.**
  - 예약이 취소되면 같은 날짜·시간·테마 슬롯의 가장 오래된 대기가 예약으로 승격된다.
  - 승격된 대기는 대기 목록에서 삭제된다.
- [x] **예약 변경 시 기존 슬롯의 첫 번째 대기를 예약으로 자동 전환한다.**
  - 예약자가 날짜·시간을 변경하면 기존 슬롯은 비게 되므로 해당 슬롯의 첫 번째 대기를 예약으로 승격한다.
  - 승격된 대기는 대기 목록에서 삭제된다.
  - 이미 지난 예약은 변경할 수 없다.
- [x] **대기 순번을 재정렬한다.**
  - 대기 순번은 별도 컬럼으로 저장하지 않고 조회 시 `ROW_NUMBER()`로 계산한다.
  - 대기 취소, 예약 취소, 예약 변경으로 대기 목록이 바뀌면 남은 대기의 순번이 다시 계산된다.

### 선택한 승인 방식
- 자동 전환 방식을 선택했다.
  - 사용자가 예약을 취소하거나 변경해 슬롯이 비는 순간 대기 1번이 예약 가능한 상태가 되므로, 별도 관리자 승인 없이 즉시 전환하는 것이 사용자 흐름에 자연스럽다고 판단했다.
  - 수동 승인 UI와 관리자 판단 절차를 추가하지 않아도 대기 상태가 오래 방치되지 않는다.

### 트랜잭션 경계
- 예약 취소와 대기 승격은 하나의 트랜잭션으로 묶는다.
  - 예약 삭제, 첫 번째 대기 예약 저장, 해당 대기 삭제는 같은 슬롯의 상태를 하나의 일관된 결과로 만들어야 한다.
- 예약 변경과 기존 슬롯 대기 승격은 하나의 트랜잭션으로 묶는다.
  - 예약 이동으로 기존 슬롯이 비는 것과 기존 슬롯의 첫 번째 대기가 예약으로 전환되는 것은 함께 성공하거나 함께 실패해야 한다.
- 대기 순번 재정렬은 별도 쓰기 트랜잭션으로 처리하지 않는다.
  - 순번은 저장값이 아니라 조회 시 계산하는 파생값이므로, 대기 목록 변경 후 조회 결과에서 자동으로 재정렬된다.

### 에러 응답 형식

```json
{ "message": "이미 예약된 시간입니다." }
```

### 에러 응답 명세

| 상황 | 상태 코드 | code 예시 |
|------|---------|---------|
| 유효하지 않은 입력값 (빈 이름, null 날짜 등) | `400` | `INVALID_INPUT` |
| 잘못된 JSON / 날짜 형식 | `400` | `INVALID_FORMAT` |
| 존재하지 않는 리소스 (ID) | `404` | `NOT_FOUND` |
| 변경하려는 시간이 이미 예약됨 (서버 상태와 충돌) | `409` | `RESERVATION_CONFLICT` |
| 이미 지난 예약 취소·변경 (비즈니스 로직상 불가) | `422` | `PAST_RESERVATION` |
| 서버 내부 오류 | `500` | `SERVER_ERROR` |

> 지나간 예약 기준: `예약 날짜 + 예약 시간 < 현재 시각 (LocalDateTime)`

### 추가 API 명세

| 기능 | 메서드 / URL | 요청 본문 | 응답 |
|------|------------|---------|------|
| **예약 변경** | `PATCH /reservations/{id}` | `{date, timeId}` | `200 {id, date, themeName, themeDescription, themeThumbnailUrl, time, reservationStatus}` |

---

## 4️⃣ API 명세

### 관리자 API (`/admin`)

| 기능               | 메서드 / URL                     | 요청 본문                                  | 응답                                                                    |
|:-----------------|:-------------------------------|:---------------------------------------|:----------------------------------------------------------------------|
| **관리자 테마 추가**    | `POST /admin/themes`           | `{name, description, thumbnail_url}`   | `201 Location: /themes/{id}`                                          |
| **관리자 테마 삭제**    | `DELETE /admin/themes/{id}`    | —                                      | `204`                                                                 |
| **관리자 예약 조회**    | `GET /admin/reservations`      | —                                      | `200 [{id, name, date, themeName, time}]`                             |
| **관리자 예약 삭제**    | `DELETE /admin/reservations/{id}` | —                                   | `204` (첫 번째 대기가 있으면 자동 예약 전환)                              |

### 공통 API (시간)

| 기능            | 메서드 / URL          | 요청 본문       | 응답                    |
|:--------------|:--------------------|:------------|:----------------------|
| **시간 조회**     | `GET /times`        | —           | `200 [{id, startAt}]` |
| **시간 추가**     | `POST /times`       | `{startAt}` | `201 {id, startAt}`   |
| **시간 삭제**     | `DELETE /times/{id}` | —          | `204`                 |

### 사용자 API

| 기능                  | 메서드 / URL                          | 요청 본문 / 쿼리 파라미터                     | 응답                                                                                                                                |
|:--------------------|:------------------------------------|:---------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------|
| **테마 전체 조회**        | `GET /themes`                       | —                                      | `200 [{id, name, description, thumbnailUrl}]`                                                                                     |
| **인기 테마 조회**        | `GET /themes?condition=popular&size={n}` | —                                 | `200 [{id, name, description, thumbnailUrl}]`                                                                                     |
| **테마별 예약 가능 시간 조회** | `GET /themes/{id}/times?date={date}` | —                                     | `200 [{id, startAt, isAvailable}]`                                                                                                |
| **예약 조회**           | `GET /reservations?username={name}` | —                                      | `200 [{id, date, themeName, themeDescription, themeThumbnailUrl, time, reservationStatus}]`                                       |
| **내 예약 통합 조회**     | `GET /reservations/me?username={name}` | —                                   | `200 [{id, date, themeName, themeDescription, themeThumbnailUrl, time, waitingNumber, reservationStatus}]`                        |
| **예약 추가**           | `POST /reservations`                | `{name, date, timeId, themeId}`        | `201 {id, date, themeName, themeDescription, themeThumbnailUrl, time, reservationStatus}`                                         |
| **예약 변경**           | `PATCH /reservations/{id}`          | `{date, timeId}`                       | `200 {id, date, themeName, themeDescription, themeThumbnailUrl, time, reservationStatus}` (첫 번째 대기가 있으면 자동 예약 전환)                 |
| **예약 삭제**           | `DELETE /reservations/{id}?username={name}` | —                              | `204` (첫 번째 대기가 있으면 자동 예약 전환)                                                                                                     |

### 예약 대기 API 명세

| 기능 | 메서드 / URL | 요청 본문 / 쿼리 파라미터 | 응답 |
|------|------------|----------------------|------|
| **예약 대기 신청** | `POST /reservations/waiting` | `{name, date, timeId, themeId}` | `201 {id, date, themeName, themeDescription, themeThumbnailUrl, time, reservationStatus}` |
| **예약 대기 취소** | `DELETE /reservations/waiting/{id}` | - | `204` |
| **내 예약 대기 조회** | `GET /reservations/waiting?username={name}` | - | `200 [{id, date, themeName, themeDescription, themeThumbnailUrl, time, waitingNumber, reservationStatus}]` |

---

## 5️⃣ 커밋 컨벤션

Following convention : https://gist.github.com/stephenparish/9941e89d80e2bc58a153

```markdown
# basic structure
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footer>

# <type>
feat (feature)
fix (bug fix)
docs (documentation)
style (formatting, missing semi colons, …)
refactor
test (when adding missing tests)
chore (maintain)

# <scope>
console - I/O
domain - 핵심 로직
validation - 유효성검사
test - 테스트코드 추가
```
