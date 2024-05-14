# 방탈출 예약 관리

## API 명세

| Role     | Method | Endpoint                                                | Description           | File Path                              | Controller Type   |
|----------|--------|---------------------------------------------------------|-----------------------|----------------------------------------|-------------------|
|          | GET    | `/`                                                     | 인기 테마 페이지 요청          | `templates/index.html`                 | `@Controller`     |
|          | GET    | `/reservation`                                          | 사용자 예약 페이지 요청         | `templates/reservation.html`           | `@Controller`     |
| `ADMIN`  | GET    | `/admin`                                                | 어드민 페이지 요청            | `templates/admin/index.html`           | `@Controller`     |
| `ADMIN`  | GET    | `/admin/reservation`                                    | 예약 관리 페이지 요청          | `templates/admin/reservation-new.html` | `@Controller`     |
| `ADMIN`  | GET    | `/admin/reservationTime`                                | 예약 시간 관리 페이지 요청       | `templates/admin/reservationTime.html` | `@Controller`     |
| `ADMIN`  | GET    | `/admin/theme`                                          | 테마 관리 페이지 요청          | `templates/admin/theme.html`           | `@Controller`     |
|          | GET    | `/login`                                                | 로그인 페이지 요청            | `templates/login.html`                 | `@Controller`     |
|          | POST   | `/login`                                                | 로그인 요청                |                                        | `@RestController` |
|          | GET    | `/login/check`                                          | 인증 정보 조회              |                                        | `@RestController` |
|          | GET    | `/token-reissue`                                        | JWT 토큰 재발급            |                                        | `@RestController` |
|          | GET    | `/reservations`                                         | 예약 정보 조회              |                                        | `@RestController` |
|          | GET    | `/reservations/search?themeId&memberId&dateFrom&dateTo` | 예약 정보 조건 검색           |                                        | `@RestController` |
|          | GET    | `/reservations/themes/{themeId}/reservationTimes?date`  | 특정 날짜의 특정 테마 예약 정보 조회 |                                        | `@RestController` |
| `MEMBER` | POST   | `/reservations`                                         | 예약 추가                 |                                        | `@RestController` |
|          | DELETE | `/reservations/{id}`                                    | 예약 취소                 |                                        | `@RestController` |
|          | GET    | `/reservationTimes`                                     | 예약 시간 조회              |                                        | `@RestController` |
|          | DELETE | `/reservationTimes/{id}`                                | 예약 시간 추가              |                                        | `@RestController` |
|          | POST   | `/reservationTimes`                                     | 예약 시간 삭제              |                                        | `@RestController` |
|          | GET    | `/themes`                                               | 테마 정보 조회              |                                        | `@RestController` |
|          | GET    | `/themes/top?today`                                     | 특정 기간의 인기 테마 조회       |                                        | `@RestController` |
|          | POST   | `/themes`                                               | 테마 추가                 |                                        | `@RestController` |
|          | DELETE | `/themes/{id}`                                          | 테마 삭제                 |                                        | `@RestController` |

---

### 로그인 요청 API

- Request

```
POST /login HTTP/1.1
Content-Type: application/json

{
        "password": "password",
        "email": "admin@email.com"
}
```

- Response

```
HTTP/1.1 200 
Content-Type: application/json
Keep-Alive: timeout=60
Set-Cookie: accessToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
Set-Cookie: refreshToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
```

---

### JWT 토큰 재발급 API

- Request

```
GET /token-reissue HTTP/1.1
Cookie:
accessToken=eyJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJJZCI6MSwiaWF0IjoxNzE1NjE1OTMyLCJleHAiOjE3MTU2MTc3MzJ9.nfu6IZlKBccnmBbMtKDTP-5TbNWUMhcVY_ee09aNwhE; 
refreshToken=eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3MTU2MTU5MzIsImV4cCI6MTcxNTYxNzczMn0.U0ZhUSmvOjCAAKD6-9F8dYO1K-LskyxPnMYe7ZJGaQA
```

- Response

```
HTTP/1.1 200 
Content-Type: application/json
Keep-Alive: timeout=60
Set-Cookie: accessToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
Set-Cookie: refreshToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
```

---

### 인증 정보 조회 API

- Request

```
GET /login/check HTTP/1.1
Cookie: _ga=GA1.1.48222725.1666268105; _ga_QD3BVX7MKT=GS1.1.1687746261.15.1.1687747186.0.0.0; Idea-25a74f9c=3cbc3411-daca-48c1-8201-51bdcdd93164; 
accessToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM;
```

- Response

```
HTTP/1.1 200 OK
Connection: keep-alive
Content-Type: application/json
Date: Sun, 03 Mar 2024 19:16:56 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

"data": {
    "name": "이름"
}
```

---

### 예약 정보 조회 API

- Request

```
GET /reservations HTTP/1.1
```

- Response

```
HTTP/1.1 200 
Content-Type: application/json

[
    {
        "id": 1,
        "name": "브라운",
        "date": "2023-08-05",
        "reservationTime": {
            "id": 1,
            "startAt": "10:00"
        }
    }
]
```

---

### 예약 정보 조회 API

- Request

```
GET /reservations/search?themeId=1&memberId=1&dateFrom='2024-05-05'&dateTo='2024-05-08' HTTP/1.1
GET /reservations/search?themeId=1&memberId=1&dateFrom='2024-05-05' HTTP/1.1
GET /reservations/search?themeId=1&memberId=1 HTTP/1.1
GET /reservations/search?themeId=1 HTTP/1.1
GET /reservations/search HTTP/1.1
```

- Response

```
HTTP/1.1 200 
Content-Type: application/json

[
    {
        "id": 1,
        "name": "브라운",
        "date": "2023-08-05",
        "reservationTime": {
            "id": 1,
            "startAt": "10:00"
        }
    }
]
```

---

### 특정 날짜의 특정 테마 예약 정보 조회

- Request

```
GET /reservations/themes/1/reservationTimes?date=2024-12-31 HTTP/1.1
```

---

- Response

```
[
    {
        "timeId": 1,
        "startAt": "17:00",
        "alreadyBooked": false
    },
    {
        "timeId": 2,
        "startAt": "20:00",
        "alreadyBooked": false
    }
]
```

---

### 예약 추가 API

- Request

```
POST /reservations HTTP/1.1
content-type: application/json

{
    "date": "2023-08-05",
    "name": "브라운",
    "timeId": 1
}
```

- Response

```
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "name": "브라운",
    "date": "2023-08-05",
    "reservationTime" : {
        "id": 1,
        "startAt" : "10:00"
    }
}
```

---

### 예약 취소 API

- Request

```
DELETE /reservations/1 HTTP/1.1
```

- Response

```
HTTP/1.1 204
```

---

### 예약 시간 조회 API

- Request

```
GET /reservationTimes HTTP/1.1
```

- Response

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

---

### 예약 시간 추가 API

- Request

```
POST /reservationTimes HTTP/1.1
content-type: application/json

{
    "startAt": "10:00"
}
```

- Response

```
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "startAt": "10:00"
}
```

---

### 예약 시간 삭제 API

- Request

```
DELETE /reservationTimes/1 HTTP/1.1
```

- Response

```
HTTP/1.1 204
```

---

### 테마 정보 조회 API

- Request

```
GET /themes HTTP/1.1
```

- response

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

---

### 특정 기간의 인기 테마 조회 API

- Request

```
GET /themes/top?today=2024-01-01 HTTP/1.1
```

- response

```
HTTP/1.1 200
Content-Type: application/json

[
    {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    },
    ...8개 생략
    {
        "id": 10,
        "name": "레벨10 탈출",
        "description": "우테코 레벨10를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    }
]
```

---

### 테마 추가 API

- Request

```
POST /themes HTTP/1.1
content-type: application/json

{
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

- response

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

---

### 테마 삭제 API

- Request

```
DELETE /themes/1 HTTP/1.1
```

- response

```
HTTP/1.1 204
```
