## 기능 구현 목록

- [x] 같은 슬롯(날짜, 시간, 테마가 겹치는 예약 가능한 단위를 지칭)에 대해 같은 사용자가 예약을 할 경우 거부한다
- [x] 이미 다른 사용자가 예약한 슬롯에 대해 예약을 할 경우 대기로 넘어간다
- [x] '내 예약 목록' 조회시 예약 상태와 대기 상태를 구분하여 보여준다
- [x] 사용자 UI에서 대기 순번을 보여준다
- [x] 예약 취소 또는 대기 취소한 사용자가 있을 경우 뒷 순번 사용자의 순번이 줄어든다
- [x] 신청한 순서대로 순번이 부여된다

## 어드민 API

### 예약 관리

| 기능    | Method / URL                      | 요청 본문                           | 응답                                                   |
|-------|-----------------------------------|---------------------------------|------------------------------------------------------|
| 예약 조회 | `GET /admin/reservations`         | -                               | `[{id, name, date, time, theme, waitingOrder}, ...]` |
| 예약 추가 | `POST /admin/reservations`        | `{name, date, timeId, themeId}` | `{id, name, date, time, theme, waitingOrder}`        |
| 예약 삭제 | `DELETE /admin/reservations/{id}` | -                               | `200 OK`                                             |

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
>

| 기능          | Method / URL                                                 | 요청 본문                           | 응답                                                            |
|-------------|--------------------------------------------------------------|---------------------------------|---------------------------------------------------------------|
| 테마 목록 조회    | `GET /user/themes`                                           | -                               | `[{id, name, description, thumbnail}, ...]`                   |
| 예약 가능 시간 조회 | `GET /user/themes/{themeId}/available-times?date=YYYY-MM-DD` | -                               | `[{id, startAt}, ...]`                                        |
| 사용자 예약 추가   | `POST /user/reservations`                                    | `{name, date, timeId, themeId}` | `{id, name, date, time, theme}`                               |
| 본인 예약 조회    | `GET /user/reservations?name={name}`                         | -                               | `[{id, name, date, time, theme, waitingOrder}, ...]`          |
| 본인 예약 변경    | `PATCH /user/reservations/{id}`                              | `{name, date, timeId}`          | `{id, name, date, time, theme}`                               |
| 본인 예약 취소    | `DELETE /user/reservations/{id}?name={name}`                 | -                               | `204 No Content`                                              |
| 인기 테마 조회    | `GET /user/themes/popular`                                   | -                               | `[{id, name, description, thumbnail, reservationCount}, ...]` |
