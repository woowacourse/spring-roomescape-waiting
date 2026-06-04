# 방탈출 예약 대기 미션

## 기능 명세
- [ ]  예약 취소 시 예약 대기 1번을 예약으로 전환한다. (자동 전환)
    - [ ]  대기 존재 유무 확인
    - [ ]  예약 변경
    - [ ]  대기 삭제
- [ ]  대기가 예약으로 승격(예약 삭제) 시 나머지 대기의 순번이 재정렬 되야한다

## API 명세

### Reservation

| 기능          | Http/url | 요청 | 응답                              |
|-------------| --- | --- |---------------------------------|
| 예약 생성       | `POST /reservations` | `{name, date, themeId, timeId}` | `{id, name, date, theme, time}` |
| 예약 삭제       | `DELETE /reservations/{reservationId}` | - | - |
| 예약 조회       | `GET /reservations` | - | `[{id, name, date, theme, time}, ...]` |
| 예약 가능 시간 조회 | `GET /themes/{themeId}/times/available?date={yyyy-MM-dd}` | - | `[{id, startAt, reservable, waitable}, ...]` |


### Theme

| 기능 | Http/url | 요청 | 응답 |
| --- | --- | --- | --- |
| 관리자 테마 추가 | `POST /admin/themes` | `{name, description, thumbnailUrl}` | `{id, name, description, thumbnailUrl}` |
| 관리자 테마 삭제 | `DELETE /admin/themes/{themeId}` | - | - |
| 관리자 테마 조회 | `GET /admin/themes` | - | `[{id, name, description, thumbnailUrl}, ...]` |
| 인기 테마 조회 | `GET /themes/popular?period={period}&limit={limit}` | - | `[{id, name, description, thumbnailUrl}, ...]` |

### ReservationTime

| 기능 | Http/url | 요청 | 응답 |
| --- | --- | --- | --- |
| 관리자 시간 생성 | `POST /admin/reservation-times` | `{startAt}` | `{id, startAt}` |
| 관리자 시간 삭제 | `DELETE /admin/reservation-times/{timeId}` | - | - |
| 관리자 시간 조회 | `GET /admin/reservation-times` | - | `[{id, startAt}, ...]` |


### ReservationWaiting

| 기능      | Http/url                                    | 요청                                | 응답               |
|---------|---------------------------------------------|-----------------------------------|------------------|
| 예약 대기 생성 | `POST /waitings`                            | `{name, theme_id, date, time_id}` | `201 Creadted`   |
| 예약 대기 삭제 | `DELETE /waitings/{waiting_id}?name={name}` | -                                 | `204 No Content` |

### User Page

| 기능 | Http/url | 요청 파라미터 | 성공 응답 | 실패 응답 |
| --- | --- | --- | --- | --- |
| 내 예약 조회 | `GET /pages/user/reservations?reservationName={name}` | `reservationName` | `200 OK` HTML | 페이지 내 에러 메시지 |
| 내 예약 취소 | `POST /pages/user/reservations/{reservationId}/delete` | `reservationName` | `302 Redirect` | `302 Redirect` + `errorCode` |
| 내 예약 변경 | `POST /pages/user/reservations/{reservationId}/update` | `reservationName`, `date`, `timeId` | `302 Redirect` | `302 Redirect` + `errorCode` |

## DB 설계

대기 기능은 별도 `reservation_waiting` 테이블로 분리한다. 대기 데이터는 예약 데이터를 참조하며, 예약 취소 시 첫 번째 대기자를 기존 예약의 예약자로 승격한 뒤 해당 대기 데이터를 삭제한다.

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
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT       NOT NULL,
    name           VARCHAR(255) NOT NULL,
    requested_at   TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id),
    UNIQUE (reservation_id, name)
);

CREATE INDEX idx_reservation_waiting_sequence
    ON reservation_waiting (reservation_id, requested_at, id);
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

대표 코드 예시:

- `INVALID_INPUT`
- `THEME_NOT_FOUND`
- `RESERVATION_TIME_NOT_FOUND`
- `RESERVATION_NOT_FOUND`
- `MY_RESERVATION_NOT_FOUND`
- `WAITING_NOT_FOUND`
- `RESERVATION_DUPLICATED`
- `RESERVATION_WAITING_DUPLICATED`
- `RESERVATION_TIME_IN_USE`
- `THEME_IN_USE`
- `PAST_RESERVATION_CANNOT_BE_CANCELLED`
- `PAST_RESERVATION_CANNOT_BE_UPDATED`
