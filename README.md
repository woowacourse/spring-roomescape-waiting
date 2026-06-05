## 기능 구현 목록

### 예약 / 대기

- [x] 같은 슬롯(날짜·시간·테마가 겹치는 예약 단위)에 같은 사용자가 다시 예약하면 거부한다
- [x] 이미 다른 사용자가 예약한 슬롯이면 대기(WAITING)로 등록된다
- [x] 신청한 순서(`enqueued_at`)대로 대기 순번이 부여된다
- [x] '내 예약 목록' 조회 시 예약(CONFIRMED)·대기(WAITING)·취소(CANCELED) 상태를 구분해 보여준다
- [x] 사용자 UI에서 대기 순번을 보여준다

### 취소 / 승급 (이번 사이클)

- [x] 예약 취소는 행을 삭제하지 않고 상태를 CANCELED로 바꾸는 **soft delete**로 처리한다
- [x] 확정 예약이 취소되면 같은 슬롯의 1순위 대기자(가장 먼저 줄 선 사람)를 **자동으로 확정 승급**한다
- [x] 취소·승급은 하나의 트랜잭션으로 원자적으로 처리한다
- [x] 취소·승급으로 남은 대기자의 순번이 조회 시 자동으로 당겨진다
- [x] '내 예약 목록'에서는 본인의 취소 이력도 함께 보여준다 (관리자 목록·인기 테마 집계에서는 취소 건 제외)

### 동시성

- [x] 같은 빈 슬롯에 동시에 예약해도 확정은 정확히 1건만 생성된다
- [x] 같은 예약을 동시에 취소해도 대기자 이중 승급이 발생하지 않는다
- [x] 예약 변경과 신규 예약이 같은 슬롯에 동시에 들어와도 확정은 1건을 유지한다

## 어드민 API

### 예약 관리

| 기능    | Method / URL                      | 요청 본문                                   | 응답                                                                   |
|-------|-----------------------------------|-----------------------------------------|----------------------------------------------------------------------|
| 예약 조회 | `GET /admin/reservations`         | -                                       | `[{id, reserverName, date, time, theme, waitingOrder, status}, ...]` |
| 예약 추가 | `POST /admin/reservations`        | `{reserverName, date, timeId, themeId}` | `{id, reserverName, date, time, theme, waitingOrder, status}`        |
| 예약 취소 | `DELETE /admin/reservations/{id}` | -                                       | `200 OK` (soft delete + 대기자 자동 승급)                                   |

> 예약 취소는 행을 삭제하지 않고 `status`를 `CANCELED`로 바꾼다. 조회(`GET /admin/reservations`)에서는 취소 건이 제외된다.

### 시간 관리

| 기능    | Method / URL               | 요청 본문       | 응답                     |
|-------|----------------------------|-------------|------------------------|
| 시간 조회 | `GET /admin/times`         | -           | `[{id, startAt}, ...]` |
| 시간 추가 | `POST /admin/times`        | `{startAt}` | `{id, startAt}`        |
| 시간 삭제 | `DELETE /admin/times/{id}` | -           | `200 OK`               |

### 테마 관리

| 기능    | Method / URL                | 요청 본문                            | 응답                                          |
|-------|-----------------------------|----------------------------------|---------------------------------------------|
| 테마 조회 | `GET /admin/themes`         | -                                | `[{id, name, description, thumbnail}, ...]` |
| 테마 추가 | `POST /admin/themes`        | `{name, description, thumbnail}` | `{id, name, description, thumbnail}`        |
| 테마 삭제 | `DELETE /admin/themes/{id}` | -                                | `200 OK`                                    |

## 사용자 API

> **사용자 흐름:** 테마를 본다 → 날짜를 고른다 → 그 조건에서 예약 가능한 시간을 본다 → 시간을 골라 예약한다.

| 기능          | Method / URL                                                 | 요청 본문                                   | 응답                                                                   |
|-------------|--------------------------------------------------------------|-----------------------------------------|----------------------------------------------------------------------|
| 테마 목록 조회    | `GET /user/themes`                                           | -                                       | `[{id, name, description, thumbnail}, ...]`                          |
| 예약 가능 시간 조회 | `GET /user/themes/{themeId}/available-times?date=YYYY-MM-DD` | -                                       | `[{id, startAt}, ...]`                                               |
| 사용자 예약 추가   | `POST /user/reservations`                                    | `{reserverName, date, timeId, themeId}` | `{id, reserverName, date, time, theme, waitingOrder, status}`        |
| 본인 예약 조회    | `GET /user/reservations?reserverName={reserverName}`         | -                                       | `[{id, reserverName, date, time, theme, waitingOrder, status}, ...]` |
| 본인 예약 변경    | `PATCH /user/reservations/{id}`                              | `{reserverName, date, timeId}`          | `{id, reserverName, date, time, theme, waitingOrder, status}`        |
| 본인 예약 취소    | `DELETE /user/reservations/{id}?reserverName={reserverName}` | -                                       | `204 No Content` (soft delete + 대기자 자동 승급)                           |
| 인기 테마 조회    | `GET /user/themes/popular`                                   | -                                       | `[{id, name, description, thumbnail, reservationCount}, ...]`        |

- `status`: `CONFIRMED`(확정) / `WAITING`(대기) / `CANCELED`(취소)
- `waitingOrder`: 대기 순번(확정·취소는 0). 본인 예약 조회는 취소 건도 함께 반환한다.
- 인기 테마(`reservationCount`)는 최근 7일 기준이며 취소된 예약은 집계에서 제외한다.

## 설계 메모 (주요 결정)

- **상태 저장 + 단일 테이블**: 예약/대기/취소를 별도 테이블로 나누지 않고 `reservation` 한 테이블의 `status` 컬럼으로 표현한다. 대기 순번은 별도로 저장하지 않고 조회 시 서브쿼리로 계산한다(취소 건 제외).
- **순번 정렬 키 `enqueued_at`**: 대기 순번은 "큐에 들어온 시각"인 `enqueued_at`으로 정한다. 수정 시각(`updated_at`)은 감사용으로만 쓰며 순번 계산에 사용하지 않는다(슬롯 변경 시에만 `enqueued_at`을 갱신해 재입장 처리).
- **soft delete**: 취소는 행 삭제가 아니라 `status = CANCELED`로 보존한다. 조회·중복검사·인기집계에서는 취소 건을 제외하고, '내 예약'에서만 이력으로 노출한다.
- **동시성(비관 락)**: "슬롯당 확정 1건" 같은 조건부 유일성은 단순 UNIQUE로 표현할 수 없어(H2는 부분 유니크 인덱스 미지원), 생성·취소·변경이 같은 `theme` 행을 `SELECT ... FOR UPDATE`로 잠가 슬롯 진입을 직렬화한다.
