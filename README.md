# spring-roomescape-waiting

방탈출 예약 시스템입니다.  
사용자는 세션 로그인 후 테마와 날짜를 기준으로 예약 슬롯을 조회하고, `slotId`로 예약을 생성/변경/취소합니다.  
관리자는 `X-ADMIN-TOKEN` 헤더로 인증하고 테마, 예약 시간, 예약을 관리합니다.

## 화면

- `GET /` 사용자 예약 페이지
- `GET /admin` 관리자 페이지

## 인증

### `POST /signup`

회원가입 후 세션에 로그인 상태를 저장합니다.

```json
{
  "name": "보예",
  "password": "password"
}
```

### `POST /login`

로그인 후 세션에 로그인 상태를 저장합니다.

```json
{
  "name": "보예",
  "password": "password"
}
```

### `DELETE /logout`

현재 세션을 종료합니다.

## 사용자 예약

### `GET /reservations`

현재 로그인한 사용자의 예약 목록을 조회합니다.

응답 예시:

```json
{
  "username": "보예",
  "reservations": [
    {
      "id": 29,
      "slot": {
        "id": 5,
        "date": "2026-05-01",
        "startAt": {
          "id": 2,
          "startAt": "11:00"
        },
        "theme": {
          "id": 3,
          "name": "청춘물",
          "content": "학교 배경인 테마 입니다.",
          "url": "/themes/youth"
        }
      },
      "waitingNumber": 1,
      "status": "WAITING"
    }
  ]
}
```

### `POST /reservations`

예약을 생성합니다.

```json
{
  "slotId": 20
}
```

### `PATCH /reservations/{id}`

예약 슬롯을 변경합니다.

```json
{
  "slotId": 21
}
```

### `DELETE /reservations/{id}`

예약을 취소합니다.

## 예약 슬롯 조회

### `GET /reservation-slots?themeId={themeId}&date={date}`

특정 테마와 날짜의 예약 슬롯 목록을 조회합니다.

응답은 `reservationSlots`로 감싸서 내려갑니다.

```json
{
  "reservationSlots": [
    {
      "slotId": 1,
      "timeId": 1,
      "startAt": "10:00",
      "waitingNumber": 0
    },
    {
      "slotId": 2,
      "timeId": 2,
      "startAt": "11:00",
      "waitingNumber": 1
    }
  ]
}
```

## 테마

### `GET /themes`

전체 테마 목록을 조회합니다.

```json
{
  "themes": [
    {
      "id": 1,
      "name": "공포",
      "content": "오금이 저리는 공포입니다.",
      "url": "/themes/scary"
    }
  ]
}
```

### `GET /themes/rank`

최근 7일 기준 인기 테마 랭킹을 조회합니다.

```json
{
  "popularThemes": [
    {
      "id": 1,
      "name": "공포",
      "thumbnailUrl": "/themes/scary",
      "rank": 1
    }
  ]
}
```

### `GET /admin/themes`

관리자용 테마 목록을 조회합니다.

### `POST /admin/themes`

테마를 추가합니다.

```json
{
  "name": "추리",
  "content": "단서를 조합해 탈출하는 테마입니다.",
  "thumbnailUrl": "/themes/detective"
}
```

### `DELETE /admin/themes/{id}`

테마를 삭제합니다.

## 예약 시간

### `GET /admin/times`

전체 예약 시간 목록을 조회합니다.

### `POST /admin/times`

예약 시간을 추가합니다.

```json
{
  "startAt": "18:00"
}
```

### `DELETE /admin/times/{id}`

예약 시간을 삭제합니다.

## 관리자 예약

### `GET /admin/reservations`

전체 예약 목록을 조회합니다.

### `POST /admin/reservations`

예약을 추가합니다.

```json
{
  "username": "보예",
  "slotId": 20
}
```

### `PATCH /admin/reservations/{id}`

예약 슬롯을 변경합니다.

```json
{
  "slotId": 21
}
```

### `DELETE /admin/reservations/{id}`

예약을 삭제합니다.

## 공통 규칙

- 관리자 API는 `X-ADMIN-TOKEN` 헤더가 필요합니다.
- 요청 본문이나 파라미터 형식이 잘못되면 `INPUT_FORMAT_ERROR`를 반환합니다.
- 비즈니스 예외는 `{"code": "...", "message": "..."}` 형식으로 반환합니다.
- 처리하지 못한 예외는 공통 에러 응답으로 반환합니다.

## 프론트 동작

- 사용자 페이지는 회원가입/로그인 후 예약을 진행합니다.
- 예약은 `테마 + 날짜`를 선택한 뒤 `reservationSlots` 목록에서 `slotId`를 골라 생성합니다.
- 내 예약 섹션에서는 현재 로그인한 사용자의 예약만 조회합니다.
- 관리자 페이지는 테마/시간/예약 관리와 예약 추가를 제공합니다.

