# spring-roomescape-waiting

방탈출 예약과 예약 대기를 관리하는 Spring Boot 애플리케이션입니다.

## 기능 목록

### 예약 관리

- 사용자는 이름, 날짜 ID, 시간 ID, 테마 ID를 입력해 예약을 생성한다.
- 같은 슬롯에 활성 예약이 없으면 `CONFIRMED`, 이미 활성 예약이 있으면 `WAITING`으로 생성한다.
- 예약 생성 시 같은 사용자가 같은 날짜, 시간, 테마에 이미 활성 예약을 가지고 있으면 예외를 반환한다.
- 사용자는 이름으로 본인의 예약 이력을 조회한다.
- 관리자도 전체 예약 이력을 조회한다.
- 취소된 예약은 `CANCELED` 상태로 남기며, 사용자와 관리자 조회에 포함된다.
- 사용자는 본인의 활성 예약을 취소한다.
- 관리자는 예약 ID로 활성 예약을 취소한다.
- 확정 예약이 취소되면 해당 슬롯의 첫 번째 대기 예약이 자동으로 확정 예약으로 전환된다.
- 사용자는 활성 예약의 날짜와 시간을 변경한다.
- 날짜 ID와 시간 ID가 모두 누락된 예약 변경 요청은 기존 예약 날짜와 시간을 유지하고 수정 시각을 갱신한다.
- 같은 날짜와 시간으로 다시 변경해도 수정 시각을 갱신하고, 대기 순서를 수정 시각 기준으로 재정렬한다.
- 취소된 예약은 수정하거나 다시 취소할 수 없다.
- `waitingNumber`는 저장하지 않고 조회 시 계산한다. `WAITING` 상태일 때만 대기 번호를 반환하고, `CONFIRMED`와 `CANCELED`는 `null`을
  반환한다.
- 예약 생성 시 같은 슬롯의 상태 판단은 `reservation_slot` row lock으로 보호한다.
- 예약 취소와 예약 변경의 동시성 처리는 추가 고려 지점으로 남겨둔다.

### 예약 슬롯 조회

- 사용자는 특정 테마와 날짜에 대해 시간별 예약 현황을 조회한다.
- 응답의 `waitingNumber`는 해당 시간에 새로 예약할 경우의 대기 번호를 의미한다.
- `waitingNumber`가 `0`이면 바로 확정 예약이 가능하다.

### 예약 날짜 관리

- 사용자는 예약 날짜 목록을 조회한다.
- 관리자는 예약 날짜 목록을 조회한다.
- 관리자는 예약 날짜를 추가한다.
- 관리자는 예약 날짜를 삭제한다.
- 이미 예약 슬롯에 사용 중인 날짜를 삭제하려 하면 예외를 반환한다.

### 예약 시간 관리

- 관리자는 예약 시간 목록을 조회한다.
- 관리자는 예약 시간을 추가한다.
- 관리자는 예약 시간을 삭제한다.
- 예약 시간 입력이 없거나 형식이 올바르지 않으면 예외를 반환한다.
- 이미 예약 슬롯에 사용 중인 시간을 삭제하려 하면 예외를 반환한다.

### 테마 관리

- 사용자는 전체 테마 목록을 조회한다.
- 사용자는 최근 7일 이내 예약 날짜를 기준으로 인기 테마 랭킹을 조회한다.
- 인기 테마 랭킹은 `CANCELED` 예약을 제외하고 집계한다.
- 관리자는 전체 테마 목록을 조회한다.
- 관리자는 테마를 추가한다.
- 관리자는 테마를 삭제한다.
- 이미 예약 슬롯에 사용 중인 테마를 삭제하려 하면 예외를 반환한다.

### 공통 예외 처리

- 관리자 API는 `X-ADMIN-TOKEN` 헤더가 올바르지 않으면 `401 Unauthorized`를 반환한다.
- 비즈니스 예외는 `{"code": "...", "message": "..."}` 형식으로 반환한다.
- 요청 본문의 타입이나 형식이 올바르지 않으면 `INPUT_FORMAT_ERROR`를 반환한다.
- 처리하지 못한 서버 예외는 공통 에러 응답으로 반환한다.

## API 명세

### 공통 규칙

- Base URL: `/`
- 관리자 API 헤더: `X-ADMIN-TOKEN: {admin-token}`
- Content-Type: `application/json`

### 화면 경로

#### `GET /`

- 설명: 사용자 예약 페이지 반환
- 응답: `times` 뷰 렌더링

#### `GET /admin`

- 설명: 관리자 페이지 반환
- 응답: `admin` 뷰 렌더링

## 예약

### `POST /reservations`

- 설명: 예약 생성
- 응답: `201 Created`

요청:

```json
{
  "name": "보예",
  "dateId": 8,
  "timeId": 2,
  "themeId": 1
}
```

응답:

```json
{
  "id": 30,
  "date": "2026-06-06",
  "time": "11:00",
  "theme": {
    "name": "공포",
    "content": "오금이 저리는 공포입니다.",
    "url": "/themes/scary"
  }
}
```

### `GET /reservations?name={name}`

- 설명: 사용자 이름으로 예약 이력 조회
- 응답: `200 OK`

```json
{
  "username": "이산",
  "reservations": [
    {
      "id": 2,
      "reservationSlot": {
        "id": 1,
        "date": {
          "id": 1,
          "startWhen": "2026-05-30"
        },
        "time": {
          "id": 1,
          "startAt": "10:00"
        },
        "theme": {
          "id": 1,
          "name": "공포",
          "content": "오금이 저리는 공포입니다.",
          "url": "/themes/scary"
        }
      },
      "status": "WAITING",
      "waitingNumber": 1
    }
  ]
}
```

### `PATCH /reservations/{id}`

- 설명: 사용자 예약 날짜와 시간 변경
- 응답: `204 No Content`

요청:

```json
{
  "dateId": 9,
  "timeId": 4
}
```

### `DELETE /reservations/{id}`

- 설명: 사용자 예약 취소
- 응답: `204 No Content`

### `GET /admin/reservations`

- 설명: 전체 예약 이력 조회
- 인증: 관리자
- 응답: `200 OK`

```json
[
  {
    "id": 1,
    "date": "2026-05-30",
    "time": {
      "id": 1,
      "startAt": "10:00"
    },
    "theme": {
      "id": 1,
      "name": "공포",
      "content": "오금이 저리는 공포입니다.",
      "url": "/themes/scary"
    },
    "userName": "보예",
    "waitingNumber": null,
    "reservationStatus": "CONFIRMED"
  }
]
```

### `DELETE /admin/reservations/{id}`

- 설명: 관리자의 예약 취소
- 인증: 관리자
- 응답: `204 No Content`

## 예약 슬롯

### `GET /reservation-slots?themeId={themeId}&dateId={dateId}`

- 설명: 특정 테마와 날짜의 시간별 예약 현황 조회
- 응답: `200 OK`

```json
[
  {
    "timeId": 1,
    "startAt": "10:00",
    "waitingNumber": 0
  },
  {
    "timeId": 2,
    "startAt": "11:00",
    "waitingNumber": 2
  }
]
```

## 예약 날짜

### `GET /reservation-dates`

- 설명: 사용자용 예약 날짜 목록 조회
- 응답: `200 OK`

```json
[
  {
    "id": 8,
    "reservationDate": "2026-06-06"
  }
]
```

### `GET /admin/reservation-dates`

- 설명: 관리자용 예약 날짜 목록 조회
- 인증: 관리자
- 응답: `200 OK`

### `POST /admin/reservation-dates`

- 설명: 예약 날짜 추가
- 인증: 관리자
- 응답: `201 Created`

요청:

```json
{
  "reservationDate": "2026-06-17"
}
```

응답:

```json
{
  "id": 12,
  "reservationDate": "2026-06-17"
}
```

### `DELETE /admin/reservation-dates/{id}`

- 설명: 예약 날짜 삭제
- 인증: 관리자
- 응답: `204 No Content`

## 예약 시간

### `GET /admin/times`

- 설명: 전체 예약 시간 목록 조회
- 인증: 관리자
- 응답: `200 OK`

```json
[
  {
    "id": 1,
    "startAt": "10:00"
  }
]
```

### `POST /admin/times`

- 설명: 예약 시간 추가
- 인증: 관리자
- 응답: `200 OK`

요청:

```json
{
  "startAt": "18:00"
}
```

응답:

```json
{
  "id": 6,
  "startAt": "18:00"
}
```

### `DELETE /admin/times/{id}`

- 설명: 예약 시간 삭제
- 인증: 관리자
- 응답: `200 OK`

## 테마

### `GET /themes`

- 설명: 전체 테마 목록 조회
- 응답: `200 OK`

```json
[
  {
    "id": 1,
    "name": "공포",
    "content": "오금이 저리는 공포입니다.",
    "url": "/themes/scary"
  }
]
```

### `GET /themes/rank`

- 설명: 최근 7일 이내 예약 날짜를 기준으로 인기 테마 랭킹 조회
- 응답: `200 OK`

```json
[
  {
    "id": 1,
    "themeName": "공포",
    "url": "/themes/scary",
    "rank": 1
  }
]
```

### `GET /admin/themes`

- 설명: 관리자용 테마 목록 조회
- 인증: 관리자
- 응답: `200 OK`

### `POST /admin/themes`

- 설명: 테마 추가
- 인증: 관리자
- 응답: `201 Created`

요청:

```json
{
  "name": "추리",
  "content": "단서를 조합해 탈출하는 테마입니다.",
  "url": "/themes/detective"
}
```

응답:

```json
{
  "id": 13,
  "name": "추리",
  "content": "단서를 조합해 탈출하는 테마입니다.",
  "url": "/themes/detective"
}
```

### `DELETE /admin/themes/{id}`

- 설명: 테마 삭제
- 인증: 관리자
- 응답: `204 No Content`
