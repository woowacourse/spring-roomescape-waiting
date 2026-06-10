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

- [X] `slot` 테이블을 추가하고 날짜, 테마, 시간 조합이 유니크하도록 제약을 건다.
- [X] `reservation` 테이블이 기존 `date`, `theme_id`, `time_id` 대신 `slot_id`만 참조하도록 스키마를 변경한다.
- [X] `reservation_waiting` 테이블이 `reservation_id` 대신 `slot_id`를 참조하도록 스키마를 변경한다.
- [X] `ReservationSlot`을 식별자를 가진 도메인 객체로 정리한다.
- [X] 슬롯을 조회하고 관리자/초기 데이터에서 생성할 수 있는 Repository를 추가한다.
- [X] 예약 Repository가 `slot_id` 기준으로 예약을 저장, 조회, 삭제하도록 변경한다.
- [X] 대기 Repository가 `slot_id` 기준으로 대기를 저장, 조회, 삭제하도록 변경한다.
- [X] 예약 생성 흐름에서 날짜, 테마, 시간으로 기존 `Slot`을 조회하고, 존재하는 슬롯만 예약이 참조하도록 변경한다.
- [X] Repository는 DB 무결성 예외를 저장소 예외로 감싸고, Service가 유스케이스 문맥에 맞는 예외로 변환하도록 변경한다.
- [X] 예약 생성 시 `reservation.slot_id` UNIQUE 제약 위반을 예약 불가능 충돌 응답으로 변환한다.
- [X] 대기 생성 흐름에서 예약 row가 아니라 `Slot`을 기준으로 대기 가능 여부와 중복 대기를 판단하도록 변경한다.
- [X] 예약 가능 시간 조회가 슬롯과 예약 존재 여부를 기준으로 예약 가능/대기 가능 상태를 계산하도록 변경한다.
- [X] 내 예약/대기 히스토리 조회 SQL이 예약과 대기 모두 `Slot`을 통해 날짜, 테마, 시간을 조회하도록 변경한다.
- [X] 예약 취소 시 해당 슬롯의 첫 번째 대기를 찾는다.
  - [X] 대기가 없으면 예약 삭제 실행
  - [X] 첫 번째 대기가 있으면 기존 예약을 삭제하고 같은 슬롯에 새 예약을 생성한 뒤 승인된 대기 row를 삭제한다.
  - [X] 예약 취소와 대기 전환을 하나의 트랜잭션으로 묶는다.
- [X] 현재 기능 범위에서는 예약 취소 후 대기 전환 흐름에 비관적 락을 도입하지 않기로 결정한다.
  - [X] 빈번한 동시 예약 생성은 `reservation.slot_id` UNIQUE 제약으로 처리한다.
  - [X] 빈번한 동시 대기 신청은 `reservation_waiting(slot_id, name)` UNIQUE 제약과 조회 시 순번 계산으로 처리한다.
  - [X] 수동 승인, 관리자 강제 승격 등 같은 슬롯 변경 유스케이스가 늘어나면 슬롯 row 기준 비관적 락을 도입한다.
- [X] Repository 테스트 fixture를 슬롯 기반 데이터 생성 방식으로 변경한다.
- [X] 서비스 테스트와 API 테스트에서 예약/대기 생성, 취소, 히스토리 조회 기대값을 슬롯 구조에 맞게 수정한다.
- [X] 동시 예약 생성은 UNIQUE 제약으로 하나만 성공하고 나머지는 충돌 처리되는지 검증한다.
- [X] 동시 대기 신청은 UNIQUE 제약으로 같은 이름의 중복 대기만 충돌 처리되는지 검증한다.

### 사이클1 머지 당시 리뷰 반영 사항 리스트

- [X] `Reservation`의 static 편의 메서드를 제거하고 필요한 객체에게 직접 물어보도록 정리한다.
  - `ReservationSlot` 객체가 있는 흐름에서는 `slot.isPast(...)`로 묻는다. 슬롯 객체 없이 화면에 표시할 날짜와 시간만 있는 흐름에서는 화면 조립 코드에서 `LocalDateTime`으로 조합해 과거 여부를 판단, `Reservation`은 자신이 가진 `slot`에 대해 `reservation.isPast(...)`처럼 묻는 메서드만 남긴다.

- [X] `Reservation` 생성 경로를 줄이고 불변식 검증 위치를 정리한다.
  - 예약은 `ReservationSlot`을 받아 생성하도록 정리, 날짜/테마/시간 조합으로 슬롯을 만드는 책임은 서비스나 테스트 fixture 쪽에서 처리하고, `Reservation`은 이름, 슬롯, 생성 시각을 기준으로 자신의 불변식만 검증

- [X] `ReservationWaitingLine`의 `Map` 사용 이유를 검토하고 표현 계층 요구가 도메인에 들어왔는지 확인한다.
  - 대기 줄은 요청 시각과 대기 ID 기준으로 정렬된 `List<ReservationWaiting>`를 보관하도록 정리, `waitingId -> sequence` 형태의 `Map`은 제거한다. 도메인에서는 `indexOf`, `containsName`, `isEmpty`를 제공하고, 화면 표시용 1-based 순번 계산은 히스토리 조회 조립 객체에서 처리

- [X] `ReservationRepository`가 `Slot` 식별자를 중심으로 상호작용하도록 유지한다.
  - 예약과 대기는 `date/theme/time` 조합이 아니라 `ReservationSlot`을 기준으로 조회, 슬롯 식별자를 중심으로 상호작용하도록 유지

- [X] `ReservationSlot`의 static 메서드를 객체에게 묻는 방식으로 바꿀 수 있는지 검토한다.
  - `ReservationSlot`의 static 상태 판단 메서드는 제거, 슬롯 객체가 있는 흐름에서는 `slot.isPast(...)`로 묻고, 슬롯 객체 없이 화면에 표시할 날짜와 시간만 있는 흐름에서는 화면 조립 코드에서 `LocalDateTime`으로 조합해 과거 여부를 판단


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
