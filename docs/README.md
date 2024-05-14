# 기능 구현

## 1단계

- [ ] gradle 의존성을 추가한다
- [ ] 엔티티를 매핑한다
- [ ] 연관관계를 매핑한다

## 2단계

- [ ] 내 예약 목록 조회 API를 구현한다
- [ ] 내 예약 확인 페이지를 제공한다
    - `GET /reservation-mine` 요청시 `reservation-mine.html` 페이지 응답

# API 명세

## 인증

### 로그인 API

Request

```
POST /login
Content-Type: application/json

{
    "password": "password",
    "email": "admin@email.com"
}
```

Response

```
HTTP/1.1 200 OK
Content-Type: application/json
Set-Cookie: token=hello.example.token; Path=/; HttpOnly
```

### 로그아웃 API

Request

```
POST /logout
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 200 OK
```

### 인증 정보 조회 API

Request

```
GET /login/check
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 200 OK
Content-Type: application/json

{
    "name": "어드민"
}
```

### 회원가입 API

Request

```
POST /signup
Content-Type: application/json

{
    "email": "admin@email.com",
    "password": "password",
    "name": "어드민"
}
```

Response

```
HTTP/1.1 200 OK
Content-Type: application/json

{
    "id": 1,
    "name": "브라운",
    "email": "admin@email.com"
}
```

## 사용자

### 사용자 목록 조회 API (접근 권한: 관리자)

Request

```
GET /members
```

Response

```
HTTP/1.1 200
Cookie: token=hello.example.token
Content-Type: application/json

[
    {
        "id": 1,
        "name": "브라운",
        "email": "admin@email.com"
    }
]
```

## 예약

### 예약 목록 조회 API (접근 권한: 관리자)

Request

```
GET /reservations/search?theme-id={$}&member-id={$}&date-from={$}&date-to={$}
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 200
Content-Type: application/json

[
    {
        "id": 1,
        "name": "브라운",
        "date": "2023-08-05",
        "time": {
            "id": 1,
            "startAt": "10:00"
        },
        "theme": {
            "id": 1,
            "name": "레벨2 탈출"
        }
    }
]
```

### 내 예약 목록 조회 API

Request

```
GET /reservations-mine
cookie: token=hello.example.token
```

Response

```
HTTP/1.1 200 
Content-Type: application/json

[
    {
        "reservationId": 1,
        "theme": "테마1",
        "date": "2024-03-01",
        "time": "10:00",
        "status": "예약"
    },
    {
        "reservationId": 2,
        "theme": "테마2",
        "date": "2024-03-01",
        "time": "12:00",
        "status": "예약"
    },
    {
        "reservationId": 3,
        "theme": "테마3",
        "date": "2024-03-01",
        "time": "14:00",
        "status": "예약"
    }
]
```

### 예약 추가 API

Request

```
POST /reservations
Cookie: token=hello.example.token
Content-Type: application/json

{
    "date": "2023-08-05",
    "themeId": 1,
    "timeId": 1
}
```

Response

```
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "name": "브라운",
    "date": "2023-08-05",
    "time": {
        "id": 1,
        "startAt": "10:00"
    },
    "theme": {
        "id": 1,
        "name": "레벨2 탈출"
    }
}
```

### 관리자용 예약 추가 API (접근 권한: 관리자)

Request

```
POST /admin/reservations
Cookie: token=hello.example.token
Content-Type: application/json

{
    "date": "2023-08-05", 
    "themeId": 1,
    "timeId": 1,
    "memberId": 1
}
```

Response

```
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "name": "브라운",
    "date": "2023-08-05",
    "time": {
        "id": 1,
        "startAt": "10:00"
    },
    "theme": {
        "id": 1,
        "name": "레벨2 탈출"
    }
}
```

### 예약 삭제 API (접근 권한: 관리자)

Request

```
DELETE /reservations/1
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 204
```

## 예약 시간

### 예약 시간 목록 조회 API (접근 권한: 관리자)

Request

```
GET /times
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 200 
Content-Type: application/json

[
   {
        "id": 1,
        "startAt": "10:00"
    }
]
```

### 예약 가능 시간 목록 조회 API

Request

```
GET /times/available?date={&}&time-id={$}
```

Response

```
HTTP/1.1 200 
Content-Type: application/json

[
   {
        "id": 1,
        "startAt": "10:00",
        "alreadyBooked": true
    }
]
```

### 예약 시간 추가 API (접근 권한: 관리자)

Request

```
POST /times
Cookie: token=hello.example.token
Content-Type: application/json

{
    "startAt": "10:00"
}
```

Response

```
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "startAt": "10:00"
}
```

### 예약 시간 삭제 API (접근 권한: 관리자)

Request

```
DELETE /times/1
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 204
```

## 테마

### 테마 목록 조회 API

Request

```
GET /themes
```

Response

```
HTTP/1.1 200 
Content-Type: application/json

[
   {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    }
]
```

### 인기 테마 목록 조회 API

Request

```
GET /themes/popular
```

Response

```
HTTP/1.1 200
Content-Type: application/json

[
   {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    }
]
```

### 테마 추가 API (접근 권한: 관리자)

Request

```
POST /themes
Cookie: token=hello.example.token
Content-Type: application/json

{
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

Response

```
HTTP/1.1 201
Location: /themes/1
Content-Type: application/json

{
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}

```

### 테마 삭제 API (접근 권한: 관리자)

Request

```
DELETE /themes/1
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 204
```
