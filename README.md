# API 명세

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

### 로그아웃 API

**Request**

```http request
POST /logout HTTP/1.1
```

**Response**

```http request
HTTP/1.1 200 OK
Set-Cookie: token=

```

---

#### 인증 정보 조회 API

**Request**

```http request
GET /login/check HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJlbWFpbCI6InBrcGtwa3BrQHdvb3dhLm5ldCJ9.L4z9728LeGazM5MsP1iSM2QLB22NCLAdZ0eiQ1zt6EU

```

**Response**

```http request
HTTP/1.1 200 OK
Content-Type: application/json

{
    "id": 1,
    "name": "어드민"
}

```

---

#### 회원가입 API

**Request**

```http request
POST /members HTTP/1.1
Content-Type: application/json

{ 
    "name": "hello",
    "email": "admin@email.com",
    "password": "password"
}
```

**Response**

```http request
HTTP/1.1 201 Created
Location: /members/1

```

---

#### 사용자 조회 API

**Request**

```http request
GET /members HTTP/1.1
Content-Type: application/json

```

**Response**

```http request
HTTP/1.1 200 OK
content-type: application/json

[
    {
        "id": 1,
        "name": "mrmrmrmr"
    },
    {
        "id": 2,
        "name": "mangcho"
    }
]

```

---

#### 사용자 탈퇴 API

**Request**

```http request
DELETE /members/id HTTP/1.1

```

**Response**

```http request
HTTP/1.1 204 No Content

```

---

### 사용자 예약 추가 API

**Request**

```http request
POST /reservations HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
content-type: application/json

{
    "date": "2023-08-05",
    "timeId": 1,
    "themeId": 1
}

```

**Response**

```http request
HTTP/1.1 201 Created
Content-Type: application/json

{
    "id": 1,
    "date": "2024-05-24",
    "time": {
        "id": 1,
        "startAt": "10:00"
    },
    "theme": {
        "id": 1,
        "name": "Theme 1",
        "description": "Description 1",
        "thumbnail": "https://www.google.jpg"
    },
    "member": {
        "id": 2,
        "name": "망쵸"
    },
    "status": "RESERVED"
}

```

---

### 모든 예약 조회 API

**Request**

```http request
GET /reservations HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
content-type: application/json

```

**Response**

```http request
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "date": "2024-05-24",
        "time": {
            "id": 1,
            "startAt": "10:00"
        },
        "theme": {
            "id": 1,
            "name": "Theme 1",
            "description": "Description 1",
            "thumbnail": "https://www.google.jpg"
        },
        "member": {
            "id": 2,
            "name": "망쵸"
        },
        "status": "RESERVED"
    }
]

```

---

### 예약 조건에 따라 조회 API

**Request**

```http request
GET /reservations?start=2024-06-12&end=2024-06-13&memberId=1&themeId=1 HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
content-type: application/json

```

**Response**

```http request
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "date": "2024-06-12",
        "time": {
            "id": 1,
            "startAt": "10:00"
        },
        "theme": {
            "id": 1,
            "name": "Theme 1",
            "description": "Description 1",
            "thumbnail": "https://www.google.jpg"
        },
        "member": {
            "id": 1,
            "name": "망쵸"
        },
        "status": "RESERVED"
    }
]

```

---

### 내 예약 조회 API

**Request**

```http request
GET /reservations-mine HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
content-type: application/json

```

**Response**

```http request
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "reservationId": 1,
        "theme": "Theme 1",
        "date": "2024-07-01",
        "time": "10:00:00",
        "status": "예약"
    },
    {
        "reservationId": 8,
        "theme": "Theme 1",
        "date": "2024-07-01",
        "time": "11:00:00",
        "status": "예약"
    },
    {
        "reservationId": 4,
        "theme": "Theme 1",
        "date": "2024-07-01",
        "time": "14:00:00",
        "status": "2번째 예약"
    },
    {
        "reservationId": 11,
        "theme": "Theme 1",
        "date": "2024-07-01",
        "time": "15:00:00",
        "status": "예약"
    },
    {
        "reservationId": 12,
        "theme": "Theme 1",
        "date": "2024-07-01",
        "time": "16:00:00",
        "status": "예약"
    },
    {
        "reservationId": 11,
        "theme": "Theme 2",
        "date": "2024-07-01",
        "time": "20:00:00",
        "status": "1번째 예약"
    }
]

```

---

### 예약 대기 취소 API

**Request**

```http request
DELETE /reservations/id HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
content-type: application/json

```

**Response**

```http request
HTTP/1.1 204 No Content

```

---

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
