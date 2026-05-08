# 방탈출 예약 관리

## 📋 목차
- [개요](#-개요)
- [실행 방법](#-실행-방법)
- [프론트엔드 접근](#-프론트엔드-접근)
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

서버 시작 시 `schema.sql`(테이블 생성)과 `data.sql`(목업 데이터 삽입)이 자동으로 실행된다.

### 테스트 실행

```bash
./gradlew test
```

---

## 3️⃣ 프론트엔드 접근

서버(`./gradlew bootRun`) 실행 후 브라우저에서 아래 URL로 접근한다.

| 페이지 | URL | 설명 |
|:------|:----|:-----|
| **메인 (이름 입력)** | `http://localhost:8080/` | 이름을 입력하면 localStorage에 저장되어 사용자 식별자로 사용된다 |
| **사용자 페이지** | `http://localhost:8080/user.html` | 인기 테마 조회, 예약하기(테마→날짜→시간 선택), 내 예약 조회·삭제 |
| **관리자 페이지** | `http://localhost:8080/admin.html` | 전체 예약 조회·삭제, 테마 추가·삭제, 시간 추가·삭제 |
| **H2 콘솔** | `http://localhost:8080/h2-console` | JDBC URL: `jdbc:h2:mem:database` / 계정: `SA` / 비밀번호: 없음 |

### 사용자 페이지 흐름

```
메인(이름 입력) → 사용자 페이지
                    ├── 🔥 인기 테마   : 최근 7일 예약 기준 상위 5개 표시
                    ├── 🎭 예약하기    : 테마 선택 → 날짜 선택 → 시간 선택(예약 불가 슬롯 비활성) → 확인
                    └── 📋 내 예약    : 본인 예약 목록 조회 및 삭제
```

### 관리자 페이지 흐름

```
관리자 페이지
    ├── 📋 전체 예약  : 모든 사용자 예약 목록 조회 및 삭제
    ├── 🎭 테마 관리  : 테마 추가(생성 시 10:00~22:00 시간 자동 보장) / 삭제
    └── ⏰ 시간 관리  : 시간 추가 / 삭제
```

### 목업 데이터 (서버 시작마다 자동 삽입)

| 항목 | 내용 |
|:----|:----|
| 예약 시간 | 10:00 ~ 22:00 (1시간 단위, 13개) |
| 테마 | 공포의 저택, 우주 정거장, 마법사의 연구실, 탐정 사무소 |
| 예약자 예시 | `김철수`, `이영희`, `박민준`, `최수진` 등 |
| 인기 테마 순위 | 공포의 저택(1위) > 탐정 사무소(2위) > 마법사의 연구실(3위) > 우주 정거장(4위) |

> 테스트 시 `김철수`로 이름 입력하면 기존 예약 3건(미래 날짜)을 바로 확인할 수 있다.

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

## 5️⃣ API 명세

### 관리자 API (`/admin`)

| 기능               | 메서드 / URL                     | 요청 본문                                  | 응답                                                                    |
|:-----------------|:-------------------------------|:---------------------------------------|:----------------------------------------------------------------------|
| **관리자 테마 추가**    | `POST /admin/themes`           | `{name, description, thumbnail_url}`   | `201 Location: /themes/{id}`                                          |
| **관리자 테마 삭제**    | `DELETE /admin/themes/{id}`    | —                                      | `204`                                                                 |
| **관리자 예약 조회**    | `GET /admin/reservations`      | —                                      | `200 [{id, name, date, themeName, time}]`                             |

### 공통 API (시간)

| 기능            | 메서드 / URL          | 요청 본문       | 응답                    |
|:--------------|:--------------------|:------------|:----------------------|
| **시간 조회**     | `GET /times`        | —           | `200 [{id, startAt}]` |
| **시간 추가**     | `POST /times`       | `{startAt}` | `201 {id, startAt}`   |
| **시간 삭제**     | `DELETE /times/{id}` | —          | `204`                 |

### 사용자 API

| 기능                  | 메서드 / URL                          | 요청 본문 / 쿼리 파라미터                     | 응답                                                                       |
|:--------------------|:------------------------------------|:---------------------------------------|:-------------------------------------------------------------------------|
| **테마 전체 조회**        | `GET /themes`                       | —                                      | `200 [{id, name, description, thumbnailUrl}]`                            |
| **인기 테마 조회**        | `GET /themes?condition=popular&size={n}` | —                                 | `200 [{id, name, description, thumbnailUrl}]`                            |
| **테마별 예약 가능 시간 조회** | `GET /themes/{id}/times?date={date}` | —                                     | `200 [{id, startAt, isAvailable}]`                                       |
| **예약 조회**           | `GET /reservations?username={name}` | —                                      | `200 [{id, date, themeName, themeDescription, themeThumbnailUrl, time}]` |
| **예약 추가**           | `POST /reservations`                | `{name, date, timeId, themeId}`        | `201 {id, date, themeName, themeDescription, themeThumbnailUrl, time}`   |
| **예약 삭제**           | `DELETE /reservations/{id}`         | —                                      | `204`                                                                    |

---

안녕하세요 아서.
정성스러운 피드백 주셔서 감사합니다!
코드 리뷰 주신 것에 대해 답변도 해보고, 추가 질문 사항에 대해 커멘트 달아두었습니다.
이번 리뷰도 잘 부탁드립니다:)

### 변경사항 (260507)
- [x] 공통 url을 RequestMapping으로 묶기
- [x] @ResponseBody + @ResponseStatus로 응답 통일
- [x] DB 스키마 변경에 따른 수정
- 날짜 : VARCHAR → DATE, 시간 : VARCHAR → TIME, 썸네일 url : VARCHAR → TEXT
- [x] DAO 데이터 저장과 조회 책임 분리
- save() 메서드에서 조회까지 하고 있어서, 책임을 분리함
- [x] DAO 불필요한 try-catch 삭제
- [x] 도메인 null 검사 추가
- [x] 유명 테마 조회 기능 수정

**[추가 질문 🙋🏻‍♀️]**

1. 도메인에서 id 필드
지금 domain 폴더의 Reservation, ReservationTime, Theme이 모두 id 필드를 가지고 있습니다.
DB로 인해 추가된 속성이기 때문에, id 필드를 가진 순간 이것은 엔티티에 가까워진다고 생각합니다.
순수한 도메인을 유지하려면 id 필드를 삭제해야하는데, 또 한편으로는 스프링을 사용하는데 순수한 도메인을 유지해야하는지에 대해 고민이 됩니다.

지금 reservation, ReservationTime, Theme는 도메인인가요? 엔티티인가요?
아서는 순수한 도메인과 엔티티를 나누는 기준이 뭐라고 생각하시는지, 그리고 스프링에서 도메인과 엔티티를 어떻게 사용하는 것이 좋을지 궁금합니다.
   
2. DAO애서 DTO 반환
아래의 커밋에 추가 질문 달아두었습니다!
   https://github.com/woowacourse/spring-roomescape-member/pull/342#discussion_r3201650542

3. 날짜 라이브러리 테스트, 서비스 테스트, dto에서 도메인에 대해 아는 것 등
   아래의 커밋에 추가 질문 달아두었습니다!
https://github.com/woowacourse/spring-roomescape-member/pull/342#discussion_r3203715674

4. null 검증
아래의 커밋에 추가 질문 달아두었습니다!
   https://github.com/woowacourse/spring-roomescape-member/pull/342#discussion_r3203715441
5. 관리자/사용자 서비스 분리
   아래의 커밋에 추가 질문 달아두었습니다!
https://github.com/woowacourse/spring-roomescape-member/pull/342#discussion_r3203828943
