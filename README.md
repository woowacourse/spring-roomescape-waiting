# 방탈출 예약 시스템

## 기능 요구 사항

### 예약 관리

- 예약을 생성할 수 있다.
- 관리자는 전체 예약 목록을 조회할 수 있다.
- 관리자는 날짜와 테마 조건으로 예약 목록을 필터링할 수 있다.
- 관리자는 예약을 삭제할 수 있다.
- 사용자는 본인의 예약 목록을 조회할 수 있다.
- 사용자는 본인의 예약 날짜/시간을 변경할 수 있다.
- 사용자는 본인의 예약을 취소할 수 있다.
- 이미 예약된 슬롯(날짜+시간+테마)에는 중복 예약을 할 수 없다.
- 과거 날짜에는 예약을 생성할 수 없다.
- 예약이 취소되거나 삭제되면 같은 슬롯의 1순위 대기가 예약으로 자동 전환된다.

### 예약 시간 관리

- 예약 시간을 추가할 수 있다.
- 예약 시간 전체 목록을 조회할 수 있다.
- 특정 날짜와 테마에 대해 예약 가능한 시간만 조회할 수 있다.
- 예약 시간을 삭제할 수 있다.
- 예약이 존재하는 시간은 삭제할 수 없다.

### 테마 관리

- 관리자는 테마를 추가할 수 있다.
- 전체 테마 목록을 조회할 수 있다.
- 기간과 개수 조건으로 인기 테마를 조회할 수 있다.
- 관리자는 테마를 삭제할 수 있다.
- 예약이 존재하는 테마는 삭제할 수 없다.

### 예약 대기

- 이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에 대기를 신청할 수 있다.
- 예약이 없는 슬롯에는 대기를 신청할 수 없다.
- 같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다.
- 같은 사용자가 같은 슬롯에 중복 대기를 신청할 수 없다.
- 사용자는 본인의 대기를 취소할 수 있다.
- 사용자의 예약과 대기가 함께 조회된다.
- 대기 항목에는 본인의 대기 순번이 함께 표시된다.
- 대기가 예약으로 전환되거나 취소되면 같은 슬롯의 남은 대기 순번이 재정렬된다.
- 대기 신청, 예약 취소, 대기 승격은 트랜잭션과 DB 제약으로 데이터 일관성을 유지한다.

---

## 실행 방법

### 백엔드

```bash
./gradlew bootRun
```

- 기본 주소: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`

### 프론트엔드

```bash
node FE/dev-server.mjs
```

- 기본 주소: `http://localhost:3000`
- 사용자 예약 페이지: `http://localhost:3000/user/index.html`
- 사용자 마이페이지: `http://localhost:3000/user/mypage.html`
- 관리자 페이지: `http://localhost:3000/admin/index.html`
- API 요청은 기본적으로 `http://localhost:8080`으로 프록시된다.
- 다른 백엔드 주소를 사용하려면 `BE_ORIGIN=http://localhost:8081 node FE/dev-server.mjs`처럼 실행한다.

---

## DB 스키마

```sql
-- 예약 시간
CREATE TABLE IF NOT EXISTS reservation_time (
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id)
);

-- 테마
CREATE TABLE IF NOT EXISTS theme (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- 예약
CREATE TABLE IF NOT EXISTS reservation (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    date       DATE         NOT NULL,
    time_id    BIGINT       NOT NULL,
    theme_id   BIGINT       NOT NULL,
    created_at DATETIME     NOT NULL,
    UNIQUE(date, time_id, theme_id),
    PRIMARY KEY (id),
    FOREIGN KEY (time_id)  REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);

-- 예약 대기
CREATE TABLE IF NOT EXISTS waiting (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    date       DATE         NOT NULL,
    time_id    BIGINT       NOT NULL,
    theme_id   BIGINT       NOT NULL,
    created_at DATETIME     NOT NULL,
    UNIQUE(name, date, time_id, theme_id),
    PRIMARY KEY (id),
    FOREIGN KEY (time_id)  REFERENCES reservation_time (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id)
);
```

---

## API 명세

### 예약

| 메서드  | 경로                         | 설명                       | 권한      |
|--------|------------------------------|--------------------------|---------|
| POST   | /reservations                | 예약 생성                   | -       |
| GET    | /reservations                | 전체 예약 조회               | ADMIN   |
| GET    | /reservations?date=&themeId= | 날짜/테마 필터 예약 조회        | ADMIN   |
| GET    | /reservations/me?name=       | 내 예약+대기 목록 조회          | 사용자   |
| PATCH  | /reservations/me/{id}        | 내 예약 날짜/시간 변경          | 사용자   |
| DELETE | /reservations/me/{id}?name=  | 내 예약 취소                  | 사용자   |
| DELETE | /reservations/{id}           | 예약 삭제                   | ADMIN   |

### 예약 대기

| 메서드  | 경로             | 설명              | 권한    |
|--------|----------------|-----------------|-------|
| POST   | /waiting       | 대기 신청           | 사용자 |
| DELETE | /waiting/me/{id}?name= | 내 대기 취소   | 사용자 |

### 예약 시간

| 메서드  | 경로                              | 설명                   | 권한    |
|--------|----------------------------------|----------------------|-------|
| POST   | /times                           | 시간 추가               | -     |
| GET    | /times                           | 전체 시간 조회            | -     |
| GET    | /times?date=&themeId=            | 예약 가능 시간 조회         | -     |
| DELETE | /times/{id}                      | 시간 삭제               | -     |

### 테마

| 메서드  | 경로                                         | 설명                   | 권한    |
|--------|---------------------------------------------|----------------------|-------|
| POST   | /themes                                     | 테마 추가               | ADMIN |
| GET    | /themes                                     | 전체 테마 조회            | -     |
| GET    | /themes?sortBy=popular&from=&to=&limit=     | 인기 테마 조회            | -     |
| DELETE | /themes/{id}                                | 테마 삭제               | ADMIN |

---

## 주요 응답 형태

예약 응답은 시간과 테마를 중첩 객체가 아니라 화면 표시용 단순 값으로 반환한다.

```json
{
  "id": 1,
  "name": "브라운",
  "date": "2026-05-05",
  "time": "10:00:00",
  "theme": "테마A"
}
```

대기 응답은 예약 응답에 `rank`를 추가한다.

```json
{
  "id": 2,
  "name": "브라운",
  "date": "2026-05-05",
  "time": "10:00:00",
  "theme": "테마A",
  "rank": 1
}
```

내 예약 조회는 예약과 대기를 분리해서 반환한다.

```json
{
  "reservations": [],
  "waitings": []
}
```

---

## 테스트 전략

각 기능 결정마다 "왜 이 방식으로 테스트했는가"를 PR 본문에 한두 줄로 기록한다.

- **Domain 규칙** — 외부 의존 없이 순수 단위 테스트
- **Validator** — Fake Repository를 활용한 단위 테스트
- **Service** — Fake Repository를 활용한 단위 테스트 (통합 테스트로 대체하지 않음)
- **Controller** — MockMvc를 활용한 슬라이스 테스트
- **Repository** — 실제 DB(H2)를 사용하는 통합 테스트
- **동시성 테스트** — 실제 Bean과 DB를 사용하는 통합 테스트
- **인수 테스트** — 전체 흐름을 검증하는 E2E 테스트 (RestAssured)
