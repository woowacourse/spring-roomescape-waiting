# 방탈출 사용자 예약

### 요구사항

- [x] 화면 응답 파일을 수정한다.
- [x] API 명세를 작성한다.
- [x] 예약 대기 요청 기능을 구현한다.
  - [x] 예약 대기 요청은 중복이 불가하다.
  - [x] 예약 대기 요청은 예약과 중복이 불가하다.
- [x] 예약 대기 취소 기능을 구현한다.
  - [x] 해당 유저가 삭제할 수 있다.
- [ ] 내 예약 목록 조회 시 예약 대기 목록도 함께 포함한다.
  - [ ] 몇 번째 대기인지 함께 표기한다.

# 방탈출 API 명세

## 예약 조회

### Request

- GET /reservations

### Response

- 200 OK
- content-ype: application/json

``` json
[
    {
        "id": 1,
        "name": "브라운",
        "date": "2023-08-05",
        "time": {
            "id": 1,
            "startAt": "10:00"
        }
    }
]
```

---

## 나의 예약 조회

### Request

- GET /reservations/me
- cookie: token={token}

### Response

- 200 OK
- content-type: application/json

```json
[
  {
    "reservationId": 1,
    "theme": "테마1",
    "date": "2024-03-01",
    "time": "10:00",
    "status": "예약 완료"
  },
  {
    "reservationId": 2,
    "theme": "테마2",
    "date": "2024-03-01",
    "time": "12:00",
    "status": "대기"
  },
  {
    "reservationId": 3,
    "theme": "테마3",
    "date": "2024-03-01",
    "time": "14:00",
    "status": "예약 완료"
  }
]
```

---

## 예약 추가

### Request

- POST /reservations
- cookie: token={token}
- content-type: application/json

```json
{
  "date": "2023-08-05",
  "timeId": 1,
  "themeId": 1
}
```

### Response

- 201 Created
- Location: /reservations/1
- content-type: application/json

```json
{
  "id": 1,
  "member": {
    "id": 1,
    "name": "아루"
  },
  "date": "2024-12-25",
  "time": {
    "id": 1,
    "startAt": "10:00:00"
  },
  "theme": {
    "id": 1,
    "name": "우테코에 어서오세요",
    "description": "우테코를 탈출하세요",
    "thumbnail": "https://avatars.githubusercontent.com/u/0"
  }
}
```

---

## 예약 삭제

### Request

- DELETE /reservations/{id}

### Response

- 204 No Content

---

## 예약 대기 추가

### Request

- POST /reservations/waiting
- cookie: token={token}
- content-type: application/json

```json
{
  "date": "2023-08-05",
  "timeId": 1,
  "themeId": 1
}
```

### Response

- 201 Created
- Location: /reservations/waiting/1
- content-type: application/json

```json
{
  "id": 1,
  "member": {
    "id": 1,
    "name": "아루"
  },
  "date": "2024-12-25",
  "time": {
    "id": 1,
    "startAt": "10:00:00"
  },
  "theme": {
    "id": 1,
    "name": "우테코에 어서오세요",
    "description": "우테코를 탈출하세요",
    "thumbnail": "https://avatars.githubusercontent.com/u/0"
  }
}
```

---

## 예약 대기 삭제

### Request

- DELETE /reservations/waiting/{id}

### Response

- 204 No Content

---

## 시간 추가

### Request

- POST /times
- content-type: application/json

```json
{
  "startAt": "10:00"
}
```

### Response

- 201 Created
- Location: /times/1
- content-type: application/json

```json
{
  "id": 1,
  "startAt": "10:00"
}
```

---

## 시간 조회

### Request

- GET /times

### Response

- 200 OK
- content-type: application/json

```json
[
  {
    "id": 1,
    "startAt": "10:00"
  }
]
```

---

## 시간 삭제

### Request

- DELETE /times/{id}

### Response

- 204 No Content

---

## 테마 추가

### Request

- POST /themes
- Location: /themes/1
- content-type: application/json

```json
{
  "name": "테마",
  "description": "테마 설명",
  "thumbnail": "테마 이미지"
}
```

### Response

- 201 Created
- content-type: application/json

```json
{
  "id": 1,
  "name": "테마",
  "description": "테마 설명",
  "thumbnail": "테마 이미지"
}
```

---

## 테마 조회

### Request

- GET /themes

### Response

- 200 OK
- content-type: application/json

```json
[
  {
    "id": 1,
    "name": "테마",
    "description": "테마 설명",
    "thumbnail": "테마 이미지"
  }
]
```

---

## 테마 삭제

### Request

- DELETE /themes/{id}

### Response

- 204 No Content

---

## 인기 테마

### Request

- GET /themes/popular

### Response

- 200 OK
- content-type: application/json

```json
[
  {
    "id": 2,
    "name": "테마2",
    "description": "설명2",
    "thumbnail": "url2"
  },
  {
    "id": 1,
    "name": "테마1",
    "description": "설명1",
    "thumbnail": "url1"
  }
]
```

---

## 예약 가능 시간

### Request

- GET /times/available?date={date}&themeId={id}
- date: yyyy-MM-dd
- themeId: 테마 id

### Response

- 200 OK
- content-type: application/json

```json
[
  {
    "time": {
      "id": 1,
      "startAt": "10:00"
    },
    "isBooked": true
  },
  {
    "time": {
      "id": 2,
      "startAt": "11:00"
    },
    "isBooked": false
  }
]
```

### 회원 가입

- POST /members
- content-type: application/json

```json
{
  "name": "아루",
  "email": "test@aru.me",
  "password": "12341234"
}
```

### Response

- 200 OK
- content-type: application/json

```json
{
  "id": 1,
  "name": "아루"
}
```

### 로그인

- POST /login
- content-type: application/json

```json
{
  "email": "test@aru.me",
  "password": "12341234"
}
```

### Response

- 204 No Content
- Set-Cookie: token={token}; Path=/; HttpOnly

### 로그아웃

- POST /logout
- content-type: application/json

### Response

- 204 No Content
- Set-Cookie: token=; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:10 GMT; Path=/; HttpOnly


