# 방탈출 예약 대기 미션

## 기능 요구 사항

- [x] 예약이 이미 존재하는 날짜, 테마, 시간에 대해 예약 대기를 신청할 수 있다.
- [x] 예약이 가능한 시간에는 대기 신청이 아니라 기존 예약 생성 흐름을 사용한다.
- [x] 예약 가능 시간은 예약 신청, 예약 불가능 시간은 대기 신청이 가능하도록 사용자 화면을 구성한다.
- [x] 대기 신청은 신청자 이름과 대기 요청 시간을 기록한다.
- [x] 대기 신청은 기존 예약을 참조하는 별도 대기 테이블에 저장한다.
- [x] 같은 예약에 대해 이미 내 이름으로 대기가 있으면 다시 대기할 수 없다.
- [x] 같은 예약에 여러 명이 대기하면 요청 시간이 빠른 순서대로 순번을 부여한다.
- [x] 완전히 같은 시간에 대기 신청이 들어와도 ID 순서로 순번이 일관되게 결정된다.
- [x] 이름을 기준으로 내 예약 내역과 대기 내역을 함께 조회할 수 있다.
- [x] 내 대기 순번을 조회할 수 있다.
- [x] 중간 순번의 대기가 취소되면 뒤 순번이 당겨져 조회된다.
- [x] 대기 상태의 시간은 사용자 화면에서 대기 취소가 가능하다.
- [x] 대기 취소 후 해당 시간은 다시 대기 신청 가능한 예약 불가능 상태로 표시된다.
 

### 사이클2 작업
- ~~[ ] 예약자가 예약을 취소할 때 해당 예약의 대기자가 없으면 예약을 삭제한다.~~
- ~~[ ] 예약자가 예약을 취소할 때 해당 예약의 대기자가 있으면 첫 번째 대기자를 예약자로 승격한다.~~
- ~~[ ] 첫 번째 대기자가 예약자로 승격되면 해당 대기 데이터는 삭제된다.~~
- ~~[ ] 예약 취소와 대기자 승격은 하나의 트랜잭션으로 처리한다.~~
- ~~[ ] 대기 신청, 중복 대기 방지, 순번 산정 과정에서 동시성 문제가 발생하지 않도록 제어한다.~~
- ~~[ ] 같은 사람이 더블 클릭 등으로 동시에 대기 신청해도 중복 대기가 생성되지 않는다.~~


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

대기 기능은 별도 `reservation_waiting` 테이블로 분리한다. 대기 데이터는 예약 데이터를 참조한다.
아직 예약 취소 시 대기자 승격 정책을 적용하지 않으므로, 대기자가 있는 예약은 변경하거나 삭제할 수 없게 막는다.

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
- `RESERVATION_HAS_WAITINGS`
- `RESERVATION_WAITING_DUPLICATED`
- `RESERVATION_TIME_IN_USE`
- `THEME_IN_USE`
- `PAST_RESERVATION_CANNOT_BE_CANCELLED`
- `PAST_RESERVATION_CANNOT_BE_UPDATED`
