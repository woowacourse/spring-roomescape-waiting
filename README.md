# 기능 요구 사항
## 사이클 1
### 1단계: 테마 도메인 추가
#### 사용자
- [X] 사용자는 테마를 조회할 수 있다.
  - [X] 전체 테마 조회
  - [X] 단일 테마 조회

#### 관리자
- [x] 관리자는 테마를 조회할 수 있다.
  - [x] 전체 테마 조회
  - [x] 단일 테마 조회
- [x] 관리자는 테마를 추가할 수 있다.
- [x] 관리자는 테마를 삭제할 수 있다.

### 2단계: 사용자 예약
- [x] 사용자가 날짜와 테마를 선택해 예약 가능한 시간 목록이 표시된다.  
      - 예약 가능한 시간이란, 관리자가 등록한 시간 중 해당 날짜+테마에 아직 예약이 없는 시간이다.
- [x] 사용자가 예약 가능한 시간을 선택하여 본인의 이름으로 예약한다.  
      - 같은 날짜/시간이라도 테마가 다르면 예약 가능하다.

## 사이클 2
### 1단계 - 서비스 정책 적용
- [x] 지나간 날짜·시간에 대한 예약 생성은 불가능하다.
- [x] 같은 날짜+시간+테마에 이미 예약이 있으면 중복 예약을 거부한다.
- [x] 예약이 존재하는 시간을 삭제할 수 없다.
- [x] 유효하지 않은 입력값(빈 이름, 잘못된 날짜 형식 등)을 거부한다.

## 2단계 - 에러 응답 설계
- [x] 서비스 정책을 위반한 요청에는 의도된 에러 응답을 반환한다.
- [x] 유효하지 않은 입력값에는 의도된 에러 응답을 반환한다.
- [x] 존재하지 않는 리소스를 요청한 경우 의도된 에러 응답을 반환한다.
- [x] 잘못된 요청으로 인해 500 서버 에러가 사용자에게 노출되지 않도록 한다.
- [x] 에러 응답 본문 결정
    - 무엇이 잘못되었는지

## 3단계 - 내 예약 조회/변경/취소

- [x] 사용자는 자신의 이름으로 본인의 예약 목록을 조회할 수 있다.
- [x] 사용자는 본인의 예약 날짜와 시간을 변경할 수 있다.
- [x] 이미 지난 예약은 변경할 수 없다.
- [x] 변경하려는 날짜·시간·테마에 이미 예약이 존재하면 변경할 수 없다.
- [x] 존재하지 않는 예약을 조회, 변경, 취소하려는 경우 의도된 에러 응답을 반환한다.
- [x] 이미 지난 예약은 취소할 수 없다.
- [x] 사용자는 본인의 예약을 취소할 수 있다.
- [x] 예약 변경·취소 과정에서 발생하는 에러는 2단계에서 정한 에러 응답 규칙에 맞춰 처리한다.

## API 명세

| 기능 | Method | URL                                                                           | 설명 |
| --- | --- |-------------------------------------------------------------------------------| --- |
| 사용자 전체 테마 조회 | GET | `/user/themes`                                                                | 전체 테마 조회 |
| 사용자 단일 테마 조회 | GET | `/user/themes/{id}`                                                           | 특정 테마 조회 |
| 관리자 전체 테마 조회 | GET | `/admin/themes`                                                               | 전체 테마 조회 |
| 관리자 단일 테마 조회 | GET | `/admin/themes/{id}`                                                          | 특정 테마 조회 |
| 관리자 테마 생성 | POST | `/admin/themes`                                                               | 테마 생성 |
| 관리자 테마 삭제 | DELETE | `/admin/themes/{id}`                                                          | 테마 삭제 |
| 전체 예약 시간 조회 | GET | `/user/times`                                                                 | 전체 예약 시간 조회 |
| 예약 가능 시간 조회 | GET | `/user/times?themeId={themeId}&date={date}`                                   | 날짜/테마 기준 예약 가능 시간 조회 |
| 예약 시간 생성 | POST | `/user/times`                                                                 | 예약 시간 생성 |
| 예약 시간 삭제 | DELETE | `/user/times/{id}`                                                            | 예약 시간 삭제 |
| 전체 예약 조회 | GET | `/user/reservations`                                                          | 전체 예약 조회 |
| 단일 예약 조회 | GET | `/user/reservations/{id}`                                                     | 단일 예약 조회 |
| 내 예약 조회 | GET | `/user/reservations/list?name={name}`                                         | 이름 기준 예약 조회 |
| 예약 생성 | POST | `/user/reservations`                                                          | 예약 생성 |
| 내 예약 변경 | PATCH | `/user/reservations/{id}`                                                     | 이름 검증 후 예약 날짜/시간 변경 |
| 예약 삭제 | DELETE | `/user/reservations/{id}`                                                     | 예약 삭제 |
| 내 예약 취소 | DELETE | `/user/reservations/my/{id}?name={name}`                                      | 이름 검증 후 예약 취소 |
| 인기 테마 조회 | GET | `/user/themes/trending?startDate={startDate}&endDate={endDate}&limit={limit}` | 인기 테마 조회 |

### 사용자 전체 테마 조회

```json
GET /themes HTTP/1.1
Content-Type: application/json

HTTP/1.1 200 OK
Content-Type: application/json
[
	{
		"id" : 1,
		"name" : "은하수",
		"description": "은하수가 휘몰아치는 밤...",
		"image": "https://example.com/images/photo.jpg"
	},
	{
    "id": 2,
    "name": "지구",
    "description": "지구멸망 3분전",
    "image": "https://example.com/images/photo2.jpg"
  }
]
```

**근거:**

- GET 호출 후 정상을 표현하기 위해 200 상태 코드를 반환하도록 했다.
- 전체 정보를 반환하기 위해 리스트 형태로 반환한다.

### 사용자 단일 테마 조회

```json
GET /themes/{id}

HTTP/1.1 200 OK
Content-Type: application/json

{
	"id" : 1,
	"name" : "은하수",
	"description" : "은하수가 휘몰아치는 밤...",
	"image" : "https://example.com/images/photo.jpg"
}
```

**근거:**

- id의 테마 조회 성공을 표현하기 위해 200 상태 코드를 반환하도록 했다.

### 관리자 전체 테마 조회

```json
GET /admin/themes HTTP/1.1
Content-Type: application/json

HTTP/1.1 200 OK
Content-Type: application/json
[
	{
		"id" : 1,
		"name" : "은하수",
		"description": "은하수가 휘몰아치는 밤...",
		"image": "https://example.com/images/photo.jpg"
	},
	{
		...
	}
]
```

### 관리자 단일 테마 조회

```json
GET /admin/themes/{id}

HTTP/1.1 200 OK
Content-Type: application/json

{
	"id" : 1,
	"name" : "은하수",
	"description" : "은하수가 휘몰아치는 밤...",
	"image" : "https://example.com/images/photo.jpg"
}

```

### 관리자 테마 생성

```json
POST /admin/themes HTTP/1.1
Content-Type: application/json

{
	"name" : "은하수",
	"description" : "은하수가 휘몰아치는 밤...",
	"image" : "https://example.com/images/photo.jpg"
}

HTTP/1.1 201 Created
Content-Type: application/json

{
	"id" : 1,
	"name" : "은하수",
	"description" : "은하수가 휘몰아치는 밤...",
	"image" : "https://example.com/images/photo.jpg"
}
```

**근거:**

- 성공적으로 데이터가 생성되었다는 것을 표현하기 위해 201 상태코드를 반환하도록 했다.

### 관리자 테마 삭제

```json
DELETE /admin/themes/{id} HTTP/1.1
Content-Type: application/json

HTTP/1.1 204 No Content
Content-Type: application/json
```

**근거:**

- 성공적으로 데이터가 삭제되었다는 것을 표현하기 위해 204 상태코드를 반환하도록 했다.

### 예약 가능 시간 조회

```json
GET user/times?themeId=1&date=2026-05-04

HTTP/1.1 200 OK
Content-Type: application/json

[
	{
		"startAt": "10:00",
		"is_available": true
	},
	{
		"startAt": "11:00",
		"is_available": false
	},
	{
		...
	}
]
```

**근거:**

- 테마, 날짜별로 가능한 시간을 필터링하기 위해 쿼리 파라미터로 요청하였다.
- 응답은 클라이언트에서 예약 가능한 시간과 불가능한 시간을 구분하여 보여주기 위해 전체 시간과 예약 가능한지에 대한 정보를 리스트 형태로 넘겨준다.

### 예약 생성

```json
POST /reservations
Content-Type: application/json

{
	"themeId": 1,
	"name": "브라운",
	"date": "2026-05-04",
	"time": "10:00"
}

HTTP/1.1 201 Created
Content-Type: application/json

응답 A안: 
{
    "id": 4,
    "name": "초록",
    "themeId": 2,
    "date": "2026-05-05",
    "time": {
        "id": 7,
        "startAt": "16:00"
    }
}

응답 B안: 
{
	"id": 1,
	"theme_name": "은하수",
	"reservation_info": { 
		"name": "브라운",
		"date": "2026-05-04",
		"time": "10:00",
		"reservationAt": "2026-05-03T15:30:00Z"
	},
	"messeage": "예약이 완료되었습니다."
}
```

**선택:**

- 응답 A안

**근거:**

- 사용자가 예약한 정보를 확인할 수 있게 테마 이름, 예약자 이름, 날짜, 시간, 예약시점, 예약메시지를 반환한다.

### 인기 테마 조회

```json
GET /themes/trending?startDate=2026-05-01&endDate=2026-05-07&limit=10

HTTP/1.1 200 OK
Content-Type: application/json
[
    {
        "id" : 1,
        "name" : "은하수",
        "description" : "은하수가 휘몰아치는 밤...",
        "image" : "https://example.com/images/photo1.jpg"
    },
    {
        "id" : 2,
        "name" : "지구",
        "description" : "지구멸망 3분전",
        "image" : "https://example.com/images/photo2.jpg"
    },
    {
      ...
    }
]
```

**근거:**

- 백엔드에서는 실제 필요한 기간과 상위 개수만 받기 위해서 클라이언트에서 기간에 대한 처리를 완료한다.

### 응답 상태 코드 규칙

| 상태 코드 | 의미 |
| --- | --- |
| 200 OK | 조회 성공 |
| 201 Created | 생성 성공 |
| 204 No Content | 삭제 성공 |
| 400 Bad Request | 요청 형식 오류, 유효하지 않은 입력, 지난 시간 예약 요청 |
| 403 Forbidden | 다른 사람의 예약 변경 또는 취소 요청 |
| 404 Not Found | 존재하지 않는 예약 요청 |
| 409 Conflict | 중복 예약, 예약이 존재하는 시간 삭제 요청 |

### 에러 응답 명세
의도한 예외는 상태 코드와 함께 아래 JSON 형식으로 응답한다.

```json
{
  "code": "ERROR_CODE",
  "message": "무엇이 잘못되었는지 설명하는 메시지"
}
```

| 에러 코드 | 상태 코드 | 본문 예시 |
| --- | --- | --- |
| `INVALID_NAME_FORMAT` | 400 Bad Request | `{ "code": "INVALID_NAME_FORMAT", "message": "이름은 필수입니다." }` |
| `INVALID_DATE_FORMAT` | 400 Bad Request | `{ "code": "INVALID_DATE_FORMAT", "message": "날짜 형식이 잘못되었습니다. (yyyy-MM-dd)" }` |
| `PAST_RESERVATION` | 400 Bad Request | `{ "code": "PAST_RESERVATION", "message": "지난 시간에는 예약할 수 없습니다." }` |
| `CANNOT_MODIFY_OTHER_RESERVATION` | 403 Forbidden | `{ "code": "CANNOT_MODIFY_OTHER_RESERVATION", "message": "다른 사람 예약을 변경할 수 없습니다." }` |
| `CANNOT_DELETE_OTHER_RESERVATION` | 403 Forbidden | `{ "code": "CANNOT_DELETE_OTHER_RESERVATION", "message": "다른 사람 예약을 취소할 수 없습니다." }` |
| `RESERVATION_NOT_FOUND` | 404 Not Found | `{ "code": "RESERVATION_NOT_FOUND", "message": "예약을 찾을 수 없습니다." }` |
| `RESERVATION_ALREADY_EXISTS` | 409 Conflict | `{ "code": "RESERVATION_ALREADY_EXISTS", "message": "이미 예약이 존재합니다." }` |
| `CANNOT_DELETE_RESERVED_TIME` | 409 Conflict | `{ "code": "CANNOT_DELETE_RESERVED_TIME", "message": "이미 예약이 존재하는 시간대이므로 삭제할 수 없습니다." }` |
