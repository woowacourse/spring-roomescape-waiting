# API 명세

### 메인 페이지 조회

#### Request

```http request
GET /admin HTTP/1.1
```

#### Response

```
HTTP/1.1 200 
```

### 예약 페이지 조회

#### Request

```http request
GET /admin/reservation HTTP/1.1
```

#### Response

```
HTTP/1.1 200 
```

### 예약 목록 조회

#### Request

```http request
GET /reservations HTTP/1.1
```

#### Response

```
HTTP/1.1 200 
Content-Type: application/json

[
    {
        "id": 1,
        "name": "브라운",
        "date": "2023-01-01",
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
```

### 예약 단건 조회

#### Request

```http request
GET /reservations/{id} HTTP/1.1
```

#### Response

```http request
HTTP/1.1 200 
Content-Type: application/json

{
    "id": 1,
    "name": "브라운",
    "date": "2023-01-01",
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
```

### 예약 검색

#### Request

```http request
GET /reservations/search?themeId=themeId&memberId=memberId&dateFrom=dateFrom&dateTo=dateTo HTTP/1.1
```

#### Response

```
HTTP/1.1 200 
Content-Type: application/json

[
    {
        "id": 1,
        "name": "브라운",
        "date": "2023-01-01",
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
```

### 예약 추가 - 사용자

#### Request

```http request
POST /reservations HTTP/1.1
content-type: application/json
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
host: localhost:8080

{
    "date": "2023-08-05",
    "timeId": 1,
    "themeId": 1
}
```

#### Response

```
HTTP/1.1 201
Location: /reservation/{id}
```

### 예약 추가 - 관리자

#### Request

```http request
POST /admin/reservations HTTP/1.1
content-type: application/json
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
host: localhost:8080

{
    "date": "2024-03-01",
    "themeId": 1,
    "timeId": 1,
    "memberId": 1
}
```

#### Response

```
HTTP/1.1 201
Location: /reservation/{id}
```

### 예약 취소

#### Request

```http request
DELETE /reservations/{id} HTTP/1.1
```

#### Response

```
HTTP/1.1 204
```

---

### 시간 목록 조회

#### Request

```http request
GET /times HTTP/1.1
```

#### Response

```http request
HTTP/1.1 200
Content-Type: application/json

[
    {
        "id": 1,
        "startAt": "10:00"
    }
]
```

### 시간 조회

#### Request

```http request
GET /times/{id} HTTP/1.1
```

#### Response

```http request
HTTP/1.1 200
Content-Type: application/json

{
    "id": 1,
    "startAt": "10:00"
}
```

### 시간 추가

#### Request

```http request
POST /times HTTP/1.1
content-type: application/json

{
    "startAt": "10:00"
}
```

#### Response

```
HTTP/1.1 201
Location: /times/{id}
```

### 시간 삭제

#### Request

```http request
DELETE /times/1 HTTP/1.1
```

#### Response

```http request
HTTP/1.1 204
```

## 테마

### 테마 조회

#### Request

```
GET /themes HTTP/1.1
```

#### Response

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

### 테마 추가

#### Request

```
POST /themes HTTP/1.1
content-type: application/json

{
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

#### Response

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

### 테마 삭제

#### Request

```
DELETE /themes/1 HTTP/1.1
```

#### Response

```
HTTP/1.1 204
```

## 사용자 예약
### 사용자 예약 페이지 조회

#### Request

```http request
GET /reservation HTTP/1.1
```

#### Response

```
HTTP/1.1 200 
```

## 인기 테마
### 인기 테마 페이지 조회

#### Request

```http request
GET / HTTP/1.1
```

#### Response

```
HTTP/1.1 200 
```

### 인기 테마 상위 10개 조회

#### Request

```http request
GET /themes/popular HTTP/1.1
```

#### Response

```
[
    {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    }
]
```

## 예약 가능 시간
### 예약 가능 시간 조회

#### Request

```http request
GET /reservations/times HTTP/1.1
```

#### Response

```http request
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

## 로그인
### 로그인 페이지 조회

#### Request

```http request
GET /login HTTP/1.1
```

#### Response

```http request
HTTP/1.1 200 
```

### 로그인 요청

#### Request

```http request
POST /login HTTP/1.1
content-type: application/json
host: localhost:8080

{
"password": "password",
"email": "admin@email.com"
}
```

#### Response

```http request
HTTP/1.1 200 OK
Content-Type: application/json
Keep-Alive: timeout=60
Set-Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
```

### 인증 정보 조회

#### Request

```http request
GET /login/check HTTP/1.1
cookie: _ga=GA1.1.48222725.1666268105; _ga_QD3BVX7MKT=GS1.1.1687746261.15.1.1687747186.0.0.0; Idea-25a74f9c=3cbc3411-daca-48c1-8201-51bdcdd93164; token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```

#### Response

```http request
HTTP/1.1 200 OK
Connection: keep-alive
Content-Type: application/json
Date: Sun, 03 Mar 2024 19:16:56 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "name": "어드민"
}
```

### 회원 목록 조회

#### Request

```http request
GET /members HTTP/1.1
host: localhost:8080
```

#### Response

```http request
HTTP/1.1 200 OK
Connection: keep-alive
Content-Type: application/json
Date: Sun, 03 Mar 2024 19:16:56 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

[
    {
        "id": "1",
        "name": "어드민"
    }
]
```
