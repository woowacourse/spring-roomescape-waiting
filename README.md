
### Http 명세

**Request**

#### 로그인 페이지 요청

```http request
GET /login
```

**Response**

```http request
templates/login.html
```

---

### 로그인 API

**Request**

```http request
POST /login HTTP/1.1
content-type: application/json

{
    "password": "password",
    "email": "admin@email.com"
}
```

**Response**

```http request
HTTP/1.1 200 OK
Content-Type: application/json
Set-Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly

```

---

#### 인증 정보 조회 API

**Request**

```http request
GET /login/check HTTP/1.1
cookie: _ga=GA1.1.48222725.1666268105; _ga_QD3BVX7MKT=GS1.1.1687746261.15.1.1687747186.0.0.0; Idea-25a74f9c=3cbc3411-daca-48c1-8201-51bdcdd93164; token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM

```

**Response**

```http request
HTTP/1.1 200 OK


{
    "name": "어드민"
}

```

#### 회원가입 API

**Request**

```http request
POST /signup HTTP/1.1
Content-Type: application/json

{ 
    "name": "hello",
    "email": "admin@email.com",
    "password": "password"
}
```

**Response**

```http request
HTTP/1.1 200 OK


{
    "name": "어드민"
}

```

### 사용자 예약 추가 API

**Request**

```http request
POST /reservations HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
content-type: application/json
host: localhost:8080

{
    "date": "2023-08-05",
    "memberId": 1,
    "timeId": 1,
    "themeId": 1
}
```

**Response**

```http request
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "date": "2023-08-05",
    "time" : {
        "id": 1,
        "startAt" : "10:00"
    },
    "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    }
}
```

### 관리자 예약 추가 API

**Request**

```http request
POST /admin/reservations HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
content-type: application/json
host: localhost:8080

{
    "memberId": 1
    "date": "2023-08-05",
    "timeId": 1,
    "themeId": 1
}
```

**Response**

```http request
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "date": "2023-08-05",
    "time" : {
        "id": 1,
        "startAt" : "10:00"
    },
    "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    }
}
```
### 모든 사용자 조회 API
**request**
```http request
GET /members
```

**response**
```http request
HTTP/1.1 200

[
    {
        "name": "name"
    }
]
```
### 회원가입 요청 API
**request**
```http request
POST /members
Content-Type: application/json

{
"name": "name",
"email": "email@email.com",
"password": "password"
}

```

**response**
```http request
<Response body is empty>Response code: 201; Time: 33ms (33 ms); Content length: 0 bytes (0 B)

```
### 로그아웃 API
**request**
```http request
POST /logout 
```

**response**
```http request
<Response body is empty>Response code: 200; Time: 33ms (33 ms); Content length: 0 bytes (0 B)

```
### 회원 삭제 API
**request**
```http request
DELETE members/1
```

**response**
```http request
<Response body is empty>Response code: 204; Time: 33ms (33 ms); Content length: 0 bytes (0 B)

```

### 관리자페이지 예약 검색 API
**request**
```http request
GET /reservations/search?name={fram}&theme={horror}&from={2024.05.12}&to={2024.06.01}
```

**response**
```http request
HTTP/1.1 200
{
    [
        {
            "id": 1,
            "date": "2023-08-05",
            "time" : {
                "id": 1,
                "startAt" : "10:00"
             },
             "theme": {
                "id": 1,
                "name": "레벨2 탈출",
                "description": "우테코 레벨2를 탈출하는 내용입니다.",
                "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
             }
         },
    ]
}
```


### 내 예약 목록 조회 API
**request**
```http request
GET /reservations-mine HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```

**response**
```http request
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
