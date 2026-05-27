## 사이클 1 기능목록

### 테마 도메인

* [x] 테마 도메인 추가
    * [x] 테마는 이름, 설명, 썸네일 이미지 url을 가진다
    * 모든 테마의 시작 시간과 소요 시간은 동일하다고 가정한다
* [x] 예약에 테마 정보가 포함 되도록 수정
* [x] 관리자의 테마 추가, 삭제

### 사용자 예약

* [x] 사용자의 날짜, 테마 선택 -> 예약 가능한 시간 표시
    * [x] 예약 가능한 시간이란 관리자가 등록한 시간 중 해당 날짜+테마에 아직 예약이 없는 시간이다
* [x] 사용자가 본인의 이름으로 예약 가능한 시간으로 예약
* [x] 같은 날짜, 시간이라도 테마가 다르면 각각 예약 가능

### 인기 테마 조회

* [x] 최근 1주일동안 예약이 많았던 테마 상위 10개 조회
    * [x] 최근 1주일을 이전 날 기준으로 조회한다

### 추가 요구사항

* [x] `dev-data.sql`로 테스트용 데이터 넣어서 인기 테마 조회 결과 검증

### 화면

* [x] 사용자가 보는 화면 출력
    * [x] 브라우저에서 정상 동작 확인까지만 한다

---

## 사이클 2 기능목록

### 예약 생성 정책 검증

> 다음 정책을 만족하지 않는 요청은 거부한다.

* [x] 지나간 날짜/시간에 대한 예약 생성은 거부한다.
* [x] 같은 날짜/시간/테마에 이미 예약이 있으면 중복 예약을 거부한다.
* [x] 없는 예약은 삭제할 수 없다.
* 유효하지 않은 입력값은 거부
    * [x] 이름이 비어 있으면 거부
    * [x] 날짜 형식이 올바르지 않으면 거부
    * [x] 시간 ID, 테마 ID가 null이면 거부
    * [x] 시간 ID, 테마 ID 존재하지 않으면 거부

### 예약 시간/ 테마 정책 검증

* [x] 예약이 존재하는 시간, 테마를 삭제할 수 없다.
* [x] 없는 시간/테마를 삭제할 수 없다.
* 유효하지 않은 입력값은 거부 (테마)
    * [x] 이름이 비어 있으면 거부
    * [x] 설명이 비어 있으면 거부
    * [x] 썸네일 이미지가 비어 있으면 거부

### 에러 응답 설계

* [x] 예외 처리를 일관되게 처리하는 전역 예외 처리기 구현.
* [x] 서비스 정책 위반, 유효하지 않은 입력, 존재하지 않는 리소스 등에 대해 의도된 에러 응답을 반환.
* [x] 500(서버 에러)이 사용자에게 노출되지 않도록 한다.
* [x] 에러 응답 본문에 요청이 실패한 이유와 잘못된 조건을 명확히 담는다.
* [x] 브라우저 화면에서 에러 발생 시 응답 메시지를 사용자에게 보여준다.

### 내 예약 조회

* [x] 사용자가 자신의 이름으로 본인의 예약 목록을 조회할 수 있다.

### 내 예약 변경

* [x] 사용자가 본인의 예약의 날짜/시간을 변경할 수 있다.
    * 변경 성공 시 예약의 날짜와 시간을 수정한다.
* [x] 존재하지 않는 예약이면 거부한다.
* [x] 사용자 이름이 일치하지 않으면 변경 거부.
* [x] 이미 지난 예약은 변경할 수 없다.
* [x] 변경하려는 날짜/시간이 과거이면 거부한다.
* [x] 변경할 예약 시간이 차 있으면 거부한다.
* [x] 존재하지 않는 시간 ID로 변경하는 요청을 거부한다.

### 내 예약 취소

* [x] 사용자가 본인의 예약을 취소할 수 있다.
    * [x] 취소 성공 시 예약을 삭제.
* [x] 존재하지 않는 예약이면 거부한다.
* [x] 사용자 이름이 일치하지 않으면 취소 거부.
* [x] 이미 지난 예약은 취소할 수 없다.

---

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

### 생성 요청
POST /reservations HTTP/1.1
Content-Type: application/json

{
   "name": "브라운",
   "date": "2026-05-03",
   "timeId": 1,
   "themeId": 1
}

### 생성 응답
HTTP/1.1 201 Created
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
}
]

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
```

### 내 예약 취소 API

```http request
### 요청
DELETE /reservations/{id}?name=브라운 HTTP/1.1

### 응답
HTTP/1.1 204 No Content
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
  "message": "사용자가 이해할 수 있는 에러 메시지"
}
```

---
