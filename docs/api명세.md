## API 명세

### 예약 API

```http request
### 조회 요청
GET /reservations HTTP/1.1
Content-Type: application/json

### 조회 응답
HTTP/1.1 200 OK
Content-Type: application/json

[
{
   "id": 1,
   "name": "브라운",
   "date": "2026-05-03",
   "time": {
      "id": 1,
      "startAt": "10:00",
   },
   "theme": { 
      "id": 1,
      "name": "테마이름",
      "description": "설명",
      "thumbnailImageUrl": "썸네일이미지url"
   }
}
]

---

### 예약 신청 요청
POST /reservations HTTP/1.1
Content-Type: application/json

{
   "name": "브라운",
   "date": "2026-05-03",
   "timeId": 1,
   "themeId": 1
}

### 예약 신청 응답
HTTP/1.1 201 Created
Content-Type: application/json

{
   "id": 1,
   "name": "브라운",
   "date": "2026-05-03",
   "time": {
      "id": 1,
      "startAt": "10:00",
      "reserved" : true
   },
   "theme": { 
      "id": 1,
      "name": "테마이름",
      "description": "설명",
      "thumbnailImageUrl": "썸네일이미지url"
   }
   "status": "RESERVED", // 대기일 시 "WAITING"
   "waitingOrder": null, // 대기일 시 2
   "createdAt": "2026-05-26T12:30:00"
}

#### 대기 순번 정책
- 예약 신청 결과가 대기 상태인 경우 `waitingOrder`는 등록 직후 계산된 안내용 순번이다.
- 동시 요청 상황에서는 아직 반영되지 않은 다른 대기 데이터로 인해 생성 응답의 `waitingOrder`가 최종 순번과 일시적으로 다를 수 있다.
- 확정 대기 순번은 내 예약 조회 API 응답의 `waitingOrder`를 기준으로 한다.

---

### 삭제 요청
DELETE /reservations/{id} HTTP/1.1

### 삭제 응답
HTTP/1.1 204 No Content

---

```

### 예약 시간 API

```http request
### 조회 요청
GET /times HTTP/1.1
Content-Type: application/json

### 조회 응답
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "startAt": "15:30",
        "reserved" : false
    },
    {
        "id": 2,
        "startAt": "16:30",
        "reserved" : false
    }
]

---

### 생성 요청
POST /times HTTP/1.1
Content-Type: application/json

{
    "startAt": "15:30"
}

### 생성 응답
HTTP/1.1 201 Created
Content-Type: application/json

{
    "id": 1,
    "startAt": "15:30",
    "reserved" : false
}

---

### 삭제 요청
DELETE /times/{id} HTTP/1.1

### 삭제 응답
HTTP/1.1 204 No Content

---

### 사용자가 날짜·테마를 골라 예약 가능한 시간을 본다. -> 요청
GET /times?date=2026-05-03&themeId=1 HTTP/1.1

### 응답
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "id": 1,
    "startAt": 15:30",
    "reserved": false
  },
  {
    "id": 2,
    "startAt": 15:30",
    "reserved": true
  }
]
```

### 예약 대기 API

```http request
### 삭제 요청
DELETE /waitlists/{id} HTTP/1.1

### 삭제 응답
HTTP/1.1 204 No Content
```

### 테마 API

```http request
### 조회 요청
GET /themes HTTP/1.1
Content-Type: application/json

### 조회 응답
HTTP/1.1 200 OK
Content-Type: application/json

[
   {
      "id": 1,
      "name": "테마이름",
      "description": "설명",
      "thumbnailImageUrl": "썸네일이미지url"
   }
]

---

### 생성 요청
POST /themes HTTP/1.1
Content-Type: application/json

{
   "name": "테마이름",
   "description": "설명",
   "thumbnailImageUrl": "썸네일이미지url"
}

### 생성 응답
HTTP/1.1 201 Created
Content-Type: application/json

{
   "id": 1,
   "name": "테마이름",
   "description": "설명",
   "thumbnailImageUrl": "썸네일이미지url"
}

---

### 삭제 요청
DELETE /themes/1 HTTP/1.1

### 삭제 응답
HTTP/1.1 204 No Content

---

### 최근 1주 동안 예약이 많은 테마 상위 10개 -> 요청
GET /themes?days=7 HTTP/1.1

### 응답
HTTP/1.1 200 OK
Content-Type: application/json

[
  {
    "id": 1,
    "name": "방탈출 제목",
    "description": "테마 설명",
    "thumbnail": "https://example.com/theme1.png"
  },
  {
    "id": 2,
    "name": "공포의 방",
    "description": "공포 테마 설명",
    "thumbnail": "https://example.com/theme2.png"
  }
]

```

### 내 예약 조회 API

```http request
### 요청
GET /reservations?name=브라운 HTTP/1.1

### 응답
HTTP/1.1 200 OK
Content-Type: application/json

[
{
   "id": 1,
   "name": "브라운",
   "date": "2026-05-03",
   "time": {
      "id": 1,
      "startAt": "10:00",
      "reserved" : true
   },
   "theme": { 
      "id": 1,
      "name": "테마이름",
      "description": "설명",
      "thumbnailImageUrl": "썸네일이미지url"
   }
   "status": "RESERVED", // 대기일 시 "WAITING"
   "waitingOrder": null, // 대기일 시 2
   "createdAt": "2026-05-26T12:30:00"
}
]
```

### 내 예약 변경 API

```http request
### 요청
PATCH /reservations/{id}?name=브라운 HTTP/1.1
Content-Type: application/json

{
   "date": "2026-05-21",
   "timeId": 2
}

### 응답
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 1,
  "name": "브라운",
  "date": "2026-05-21",
  "time": {
    "id": 2,
    "startAt": "11:00",
    "reserved": true
  },
  "theme": {
    "id": 1,
    "name": "테마 이름",
    "description": "설명",
    "thumbnailImageUrl": "썸네일이미지url"
  }
}

#### 정책
- 변경으로 인해 대기자가 예약으로 자동 승격될 수 있지만, 승격 결과는 변경 응답에 포함하지 않는다.
- 사용자는 내 예약 조회 API(`GET /reservations?name={name}`)를 통해 변경된 예약/대기 상태를 확인한다.

```

### 내 예약 취소 API

```http request
### 요청
DELETE /reservations/{id}?name=브라운 HTTP/1.1

### 응답
HTTP/1.1 204 No Content

#### 정책
- 취소로 인해 대기자가 예약으로 자동 승격될 수 있지만, 승격 결과는 취소 응답에 포함하지 않는다.
- 사용자는 내 예약 조회 API(`GET /reservations?name={name}`)를 통해 변경된 예약/대기 상태를 확인한다.
```

### 에러 응답 명세

| 상황                                     | 상태 코드                     |
|----------------------------------------|---------------------------|
| 유효하지 않은 입력                             | 400 Bad Request           |
| 정책에 안 맞는 경우                            | 400 Bad Request           |
| 존재한 예약에서 사용자 이름이 일치하지 않는 경우 (권한이 없는 것) | 403 Forbidden             |
| 존재하지 않는 경우                             | 404 Not Found             |
| 중복된 경우                                 | 409 Conflict              |
| 서버 오류                                  | 500 Internal Server Error |

```json
{
  "type": "about:blank",
  "title": "잘못된 입력",
  "status": 400,
  "detail": "사용자가 이해할 수 있는 에러 메시지",
  "instance": "/요청/경로",
  "code": "INVALID_INPUT"
}
```

`code`는 도메인 예외와 입력값 검증 실패 응답에 포함된다. 예상하지 못한 서버 오류 응답에는 `code`가 포함되지 않을 수 있다.

---
