# 방탈출 예약 관리

## 🙋기능 요구사항

### 관리자 예약 관리

- 관리자는 예약을 예약자 이름, 예약 날짜, 예약 시간, 테마 정보를 입력해 예약을 추가할 수 있다
- 관리자는 전체 예약 목록을 조회할 수 있다
- 관리자는 특정 예약을 삭제할 수 있어야 한다.

### 관리자 예약 시간 관리

- 관리자는 시간을 입력해서 예약 시간을 등록할 수 있다.
- 관리자는 전체 예약 시간 목록을 조회할 수 있다.
- 관리자는 예약 시간을 삭제할 수 있다.

### 관리자 테마 관리

- 관리자는 테마의 이름, 설명, 썸네일 이미지 URL를 입력하여 등록할 수 있다.
- 관리자는 전체 테마 목록을 조회할 수 있다.
- 관리자는 특정 테마를 삭제할 수 있다.

### 사용자 예약

- 사용자는 특정 날짜 및 테마의 예약 가능한 시간을 조회할 수 있다.
- 사용자는 예약자 이름, 예약 날짜, 예약 시간, 테마 정보를 입력해서 예약을 할 수 있다.
- 사용자는 자신의 이름으로 본인의 예약 목록을 조회할 수 있다.
- 사용자는 자신의 예약의 날짜, 시간을 변경할 수 있다. (테마는 X)
- 사용자는 자신의 예약을 취소할 수 있다.
- 사용자는 이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에 대기를 신청할 수 있다.
- 같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다.

### 인기 테마

- 사용자는 최근 1주일 동안 예약이 많았던 테마 상위 10개를 확인할 수 있어야 한다.

## 📜 서비스 정책

### 예약

- 지나간 날짜, 시간에 대한 예약 생성/변경/삭제는 불가능하다
- 같은 날짜, 시간, 테마에 이미 예약이 있으면 예약할 수 없다.
- 사용자는 다른 사람의 예약을 변경/취소할 수 없다.
- 이미 취소된 예약은 변경/취소할 수 없다.

### 테마

- 모든 테마의 시작 시간과 소요 시간은 동일하다.
- 예약이 존재하는 테마는 삭제할 수 없다.
- 사용자는 테마를 추가/삭제할 수 없다.

### 예약 시간

- 예약이 존재하는 시간은 삭제할 수 없다.
- 사용자는 예약 시간을 추가/삭제할 수 없다.

### 예약 대기

- 같은 사용자가 같은 슬롯에 활성 상태(CONFIRMED/WAITING)로 중복 신청할 수 없다.
- 취소된 예약 이력은 같은 사용자의 같은 슬롯에 여러 번 남을 수 있다.
- 사용자는 본인의 대기를 취소할 수 있다.

## 🌐 API 명세

### 🔐 관리자 API

#### 예약

##### 관리자 예약 목록 조회 API ✅

**Request**

```http
GET /admin/reservations HTTP/1.1
```

**Response**

```http
HTTP/1.1 200
Content-Type: application/json

{
  "reservations": [
    {
      "id": 1,
      "guestName": "브라운",
      "date": "2023-08-05",
      "time": {
        "id": 1,
        "startAt": "10:00"
      },
      "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
      }
    }
  ]
}
```

##### 관리자 예약 삭제 API ✅

**Request**

```http
DELETE /admin/reservations/1 HTTP/1.1
```

**Response**

```http
HTTP/1.1 204
```

에러 상황:

| 에러 상황               | 상태코드  | 구현여부 |
|---------------------|-------|------|
| 유효하지 않는 입력값         | `400` | ✅    |
| 예약이 존재하지 않을 때       | `404` | ✅    |
| 이미 취소된 예약인 경우       | `409` |      |
| 이미 시작된 예약을 취소하려는 경우 | `422` |      |

#### 예약 시간

##### 관리자 예약 시간 추가 API ✅

**Request**

```http
POST /admin/times HTTP/1.1
Content-Type: application/json

{
  "startAt": "10:00"
}
```

**Response**

```http
HTTP/1.1 201
Content-Type: application/json

{
  "id": 1,
  "startAt": "10:00"
}
```

에러 상황:

| 에러 상황        | 상태코드  | 구현여부 |
|--------------|-------|------|
| 유효하지 않는 입력값  | `400` | ✅    |
| 시간이 이미 존재할 때 | `409` | ✅    |

##### 관리자 예약 시간 삭제 API ✅

**Request**

```http
DELETE /admin/times/1 HTTP/1.1
```

**Response**

```http
HTTP/1.1 204
```

에러 상황:

| 에러 상황              | 상태코드  | 구현여부 |
|--------------------|-------|------|
| 유효하지 않는 입력값        | `400` | ✅    |
| 시간이 존재하지 않을 때      | `404` | ✅    |
| 해당 시간으로 된 예약이 있을 때 | `409` | ✅    |

#### 테마

##### 관리자 테마 추가 API ✅

**Request**

```http
POST /admin/themes HTTP/1.1
Content-Type: application/json

{
  "name": "공포",
  "description": "무서움",
  "thumbnail": "url"
}
```

**Response**

```http
HTTP/1.1 201
Content-Type: application/json

{
  "id": 1,
  "name": "공포",
  "description": "무서움",
  "thumbnail": "url"
}
```

에러 상황:

| 에러 상황       | 상태코드  | 구현여부 |
|-------------|-------|------|
| 유효하지 않는 입력값 | `400` | ✅    |

##### 관리자 테마 삭제 API ✅

**Request**

```http
DELETE /admin/themes/1 HTTP/1.1
```

**Response**

```http
HTTP/1.1 204
```

에러 상황:

| 에러 상황             | 상태코드  | 구현여부 |
|-------------------|-------|------|
| 유효하지 않는 입력값       | `400` | ✅    |
| 테마가 존재하지 않을 때     | `404` | ✅    |
| 해당 테마로 된 예약이 있을 때 | `409` | ✅    |

### 👤 사용자 API

#### 예약

##### 예약 추가 API

**Request**

```http
POST /reservations HTTP/1.1
Content-Type: application/json

{
  "date": "2023-08-05",
  "guestName": "브라운",
  "timeId": 1,
  "themeId": 1
}
```

**Response**

```http
HTTP/1.1 201
Content-Type: application/json

{
  "id": 1,
  "guestName": "브라운",
  "date": "2023-08-05",
  "time": {
    "id": 1,
    "startAt": "10:00"
  },
  "theme": {
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  },
  "status": "WAITING",
  "waitNumber": 5
}
```

에러 상황:

| 에러 상황                            | 상태코드  | 구현여부 |
|----------------------------------|-------|------|
| 유효하지 않는 입력값                      | `400` | ✅    |
| 시간이 존재하지 않을 때                    | `404` | ✅    |
| 테마가 존재하지 않을 때                    | `404` | ✅    |
| 이미 해당 날짜, 시간, 테마에 이미 대기를 걸어놓은 경우 | `409` |      |
| 이미 지난 시간에 예약하려는 경우               | `422` | ✅    |

##### 사용자의 활성 예약 목록 조회 API

**Request**

```http
GET /reservations/me/active?guestName=브라운 HTTP/1.1
```

**Response**

```http
HTTP/1.1 200
Content-Type: application/json

{
  "reservations": [
    {
      "id": 1,
      "guestName": "브라운",
      "date": "2023-08-05",
      "time": {
        "id": 1,
        "startAt": "10:00"
      },
      "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
      },
        "status": 'WAITING',
        "waitNumber": 5
    }
  ]
  
}
```

에러 상황:

| 에러 상황                     | 상태코드  | 구현여부 |
|---------------------------|-------|------|
| 유효하지 않는 입력값(guestName 누락) | `400` | ✅    |

##### 사용자의 모든 예약 목록 조회 API

**Request**

```http
GET /reservations/me?guestName=브라운 HTTP/1.1
```

**Response**

```http
HTTP/1.1 200
Content-Type: application/json

{
  "reservations": [
    {
      "id": 1,
      "guestName": "브라운",
      "date": "2023-08-05",
      "time": {
        "id": 1,
        "startAt": "10:00"
      },
      "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
      },
        "status": 'CANCELLED',
        "waitNumber": 5
    }
  ]
  
}
```

에러 상황:

| 에러 상황                     | 상태코드  | 구현여부 |
|---------------------------|-------|------|
| 유효하지 않는 입력값(guestName 누락) | `400` | ✅    |

##### 예약 수정 API

> 예약 날짜, 시간을 변경한다. (테마 X)

**Request**

```http
PATCH /reservations/1 HTTP/1.1
Content-Type: application/json

{
  "date": "2023-08-05",
  "timeId": 1
}
```

**Response**

```http
HTTP/1.1 200
Content-Type: application/json

{
  "id": 1,
  "guestName": "브라운",
  "date": "2023-08-05",
  "time": {
    "id": 1,
    "startAt": "10:00"
  },
  "theme": {
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  },
    "isWaited": true,
    "waitNumber": 5
}
```

에러 상황:

| 에러 상황               | 상태코드  | 구현여부 |
|---------------------|-------|------|
| 유효하지 않는 입력값         | `400` | ✅    |
| 본인의 예약이 아닌 경우       | `403` | ✅    |
| 예약 시간이 존재하지 않을 때    | `404` | ✅    |
| 예약이 존재하지 않을 때       | `404` | ✅    |
| 이미 취소된 예약인 경우       | `409` |      |
| 해당 시간에 이미 예약이 있는 경우 | `409` | ✅    |
| 이미 지난 시간에 예약하려는 경우  | `422` | ✅    |
| 이미 시작된 예약을 수정하려는 경우 | `422` | ✅    |

##### 예약 삭제 API ✅

**Request**

```http
DELETE /reservations/1 HTTP/1.1
```

**Response**

```http
HTTP/1.1 204
```

에러 상황:

| 에러 상황               | 상태코드  | 구현여부 |
|---------------------|-------|------|
| 유효하지 않는 입력값         | `400` | ✅    |
| 본인의 예약이 아닌 경우       | `403` | ✅    |
| 예약이 존재하지 않을 때       | `404` | ✅    |
| 이미 취소된 예약인 경우       | `409` |      |
| 이미 시작된 예약을 취소하려는 경우 | `422` | ✅    |

#### 예약 시간

##### 예약 시간 목록 조회 API ✅

**Request**

```http
GET /times HTTP/1.1
```

**Response**

```http
HTTP/1.1 200
Content-Type: application/json

{
  "times": [
    {
      "id": 1,
      "startAt": "10:00"
    }
  ]
}
```

##### 예약 가능 시간 조회 API ✅

**Request**

```http
GET /times/availability?date=2026-05-04&themeId=1 HTTP/1.1
```

**Response**

```http
HTTP/1.1 200
Content-Type: application/json

{
  "availableTimes": [
    {
      "id": 1,
      "startAt": "10:00",
      "isAvailable": true
    }
  ]
}
```

에러 상황:

| 에러 상황       | 상태코드  | 구현여부 |
|-------------|-------|------|
| 유효하지 않는 입력값 | `400` | ✅    |

#### 테마

##### 테마 목록 조회 API ✅

**Request**

```http
GET /themes HTTP/1.1
```

**Response**

```http
HTTP/1.1 200
Content-Type: application/json

{
  "themes": [
    {
      "id": 1,
      "name": "공포",
      "description": "무서움",
      "thumbnail": "url"
    }
  ]
}
```

##### 인기 테마 조회 API ✅

**Request**

```http
GET /themes/popularity?days=7&size=10 HTTP/1.1
```

**Response**

```http
HTTP/1.1 200
Content-Type: application/json

{
  "themes": [
    {
      "id": 1,
      "name": "공포",
      "description": "무서움",
      "thumbnail": "url"
    }
  ]
}
```

에러 상황:

| 에러 상황         | 상태코드  | 구현여부 |
|---------------|-------|------|
| 유효하지 않는 입력값 ㅋ | `400` | ✅    |

## 🖥️ 프론트엔드 페이지

### 사용자 예약 페이지

- 경로: `http://localhost:8080`
- 사용자는 인기 테마를 확인하고, 날짜와 테마를 선택해 예약 가능한 시간을 조회할 수 있다.
- 사용자는 예약 가능한 시간을 선택한 뒤 이름을 입력해 예약할 수 있다.
- 사용자는 예약자 이름으로 본인의 예약 목록을 조회할 수 있다.
- 사용자는 본인의 예약에 한해 예약 날짜와 시간을 수정할 수 있다.
- 사용자는 본인의 예약에 한해 예약을 취소할 수 있다.

### 관리자 페이지

- 경로: `http://localhost:8080/admin`
- 관리자는 사용자 예약 화면과 같은 흐름으로 날짜, 테마, 예약 가능한 시간, 이름을 입력해 예약을 등록할 수 있다.
- 관리자는 예약 목록을 조회하고 예약을 삭제할 수 있다.
- 관리자는 예약 시간 목록을 조회하고 예약 시간을 등록하거나 삭제할 수 있다.
- 관리자는 테마 목록을 조회하고 테마를 등록하거나 삭제할 수 있다.
