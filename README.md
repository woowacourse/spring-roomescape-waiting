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

### 사이클2 기능 구현 리스트

- [ ] `reservation_slot` 테이블을 추가하고 슬랏 조합이 유니크 하도록 제약 걸기
- [ ] `reservation` 테이블이 기존 3개 FK 대신 `slot_id`만 참조하도록 스키마 변경
- [ ] `reservation_waiting` 테이블이 `reservation_id` 대신 `slot_id`를 참조하도록 스키마 변경
- [ ] 슬롯 조회&생성하는 Repository를 추가
- [ ] 예약 생성 흐름에서 날짜, 테마, 시간으로 `Slot`을 찾거나 만든 뒤 예약이 슬롯을 참조하도록 변경
- [ ] 대기 생성 흐름에서 예약 row가 아니라 `Slot`을 기준으로 대기 가능 여부와 중복 대기를 판단하도록 변경한다.
- [ ] 예약 가능 시간 조회가 슬롯과 예약 존재 여부를 기준으로 예약 가능/대기 가능 상태를 계산하도록 변경한다.
- [ ] 내 예약/대기 히스토리 조회 SQL이 '예약', '대기' 모두 `Slot`을 통해 날짜, 테마, 시간을 조회하도록 변경한다.
- [ ] 예약 취소 시 해당 슬롯의 첫 번째 대기를 찾는다
  - [ ] 대기가 없으면 예약 삭제 실행
  - [ ] 첫 번째 대기가 있으면 기존 예약을 삭제하고 같은 슬롯에 새 예약을 생성한 뒤 승인된 대기 row를 삭제하도록 한다.
  - [ ] 예약 취소와 대기 전환을 하나의 트랜잭션으로 묶는다.
- [ ] 슬롯 단위에 비관적 락을 적용하여 동시성 처리, 취소/전환 흐름에서 해당 슬롯 row를 잠근 뒤 작업하도록 변경한다.
- [ ] Repository 테스트 fixture를 슬롯 기반 데이터 생성 방식으로 변경
- [ ] 서비스 테스트와 API 테스트에서 예약/대기 생성, 취소, 히스토리 조회 기대값을 슬롯 구조에 맞게 수정
- [ ] 동시 예약 취소, 동시 대기 신청, 동시 대기 전환 상황을 검증하는 동시성 통합 테스트를 추가


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
