# 방탈출 사용자 예약 미션

> PR 본문에 주요 API 설계 결정과 그 이유를 한두 줄씩 남긴다.


## 기능 요구 사항

- [x] 1단계 - 테마 도메인 추가
  - [x] 방탈출 게임의 `테마` 정보를 추가한다.
    - [x] 모든 테마의 시작 시간과 소요 시간은 동일하다고 가정한다.
    - [x] 테마는 이름, 설명, 썸네일 이미지 URL을 가진다.
  - [x] 예약에 테마 정보를 포함하도록 기존 코드를 변경한다.
  - [x] 관리자가 테마를 추가, 삭제할 수 있다.

- 2단계 - 사용자 예약
  - [x] 사용자가 날짜와 테마를 선택하면 예약 가능한 시간 목록이 표시된다.
    - [x] 예약 가능한 시간이란, 관리자가 등록한 시간 중 해당 날짜+테마에 아직 예약이 없는 시간이다.
  - [x] 사용자가 예약 가능한 시간을 선택하여 본인의 이름으로 예약한다.
  - [x] 같은 날짜·시간이라도 테마가 다르면 각각 예약 가능하다.

- 3단계 - 인기 테마 조회
  - [x] 최근 1주 동안 예약이 많았던 테마 상위 10개를 조회한다.
  - [x] 예: 오늘이 5월 8일이면, 게임 날짜가 5월 1일~5월 7일인 예약을 집계해 인기 순서대로 10개를 응답한다.

## API 명세서

`Resrevation`

| 기능          | Http/url                                                         | 요청본문                   | 응답                              |
|-------------|------------------------------------------------------------------|------------------------|---------------------------------|
| 예약 생성       | `POST` `/reservations`                                           | `{name, date, themeId, timeId}` | `{id, name, date, theme, time}`        |
| 예약 삭제       | `DELETE` `/reservations/{reservationId}`                         | -                      | -                               |
| 예약 조회       | `GET` `/reservations`                                            | -                      | `[{id, name, date, theme, time}, ...]` |
| 예약 가능 시간 조회 | `GET` `/themes/{themeId}/times/available?date={yyyy-MM-dd}`      | -                      | `[{id, startAt}, ...]`          |
| 인기 테마 조회    | `GET` `/themes/popular?period={period}&limit={limit}`            | -                      | `[{id, name, description, thumbnailUrl}, ...]` |

`Theme`

| 기능 | Http/url | 요청본문 | 응답 |
|--------|------------------------------------|-----------------------------------------|-----------------------------------------------------|
| 관리자 테마 추가 | `POST` `/admin/themes` | `{name, description, thumbnailUrl}` | `{id, name, description, thumbnailUrl}` |
| 관리자 테마 삭제 | `DELETE` `/admin/themes/{themeId}` | - | - |
| 관리자 테마 조회  | `GET` `/admin/themes` | - | `[{id, name, description, thumbnailUrl}, ...]` |
| 인기 테마 조회 | `GET` `/themes/popular?period={period}&limit={limit}` | - | `[{id, name, description, thumbnailUrl}, ...]` |

`ReservationTime`

| 기능 | Http/url | 요청본문 | 응답 |
|--------------|------------------------------------------|-------------|-----------------------------------------------------|
| 관리자 시간 생성 | `POST` `/admin/reservation-times` | `{startAt}` | `{id, startAt}` |
| 관리자 시간 삭제 | `DELETE` `/admin/reservation-times/{timeId}` | - | - |
| 관리자 시간 조회 | `GET` `/admin/reservation-times` | - | `[{id, startAt}, ...]` |
| 예약 가능 시간 조회 | `GET` `/themes/{themeId}/times/available?date={yyyy-MM-dd}` | - | `[{id, startAt}, ...]` |

## 테이블 설계

`Reservation`
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

`Reservation_time`
```sql
CREATE TABLE reservation_time
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    start_at TIME   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (start_at)
);
```

`themes`
```sql
CREATE TABLE theme
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255) NOT NULL,
    description     VARCHAR(10000), //설명은 null이여도 된다. h2가 아니라면 TEXT
    thumbnail_url     VARCHAR(2048), //이미지는 null이여도 된다. 단 디폴트 이미지를 써야한다.
    PRIMARY KEY (id),
    UNIQUE (name)
);
```

## Cycle2 기능 목록

- [x] 지난 날짜·시간에 대한 예약 생성 거부
- [x] 같은 날짜 + 시간 + 테마 중복 예약 거부
- [x] 예약이 존재하는 시간 삭제 거부
- [x] 유효하지 않은 입력값(빈 이름, 잘못된 날짜 형식, 필수값 누락 등) 거부
- [x] 서비스 정책 위반, 유효하지 않은 입력, 존재하지 않는 리소스에 대해 의도된 에러 응답 반환
- [x] 브라우저에서 에러 발생 시 사용자에게 의미 있는 메시지 표시
- [x] 사용자가 자신의 이름으로 본인 예약 목록 조회
- [x] 사용자가 자신의 예약 취소
- [x] 사용자가 자신의 예약 날짜·시간 변경
- [x] 변경/취소 시 발생하는 에러 케이스도 2단계 규칙에 맞춰 처리

## Cycle2 에러 응답 명세

응답 본문은 다음 형식을 사용한다.

```json
{
  "code": "RESERVATION_DUPLICATED",
  "status": 409,
  "message": "동일한 시기에 예약을 할 수 없습니다."
}
```

상태 코드 규칙:

- `400 Bad Request`
  - 유효하지 않은 입력값, 잘못된 날짜/시간 형식, 필수값 누락
- `404 Not Found`
  - 존재하지 않는 테마, 예약 시간, 내 예약
- `409 Conflict`
  - 중복 예약, 예약 중인 시간/테마 삭제, 이미 지난 예약 변경/취소

대표 코드 예시:

- `RESERVATION_NAME_REQUIRED`
- `RESERVATION_NAME_TOO_LONG`
- `THEME_ID_REQUIRED`
- `RESERVATION_TIME_ID_REQUIRED`
- `INVALID_DATE_FORMAT`
- `INVALID_TIME_FORMAT`
- `THEME_NOT_FOUND`
- `RESERVATION_TIME_NOT_FOUND`
- `MY_RESERVATION_NOT_FOUND`
- `RESERVATION_DUPLICATED`
- `RESERVATION_TIME_IN_USE`
- `THEME_IN_USE`
- `PAST_RESERVATION_CANNOT_BE_CANCELLED`
- `PAST_RESERVATION_CANNOT_BE_UPDATED`

## Cycle2 변경/취소 명세

사용자 예약 화면은 이름 기준 조회 후에만 본인 예약에 대한 변경/취소가 가능하다.

| 기능 | Http/url | 요청 파라미터 | 성공 응답 | 실패 응답 |
|--------|------------------------------------|-----------------------------------------|-----------------------------------------------------|-----------------------------------------------------|
| 내 예약 조회 | `GET` `/pages/user/reservations?reservationName={name}` | `reservationName` | `200 OK` HTML | 페이지 내 에러 메시지 |
| 내 예약 취소 | `POST` `/pages/user/reservations/{reservationId}/delete` | `reservationName` | `302 Redirect` | `302 Redirect` + `errorCode` |
| 내 예약 변경 | `POST` `/pages/user/reservations/{reservationId}/update` | `reservationName`, `date`, `timeId` | `302 Redirect` | `302 Redirect` + `errorCode` |

변경/취소 정책:

- 이미 지난 예약은 취소할 수 없다.
- 이미 지난 예약은 변경할 수 없다.
- 변경하려는 시간이 이미 차 있으면 변경할 수 없다.
- 조회한 이름의 예약이 아니면 변경/취소할 수 없다.























## API 설계 룰

```angular2html
1. 리소스 식별 기준
리소스는 비즈니스 도메인에서 독립적으로 식별 가능한 핵심 개체를 기준으로 정의한다.
 일반적으로 다른 객체에 의존하지 않고, 자체적인 생명주기를 가지는 상위 개념을 리소스로 본다.

2. 관리자/사용자 API 분리 기준
관리자 API와 사용자 API는 분리하는 것이 일반적이다.
두 API는 동일한 도메인을 다루더라도 목적과 책임이 다르며, 요구하는 데이터의 범위와 응답 형태도 다르기 때문이다.
 따라서 권한과 사용 시나리오에 따라 인터페이스를 분리하여 책임을 명확히 하는 것이 바람직하다.

관리자 API와 사용자 API는 책임과 권한이 명확히 다르기 때문에 컨트롤러뿐 아니라 URL도 분리하는 것이 일반적이다.
/api/admin/**, /api/**와 같이 prefix를 분리하면 가독성이 높아지고 권한 제어도 명확해져 유지보수성과 보안성이 향상된다.

 3. 우리 그룹의 "좋은 API" 정의

* 핵심 리소스를 정확히 파악할 수 있다.
* 오해의 소지가 없고 일관되어야 한다.
* 사용자가 명확하다. (/api/admin/**, /api/** )

--------------------------------------------------------------------------------------------------

4. 서버 클라이언트 책임 기준
클라이언트는 사용자 요구사항/UX&UI적으로 필요한 정보만 책임을 가진다.
서버는 클라이언트가 필요한 정보를 제공하는 것에 책임을 진다.

```
