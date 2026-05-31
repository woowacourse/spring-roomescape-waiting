# 방탈출 예약 대기 관리

## 🙋 기능 요구사항

이 문서는 기존 방탈출 사용자 예약 미션에 예약 대기 기능을 추가하기 위한 요구사항과 API 계약을 정리한다.

### 관리자 예약 관리

- 관리자는 예약자 이름, 예약 날짜, 예약 시간, 테마 정보를 입력해 예약을 추가할 수 있다.
- 관리자는 전체 활성 예약 요청 목록을 조회할 수 있다.
- 관리자는 특정 활성 예약 요청을 삭제할 수 있다.

### 관리자 예약 시간 관리

- 관리자는 시간을 입력해서 예약 시간을 등록할 수 있다.
- 관리자는 전체 예약 시간 목록을 조회할 수 있다.
- 관리자는 예약 시간을 삭제할 수 있다.

### 관리자 테마 관리

- 관리자는 테마의 이름, 설명, 썸네일 이미지 URL을 입력해 테마를 등록할 수 있다.
- 관리자는 전체 테마 목록을 조회할 수 있다.
- 관리자는 특정 테마를 삭제할 수 있다.

### 사용자 예약

- 사용자는 인기 테마를 확인할 수 있다.
- 사용자는 특정 날짜와 테마에 대한 전체 예약 시간을 조회할 수 있다.
- 사용자는 시간별 예약 가능 여부를 확인할 수 있다.
- 사용자는 예약 가능한 슬롯(날짜 + 시간 + 테마)에 예약을 신청할 수 있다.
- 사용자는 이미 다른 사용자가 예약한 슬롯에 같은 예약 API로 대기를 신청할 수 있다.
- 사용자는 본인의 예약 또는 대기를 취소할 수 있다.
- 사용자는 본인의 활성 예약 요청의 날짜와 시간을 변경할 수 있다.

### 예약 대기

- 같은 슬롯에 먼저 신청한 사용자는 예약 확정 상태가 된다.
- 같은 슬롯에 이미 활성 예약 요청이 있으면 이후 신청자는 대기 상태가 된다.
- 같은 슬롯에 대한 대기는 신청 순서대로 순번이 정해진다.
- 같은 사용자는 같은 슬롯에 활성 예약 또는 활성 대기를 중복으로 가질 수 없다.
- 사용자가 본인의 예약 또는 대기를 취소하면 해당 요청은 취소 이력으로 이동한다.
- 관리자가 활성 예약 요청을 삭제하면 취소 이력을 남기지 않고 활성 목록에서 제거한다.
- 활성 요청이 제거된 뒤 같은 슬롯의 예약 확정자와 대기 순번은 남아 있는 활성 요청의 신청 순서 기준으로 다시 계산된다.

### 내 예약 목록 조회

- 사용자는 본인의 활성 예약, 대기, 취소 이력을 함께 조회할 수 있다.
- 목록의 각 항목은 조회 시점에 계산된 상태로 구분된다.
    - `RESERVED`: 예약 확정
    - `WAITING`: 예약 대기
    - `CANCELED`: 취소된 예약 또는 대기
- `waitingRank`는 같은 슬롯에서 자신보다 앞에 있는 활성 요청 수이며, 예약 확정 항목은 `0`, 대기 항목은 현재 대기 순번으로 표시된다.
  취소 이력의 `waitingRank`는 `null`이다.

### 예약 정책

- 지나간 날짜와 시간에 대한 예약 또는 대기는 생성할 수 없다.
- 같은 날짜, 시간, 테마에 활성 요청이 없으면 예약 확정으로 생성된다.
- 같은 날짜, 시간, 테마에 활성 요청이 있으면 대기로 생성된다.
- 같은 사용자가 같은 날짜, 시간, 테마에 이미 활성 요청을 가지고 있으면 중복 신청을 거부한다.
- 활성 예약 또는 대기가 존재하는 예약 시간은 삭제할 수 없다.
- 빈 이름, 잘못된 날짜/시간 형식, 필수값 누락 등 유효하지 않은 입력값은 거부한다.
- 이미 지난 예약 또는 대기는 취소할 수 없다.
- 예약 요청 변경은 기존 활성 요청을 취소 이력으로 이동한 뒤, 변경 대상 슬롯에 새 요청을 생성하는 방식으로 처리한다.
- 예약 요청 변경 성공 시 응답되는 `id`는 새로 생성된 예약 요청 id이며, 기존 예약 요청 id와 다를 수 있다.
- 변경 대상 슬롯에 같은 사용자의 다른 활성 요청이 있으면 중복 신청으로 거부한다.

### 인기 테마

- 사용자는 최근 1주일 동안 예약이 많았던 테마 상위 10개를 확인할 수 있다.

## 🗄️ 저장 정책

### 활성 예약 요청

`reservation` 테이블은 현재 취소되지 않은 활성 예약 요청만 저장한다.
예약 확정과 대기는 별도 컬럼으로 저장하지 않고, 같은 슬롯의 활성 요청을 `request_order` 오름차순으로 정렬해 계산한다.

- 같은 슬롯에서 가장 빠른 `request_order`를 가진 요청은 `RESERVED`이다.
- 같은 슬롯에서 그 뒤에 있는 요청은 `WAITING`이다.
- 대기 순번은 같은 슬롯에서 자신보다 앞에 있는 활성 요청 수로 계산한다.

### 취소 이력

사용자가 예약 또는 대기를 취소하거나 예약 요청을 변경하면 기존 요청은 `reservation_history`에 복사한 뒤 `reservation`에서 제거한다.
따라서 `reservation`에는 `is_canceled` 컬럼을 두지 않는다.

`reservation_history.reservation_id`는 원본 예약 요청 id를 기록하기 위한 값이며, 원본 row가 활성 테이블에서 제거되므로 외래 키로 관리하지 않는다.

관리자 예약 삭제는 운영자가 활성 요청을 직접 제거하는 hard delete 정책으로 처리하며, `reservation_history`에 이력을 남기지 않는다.

### 신청 순서

`request_order`는 예약 요청의 신청 순서를 표현한다.
`id`는 식별자이고 `request_order`는 순서이므로, 비즈니스 로직에서 예약 확정자와 대기 순번을 계산할 때는 `request_order`를 기준으로 한다.

## 🌐 API 명세

### 공통 규칙

- 현재 API 명세에서는 예약자 `name`을 사용자 식별자로 사용한다.
- 회원/인증 기반으로 확장하는 경우 `name` 파라미터는 로그인 사용자 정보로 대체한다.
- 날짜 예시는 현재 날짜 이후의 값인 `2026-06-10`, `2026-06-11`을 사용한다.
- 시간 슬롯은 `date`, `timeId`, `themeId` 조합으로 식별한다.
- `status`, `waitingRank`는 저장된 컬럼이 아니라 조회 시점에 계산된 응답 값이다.

### 공통 에러 응답

에러 응답은 RFC 7807 `ProblemDetail` 형식을 따른다.
`detail`에는 사용자가 문제를 이해하고 다음 행동을 판단할 수 있는 메시지를 담는다.

**Response**

```http
Content-Type: application/json
```

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "에러 메시지"
}
```

요청 본문 검증에 실패한 경우에는 `errors` 필드에 잘못된 필드와 사유가 함께 포함된다.

```http
Content-Type: application/json
```

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "입력값이 올바르지 않습니다.",
  "errors": [
    {
      "field": "name",
      "reason": "예약자 이름은 비어 있을 수 없습니다."
    }
  ]
}
```

| statusCode                 | 상황                                                                                                                                                     |
|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| 400 Bad Request            | 요청 본문, 쿼리 파라미터, path variable의 형식이 잘못된 경우, 필수값이 누락된 경우, 빈 문자열이나 양수가 아닌 id처럼 유효하지 않은 값이 전달된 경우, 지나간 날짜와 시간으로 예약 생성/변경을 요청한 경우, 이미 지난 예약 또는 대기를 취소하려는 경우 |
| 404 Not Found              | 요청에서 조회, 변경, 삭제, 참조한 예약 요청, 예약 시간 또는 테마가 존재하지 않는 경우                                                                                                    |
| 405 Method Not Allowed     | 지원하지 않는 HTTP 메서드로 요청한 경우                                                                                                                               |
| 415 Unsupported Media Type | 지원하지 않는 Content-Type으로 요청한 경우                                                                                                                          |
| 409 Conflict               | 이미 존재하는 자원을 생성하려는 경우, 같은 사용자가 같은 슬롯에 활성 요청을 중복 생성하려는 경우, 활성 예약 또는 대기가 존재하는 예약 시간을 삭제하려는 경우                                                             |
| 500 Internal Server Error  | 서버 내부 오류가 발생한 경우. 내부 오류 상세는 응답에 노출하지 않는다.                                                                                                              |

### 예약 API

#### 예약 신청 API

예약 신청 API는 확정 예약과 대기 신청을 함께 처리한다.
요청한 슬롯에 활성 요청이 없으면 `RESERVED`로 생성되고, 이미 활성 요청이 있으면 `WAITING`으로 생성된다.

**Request**

```http
POST /reservations HTTP/1.1
Content-Type: application/json
```

```json
{
  "date": "2026-06-10",
  "name": "브라운",
  "timeId": 1,
  "themeId": 1
}
```

**Response**

```http
HTTP/1.1 201 Created
Content-Type: application/json
```

```json
{
  "id": 1,
  "status": "RESERVED",
  "waitingRank": 0,
  "name": "브라운",
  "date": "2026-06-10",
  "time": {
    "id": 1,
    "startAt": "10:00"
  },
  "theme": {
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://example.com/theme.png"
  }
}
```

이미 활성 요청이 있는 슬롯에 신청하면 대기로 생성된다.

```json
{
  "id": 2,
  "status": "WAITING",
  "waitingRank": 1,
  "name": "포비",
  "date": "2026-06-10",
  "time": {
    "id": 1,
    "startAt": "10:00"
  },
  "theme": {
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://example.com/theme.png"
  }
}
```

**Status Code**

| statusCode      | 상황                                                                                                                            |
|-----------------|-------------------------------------------------------------------------------------------------------------------------------|
| 201 Created     | 예약 확정 또는 대기 생성 성공                                                                                                             |
| 400 Bad Request | 예약자 이름이 비어 있거나 255자를 초과한 경우, 날짜 형식이 `yyyy-MM-dd`가 아닌 경우, 필수값이 누락된 경우, `timeId` 또는 `themeId`가 양수가 아닌 경우, 지나간 날짜와 시간에 대한 요청인 경우 |
| 404 Not Found   | `timeId` 또는 `themeId`에 해당하는 예약 시간 또는 테마가 존재하지 않는 경우                                                                           |
| 409 Conflict    | 같은 사용자가 같은 날짜, 시간, 테마에 이미 활성 예약 또는 대기를 가지고 있는 경우                                                                              |

#### 내 예약 및 대기 조회 API

활성 예약, 대기, 취소 이력을 함께 반환한다.
`id`는 활성 요청에서는 현재 예약 요청 id, 취소 이력에서는 원본 예약 요청 id를 의미한다.
`waitingRank`는 예약 확정 항목에서는 `0`, 대기 항목에서는 현재 대기 순번, 취소 이력에서는 `null`이다.

**Request**

```http
GET /reservations?name=브라운 HTTP/1.1
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "reservations": [
    {
      "id": 1,
      "status": "RESERVED",
      "waitingRank": 0,
      "name": "브라운",
      "date": "2026-06-10",
      "time": {
        "id": 1,
        "startAt": "10:00"
      },
      "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://example.com/theme.png"
      }
    },
    {
      "id": 3,
      "status": "WAITING",
      "waitingRank": 2,
      "name": "브라운",
      "date": "2026-06-11",
      "time": {
        "id": 2,
        "startAt": "11:00"
      },
      "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://example.com/theme.png"
      }
    },
    {
      "id": 4,
      "status": "CANCELED",
      "waitingRank": null,
      "name": "브라운",
      "date": "2026-06-12",
      "time": {
        "id": 3,
        "startAt": "12:00"
      },
      "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://example.com/theme.png"
      }
    }
  ]
}
```

**Status Code**

| statusCode      | 상황                                     |
|-----------------|----------------------------------------|
| 200 OK          | 예약자 이름에 해당하는 활성 예약, 대기, 취소 이력 목록 조회 성공 |
| 400 Bad Request | `name` 쿼리 파라미터가 누락되었거나 비어 있는 경우        |

#### 내 예약 변경 API

예약 변경은 기존 활성 요청을 취소 이력으로 이동하고, 변경 대상 슬롯에 새 요청을 생성하는 방식으로 처리한다.
변경 대상 슬롯에 활성 요청이 없으면 예약 확정으로 생성되고, 이미 활성 요청이 있으면 대기로 생성된다.
단, 같은 사용자가 변경 대상 슬롯에 이미 다른 활성 예약 또는 대기를 가지고 있으면 중복 신청으로 거부한다.
변경 성공 응답의 `id`는 변경 전 예약 요청 id가 아니라 새로 생성된 예약 요청 id이다.
따라서 클라이언트는 변경 이후 작업에 응답으로 받은 새 `id`를 사용해야 한다.

**Request**

```http
PATCH /reservations/1 HTTP/1.1
Content-Type: application/json
```

```json
{
  "name": "브라운",
  "date": "2026-06-11",
  "timeId": 2
}
```

**Response**

응답의 `id`는 변경 요청을 보낸 기존 예약 요청 id가 아니라, 변경 결과로 새로 생성된 예약 요청 id이다.

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "id": 4,
  "status": "RESERVED",
  "waitingRank": 0,
  "name": "브라운",
  "date": "2026-06-11",
  "time": {
    "id": 2,
    "startAt": "11:00"
  },
  "theme": {
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://example.com/theme.png"
  }
}
```

**Status Code**

| statusCode      | 상황                                                                                                                        |
|-----------------|---------------------------------------------------------------------------------------------------------------------------|
| 200 OK          | 본인 예약 요청 변경 성공                                                                                                            |
| 400 Bad Request | 예약 id 또는 `timeId`가 양수가 아닌 경우, 예약자 이름이 비어 있거나 255자를 초과한 경우, 날짜 형식이 `yyyy-MM-dd`가 아닌 경우, 필수값이 누락된 경우, 지나간 날짜와 시간으로 변경하려는 경우 |
| 404 Not Found   | 해당 이름으로 활성 예약 요청을 찾을 수 없거나 `timeId`에 해당하는 예약 시간이 존재하지 않는 경우                                                               |
| 409 Conflict    | 같은 사용자가 변경 대상 날짜, 시간, 테마에 이미 다른 활성 예약 또는 대기를 가지고 있는 경우                                                                    |

#### 내 예약 또는 대기 취소 API

예약 확정 항목과 대기 항목 모두 같은 API로 취소한다.
취소된 요청은 `reservation_history`로 이동하고 활성 예약 목록에서는 제거된다.

**Request**

```http
DELETE /reservations/1?name=브라운 HTTP/1.1
```

**Response**

```http
HTTP/1.1 204 No Content
```

**Status Code**

| statusCode      | 상황                                                                       |
|-----------------|--------------------------------------------------------------------------|
| 204 No Content  | 본인 예약 또는 대기 취소 성공, 또는 취소할 활성 요청이 존재하지 않는 경우                              |
| 400 Bad Request | 예약 요청 id가 양수가 아닌 경우, `name` 쿼리 파라미터가 누락되었거나 비어 있는 경우, 이미 지난 요청을 취소하려는 경우 |
| 404 Not Found   | 해당 id의 활성 요청은 존재하지만 예약자 이름이 일치하지 않는 경우                                   |

### 관리자 예약 API

#### 관리자 예약 조회 API

관리자는 활성 예약 요청 목록을 조회한다.
응답에는 예약 확정과 대기가 함께 포함되며, 각 항목의 상태와 대기 순번은 조회 시점에 계산된다.

**Request**

```http
GET /admin/reservations HTTP/1.1
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "reservations": [
    {
      "id": 1,
      "status": "RESERVED",
      "waitingRank": 0,
      "name": "브라운",
      "date": "2026-06-10",
      "time": {
        "id": 1,
        "startAt": "10:00"
      },
      "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://example.com/theme.png"
      }
    }
  ]
}
```

**Status Code**

| statusCode | 상황                    |
|------------|-----------------------|
| 200 OK     | 관리자 활성 예약 요청 목록 조회 성공 |

#### 관리자 예약 삭제 API

관리자 삭제는 활성 예약 요청을 즉시 제거하는 hard delete로 처리한다.
사용자 취소와 달리 `reservation_history`에 취소 이력을 남기지 않는다.

**Request**

```http
DELETE /admin/reservations/1 HTTP/1.1
```

**Response**

```http
HTTP/1.1 204 No Content
```

**Status Code**

| statusCode      | 상황                                    |
|-----------------|---------------------------------------|
| 204 No Content  | 예약 요청 삭제 성공, 또는 삭제할 활성 요청이 존재하지 않는 경우 |
| 400 Bad Request | 예약 요청 id가 양수가 아닌 경우                   |

### 시간 API

#### 시간 추가 API

**Request**

```http
POST /admin/times HTTP/1.1
Content-Type: application/json
```

```json
{
  "startAt": "10:00"
}
```

**Response**

```http
HTTP/1.1 201 Created
Content-Type: application/json
```

```json
{
  "id": 1,
  "startAt": "10:00"
}
```

**Status Code**

| statusCode      | 상황                                  |
|-----------------|-------------------------------------|
| 201 Created     | 예약 시간 생성 성공                         |
| 400 Bad Request | 예약 시간이 비어 있거나 시간 형식이 `HH:mm`이 아닌 경우 |
| 409 Conflict    | 같은 시작 시간의 예약 시간이 이미 존재하는 경우         |

#### 시간 조회 API

**Request**

```http
GET /times HTTP/1.1
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "times": [
    {
      "id": 1,
      "startAt": "10:00"
    }
  ]
}
```

**Status Code**

| statusCode | 상황             |
|------------|----------------|
| 200 OK     | 예약 시간 목록 조회 성공 |

#### 시간 삭제 API

**Request**

```http
DELETE /admin/times/1 HTTP/1.1
```

**Response**

```http
HTTP/1.1 204 No Content
```

**Status Code**

| statusCode      | 상황                                 |
|-----------------|------------------------------------|
| 204 No Content  | 예약 시간 삭제 성공                        |
| 400 Bad Request | 예약 시간 id가 양수가 아닌 경우                |
| 404 Not Found   | 삭제하려는 예약 시간이 존재하지 않는 경우            |
| 409 Conflict    | 해당 예약 시간에 연결된 활성 예약 또는 대기가 존재하는 경우 |

#### 날짜와 테마 기준 시간 상태 조회 API

예약 여부와 무관하게 전체 예약 시간을 반환한다.
`isAvailable`이 `true`이면 현재 활성 요청이 없어 예약 확정으로 신청할 수 있고, `false`이면 이미 활성 요청이 있어 대기로 신청할 수 있다.

**Request**

```http
GET /times/availability?date=2026-06-10&themeId=1 HTTP/1.1
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "availableTimes": [
    {
      "id": 1,
      "startAt": "10:00",
      "isAvailable": false
    },
    {
      "id": 2,
      "startAt": "11:00",
      "isAvailable": true
    }
  ]
}
```

**Status Code**

| statusCode      | 상황                                                                     |
|-----------------|------------------------------------------------------------------------|
| 200 OK          | 날짜와 테마 기준 시간 상태 조회 성공                                                  |
| 400 Bad Request | `date` 형식이 `yyyy-MM-dd`가 아니거나 필수 쿼리 파라미터가 누락된 경우, `themeId`가 양수가 아닌 경우 |
| 404 Not Found   | `themeId`에 해당하는 테마가 존재하지 않는 경우                                         |

### 테마 API

#### 테마 추가 API

**Request**

```http
POST /admin/themes HTTP/1.1
Content-Type: application/json
```

```json
{
  "name": "공포",
  "description": "무서움",
  "thumbnail": "https://example.com/theme.png"
}
```

**Response**

```http
HTTP/1.1 201 Created
Content-Type: application/json
```

```json
{
  "id": 1,
  "name": "공포",
  "description": "무서움",
  "thumbnail": "https://example.com/theme.png"
}
```

**Status Code**

| statusCode      | 상황                                |
|-----------------|-----------------------------------|
| 201 Created     | 테마 생성 성공                          |
| 400 Bad Request | 이름, 설명, 썸네일이 비어 있거나 길이 제한을 초과한 경우 |
| 409 Conflict    | 같은 이름의 테마가 이미 존재하는 경우             |

#### 테마 목록 조회 API

**Request**

```http
GET /themes HTTP/1.1
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "themes": [
    {
      "id": 1,
      "name": "공포",
      "description": "무서움",
      "thumbnail": "https://example.com/theme.png"
    }
  ]
}
```

**Status Code**

| statusCode | 상황          |
|------------|-------------|
| 200 OK     | 테마 목록 조회 성공 |

#### 테마 삭제 API

**Request**

```http
DELETE /admin/themes/1 HTTP/1.1
```

**Response**

```http
HTTP/1.1 204 No Content
```

**Status Code**

| statusCode      | 상황                              |
|-----------------|---------------------------------|
| 204 No Content  | 테마 삭제 성공                        |
| 400 Bad Request | 테마 id가 양수가 아닌 경우                |
| 404 Not Found   | 삭제하려는 테마가 존재하지 않는 경우            |
| 409 Conflict    | 해당 테마에 연결된 활성 예약 또는 대기가 존재하는 경우 |

#### 인기 테마 조회 API

**Request**

```http
GET /themes/popularity?days=7&size=10 HTTP/1.1
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "themes": [
    {
      "id": 1,
      "name": "공포",
      "description": "무서움",
      "thumbnail": "https://example.com/theme.png"
    }
  ]
}
```

**Status Code**

| statusCode      | 상황                                            |
|-----------------|-----------------------------------------------|
| 200 OK          | 인기 테마 조회 성공                                   |
| 400 Bad Request | `days` 또는 `size`가 양수가 아니거나 필수 쿼리 파라미터가 누락된 경우 |

## 🖥️ 프론트엔드 페이지

### 사용자 예약 페이지

- 경로: `http://localhost:8080`
- 사용자는 인기 테마를 확인할 수 있다.
- 사용자는 날짜와 테마를 선택해 전체 예약 시간을 조회할 수 있다.
- 시간 목록에는 현재 활성 요청이 없는 시간이 선택 가능하게 표시된다.
- 이미 활성 요청이 있는 시간은 비활성화되어 화면에서 직접 선택할 수 없다.
- 화면에서 선택한 예약 요청은 `POST /reservations`로 생성된다.
- 사용자는 이름을 입력해 본인의 예약, 대기, 취소 이력을 함께 조회할 수 있다.
- 내 예약 목록에는 테마, 날짜, 시간, 예약자 이름과 변경/취소 버튼이 표시된다.
- 사용자는 본인의 예약 또는 대기를 같은 취소 버튼으로 취소할 수 있다.
- 예약, 대기, 조회, 취소 요청에 실패하면 화면에서 실패 메시지를 확인할 수 있다.

### 관리자 페이지

- 경로: `http://localhost:8080/admin`
- 관리자는 사용자 예약 화면과 같은 흐름으로 날짜, 테마, 시간, 이름을 입력해 예약 요청을 등록할 수 있다.
- 관리자는 활성 예약 요청 목록을 조회하고 요청을 삭제할 수 있다.
- 관리자는 예약 시간과 테마를 등록하거나 삭제할 수 있다.
- 관리자 작업에 실패하면 화면에서 실패 메시지를 확인할 수 있다.
