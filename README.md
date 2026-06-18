# JPA 방탈출 예약 대기

## 기능 명세

1단계
- [X] 매핑 변환
  - [X] Theme 매핑 변환
  - [X] ReservationTime 매핑 변환
- [ ] 연관관계 매핑
  - [X] Reservation 매핑
  - [ ] ReservationWaiting 매핑

## API 명세

### Reservation

| 기능 | Method / URL | 요청 | 응답 |
|---|---|---|---|
| 예약 생성 | `POST /reservations` | body `{name, date, themeId, timeId}` | `201` `{id, name, date, theme, time}` |
| 예약 취소 | `DELETE /reservations/{id}?name={name}` | query `name` | `204` |
| 예약 목록 조회 | `GET /reservations` | - | `200` `[{id, name, date, theme, time}, ...]` |
| 예약 가능 시간 조회 | `GET /themes/{themeId}/times/available?date={yyyy-MM-dd}` | - | `200` `[{id, startAt}, ...]` |

### Theme

| 기능 | Method / URL | 요청 | 응답 |
|---|---|---|---|
| 테마 추가 | `POST /admin/themes` | body `{name, description, thumbnailUrl}` | `201` `{id, name, description, thumbnailUrl}` |
| 테마 삭제 | `DELETE /admin/themes/{themeId}` | - | `204` |
| 테마 목록 조회 | `GET /admin/themes` | - | `200` `[{id, name, description, thumbnailUrl}, ...]` |
| 인기 테마 조회 | `GET /themes/popular?period={period}&limit={limit}` | - | `200` `[{id, name, description, thumbnailUrl}, ...]` |

### ReservationTime

| 기능 | Method / URL | 요청 | 응답 |
|---|---|---|---|
| 예약 시간 생성 | `POST /admin/reservation-times` | body `{startAt}` | `201` `{id, startAt}` |
| 예약 시간 삭제 | `DELETE /admin/reservation-times/{timeId}` | - | `204` |
| 예약 시간 목록 조회 | `GET /admin/reservation-times` | - | `200` `[{id, startAt}, ...]` |

### ReservationWaiting

| 기능 | Method / URL | 요청 | 응답 |
|---|---|---|---|
| 예약 대기 생성 | `POST /waitings` | body `{name, date, themeId, timeId}` | `201` `{id, name, date, themeId, timeId, requestedAt}` |
| 예약 대기 취소 | `DELETE /waitings/{waitingId}?name={name}` | query `name` | `204` |

### MyHistory

| 기능 | Method / URL | 요청 | 응답 |
|---|---|---|---|
| 내 예약·대기 이력 조회 | `GET /historys/{name}` | - | `200` `[{id, type, date, theme, time, status}, ...]` |

### User Page

| 기능 | Method / URL | 요청 파라미터 | 성공 응답 | 실패 응답 |
|---|---|---|---|---|
| 예약 페이지 조회 | `GET /pages/user/reservations` | `reservationName`, `themeId`(선택), `date`(선택) | `200` HTML | - |
| 예약 생성 | `POST /pages/user/reservations` | `name`, `date`, `themeId`, `timeId` | `302 Redirect` | `302 Redirect` + `errorCode` |
| 예약 대기 생성 | `POST /pages/user/reservations/waitings` | `name`, `date`, `themeId`, `timeId` | `302 Redirect` | `302 Redirect` + `errorCode` |
| 예약 대기 취소 | `POST /pages/user/reservations/waitings/{id}/delete` | `reservationName`, `themeId`, `date` | `302 Redirect` | `302 Redirect` + `errorCode` |
| 예약 취소 | `POST /pages/user/reservations/{id}/delete` | `reservationName` | `302 Redirect` | `302 Redirect` + `errorCode` |
| 예약 변경 | `POST /pages/user/reservations/{id}/update` | `reservationName`, `date`, `timeId` | `302 Redirect` | `302 Redirect` + `errorCode` |

## DB 설계

`reservation_waiting`은 `reservation`을 직접 참조하지 않고 `date`, `theme_id`, `time_id`를 직접 보유한다(반정규화).
예약 취소 시 해당 슬롯의 첫 번째 대기자(`requested_at` 오름차순)를 신규 예약으로 등록하고 대기 데이터를 삭제한다.
이 과정은 단일 트랜잭션으로 처리되어 중간 실패 시 전체가 롤백된다.

### Theme

```sql
CREATE TABLE theme
(
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    description   VARCHAR(255) NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (name)
);
```

### ReservationTime

```sql
CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (start_at)
);
```

### Reservation

```sql
CREATE TABLE reservation
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(255) NOT NULL,
    date     DATE         NOT NULL,
    theme_id BIGINT       NOT NULL,
    time_id  BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    UNIQUE (date, theme_id, time_id)
);
```

### ReservationWaiting

```sql
CREATE TABLE reservation_waiting
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    date         DATE         NOT NULL,
    theme_id     BIGINT       NOT NULL,
    time_id      BIGINT       NOT NULL,
    name         VARCHAR(255) NOT NULL,
    requested_at DATETIME     NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (theme_id) REFERENCES theme (id),
    FOREIGN KEY (time_id) REFERENCES reservation_time (id),
    UNIQUE (date, theme_id, time_id, name)
);
```

## 에러 응답 구조

응답 본문은 다음 형식을 사용한다.

```json
{
  "code": "RESERVATION_DUPLICATED",
  "status": 409,
  "message": "동일한 시기에 예약을 할 수 없습니다."
}
```

상태 코드 규칙:

- `400 Bad Request`: 유효하지 않은 입력값, 잘못된 날짜/시간 형식, 필수값 누락
- `404 Not Found`: 존재하지 않는 테마, 예약 시간, 예약, 내 예약, 내 대기
- `409 Conflict`: 중복 예약, 중복 대기, 예약 중인 시간/테마 삭제, 이미 지난 예약 변경/취소

에러 코드 목록:

| 코드 | 상태 | 설명 |
|---|---|---|
| `INVALID_INPUT` | 400 | 유효하지 않은 입력값 |
| `RESERVATION_DATE_TIME_IN_PAST` | 400 | 과거 날짜/시간으로 예약 불가 |
| `RESERVATION_NOT_FOUND` | 404 | 예약을 찾을 수 없음 |
| `MY_RESERVATION_NOT_FOUND` | 404 | 본인 예약을 찾을 수 없음 |
| `RESERVATION_WAITING_NOT_FOUND` | 404 | 예약 대기를 찾을 수 없음 |
| `RESERVATION_TIME_NOT_FOUND` | 404 | 예약 시간을 찾을 수 없음 |
| `THEME_NOT_FOUND` | 404 | 테마를 찾을 수 없음 |
| `RESERVATION_DUPLICATED` | 409 | 중복 예약 |
| `RESERVATION_WAITING_DUPLICATED` | 409 | 중복 대기 또는 예약자가 대기 신청 |
| `RESERVATION_TIME_DUPLICATED` | 409 | 중복 예약 시간 |
| `THEME_NAME_DUPLICATED` | 409 | 중복 테마 이름 |
| `RESERVATION_TIME_IN_USE` | 409 | 예약이 존재하는 시간 삭제 시도 |
| `THEME_IN_USE` | 409 | 예약이 존재하는 테마 삭제 시도 |
| `PAST_RESERVATION_CANNOT_BE_CANCELLED` | 409 | 이미 지난 예약 취소 불가 |
| `PAST_RESERVATION_CANNOT_BE_UPDATED` | 409 | 이미 지난 예약 변경 불가 |
