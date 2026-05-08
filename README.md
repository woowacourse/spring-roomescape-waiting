# 방탈출 예약 시스템

## 기능 목록

### 1단계 - 테마 도메인 추가
- [x] 테마 추가 (관리자)
- [x] 테마 목록 조회 (관리자)
- [x] 테마 삭제 (관리자)
- [x] 예약에 테마 정보 포함

### 2단계 - 사용자 예약
- [x] 날짜 + 테마 기준 예약 가능한 시간 조회
- [x] 예약 생성 (사용자)
- [x] 동일 날짜 + 시간 + 테마 중복 예약 방지

### 3단계 - 인기 테마 조회
- [x] 최근 1주일 예약 수 기준 인기 테마 상위 N개 조회

---

## API 명세

### 예약 시간 (Reservation Time)

| 구분 | Method | URL | Request Body | Response |
|------|--------|-----|--------------|----------|
| 사용자 | `GET` | `/times?date={date}&themeId={id}` | - | `200` |
| 관리자 | `GET` | `/admin/times` | - | `200` |
| 관리자 | `POST` | `/admin/times` | `{"startAt": "HH:mm"}` | `201` |
| 관리자 | `DELETE` | `/admin/times/{id}` | - | `204` |

**사용자 응답 예시** `GET /times?date=2026-05-01&themeId=1`
- 해당 날짜 + 테마에 아직 예약이 없는 시간만 반환

```json
[
  { "id": 1, "startAt": "10:00" },
  { "id": 2, "startAt": "14:00" }
]
```

---

### 테마 (Theme)

| 구분 | Method | URL                         | Request Body | Response |
|------|--------|-----------------------------|--------------|----------|
| 사용자 | `GET` | `/themes/`                  | - | `200` |
| 사용자 | `GET` | `/themes/popular?limit={n}` | - | `200` |
| 관리자 | `GET` | `/admin/themes`             | - | `200` |
| 관리자 | `POST` | `/admin/themes`             | `{"name", "thumbnailUrl", "description"}` | `201` |
| 관리자 | `DELETE` | `/admin/themes/{id}`        | - | `204` |

**사용자 응답 예시** `GET /themes?limit=10`
- 오늘 기준 직전 7일(어제 ~ 7일 전) 예약 수 기준 내림차순 정렬

```json
[
  { "id": 1, "name": "공포의 저택", "thumbnailUrl": "https://...", "description": "..." },
  { "id": 2, "name": "우주 탐험대", "thumbnailUrl": "https://...", "description": "..." }
]
```

---

### 예약 (Reservation)

| 구분 | Method | URL | Request Body | Response |
|------|--------|-----|--------------|----------|
| 사용자 | `POST` | `/reservations` | `{"name", "date", "timeId", "themeId"}` | `201` |
| 관리자 | `GET` | `/admin/reservations` | - | `200` |
| 관리자 | `POST` | `/admin/reservations` | `{"name", "date", "timeId", "themeId"}` | `201` |
| 관리자 | `DELETE` | `/admin/reservations/{id}` | - | `204` |

**예약 생성 요청 예시** `POST /reservations`

```json
{
  "name": "홍길동",
  "date": "2026-05-01",
  "timeId": 1,
  "themeId": 2
}
```

**예약 응답 예시**

```json
{
  "id": 1,
  "name": "홍길동",
  "date": "2026-05-01",
  "time": { "id": 1, "startAt": "10:00" },
  "theme": { "id": 2, "name": "우주 탐험대", "thumbnailUrl": "https://...", "description": "..." }
}
```

---

## 미션 중 기록
### API 설계 결정 기록

| 결정 | 이유 |
|------|------|
| 사용자/관리자 엔드포인트 분리 (`/admin/*`) | 같은 리소스라도 접근 주체에 따라 제공 데이터와 권한이 달라짐 |
| 예약 가능 시간 조회를 `/times?date&themeId`로 설계 | `time`이 리소스이고 `date`, `themeId`는 필터 조건이므로 쿼리 파라미터로 처리 |
| 인기 테마 조회를 `/themes?limit`으로 설계 | `theme`이 리소스이고 `limit`은 조회 개수를 제한하는 필터 조건 |
| 예약 생성 응답을 `201 Created`로 | 새 리소스가 생성되는 경우 `200` 대신 `201`이 의미상 정확함 |


### 테스트 작성 시 어려웠던 부분

- RestAssured에서 쿼리 파라미터를 넘기는 방법을 몰라 막혔음
  - `.queryParam("date", "2026-05-03")` 형태로 전달해야 함
  - URL에 직접 `?date=...` 형태로 붙이면 파싱 오류 발생

- 테스트 간 DB 데이터 분리 방법을 몰라 막혔음
  - `@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)` 로 컨텍스트를 매 테스트 전에 재시작
  - `@Sql(scripts = "/testData.sql", executionPhase = BEFORE_TEST_METHOD)` 로 테스트별 데이터 주입
  - 테이블 초기화가 필요한 경우 아래 SQL을 sql 파일 앞에 추가
    ```sql
    DROP TABLE IF EXISTS reservation;
    DROP TABLE IF EXISTS reservation_time;
    DROP TABLE IF EXISTS theme;
    ```

### 막힌 순간

- `@PathVariable`, `@RequestParam` 에 이름을 명시하지 않으면 IntelliJ 컴파일 환경에서 파라미터 이름을 찾지 못해 500 에러 발생
  - 원인: IntelliJ 기본 컴파일러는 `-parameters` 플래그를 적용하지 않음
  - 해결: `@PathVariable("id")`, `@RequestParam("date")` 처럼 명시적으로 이름을 지정
- 플래키 테스트 현상이 발생했는데 인지하지 못하고 있다가, 페어와 코드 싱크가 맞지 않는 줄 알고 원인을 찾아다닌 것 
