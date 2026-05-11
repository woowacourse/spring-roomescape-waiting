## 기능 요구 사항
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
- [x]  사용자가 날짜와 테마를 선택해 예약 가능한 시간 목록이 표시된다.
      - 예약 가능한 시간이란, 관리자가 등록한 시간 중 해당 날짜+테마에 아직 예약이 없는 시간이다.
- [x]  사용자가 예약 가능한 시간을 선택하여 본인의 이름으로 예약한다.
      - 같은 날짜/시간이라도 테마가 다르면 예약 가능하다.

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
| 예약 생성 | POST | `/user/reservations`                                                          | 예약 생성 |
| 예약 삭제 | DELETE | `/user/reservations/{id}`                                                     | 예약 삭제 |
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
| 500 Internal Server Error | 예약 불가 등 서버 처리 실패 |