# 방탈출 예약 대기 시스템

## DB 테이블 설계

![db-table.svg](db-table.svg)

- Reservation Detail: 예약 정보
- Reservation: 예약과 예약 대기 정보
- Member: 사용자 정보
- Reservation Time: 예약 시간 정보
- Theme: 테마 정보

## 시스템 정책

- 예약 정보(Reservation Detail) 당 1개의 예약만 가능하다. 
- 예약 대기는 여러 명이 할 수 있다. 
- 한 명의 사용자는 예약 대기와 에약을 동시에 할 수 없다. 
- 관리자는 수동으로 예약 대기를 예약으로 전환할 수 있다. 
- 예약 취소와 예약 대기 취소 요청이 발생하면 Reservation의 Status는 CANCELED로 변경된다.


## API 명세

---

### 로그인 API

**Request**

```http request
POST /login HTTP/1.1
content-type: application/json

{
    "email": "admin@email.com",
    "password": "password"
}

```

**Response**

```http request
HTTP/1.1 200 OK
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

#### 모든 사용자 조회 API

**Request**

```http request
GET /members HTTP/1.1

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
DELETE /members/{idMember} HTTP/1.1

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

### 내 예약 조회 API

**Request**

```http request
GET /reservations-mine HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI

```

**Response**

```http request
HTTP/1.1 200 OK
Content-Type: application/json

[
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
    }
]

```

---

### 사용자의 예약 대기 취소 API

**Request**

```http request
DELETE /waitings/{idWaiting} HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
content-type: application/json

```

**Response**

```http request
HTTP/1.1 204 No Content

```

---

### 사용자의 예약 취소 API

**Request**

```http request
DELETE /reservations/{idReservations} HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI

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
    "date": "2024-05-25",
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

### 예약 대기를 예약으로 전환 API

**Request**

```http request
POST /admin/reservations HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
content-type: application/json

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
    "date": "2024-05-25",
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

### 관리자의 모든 예약 조회 API

**Request**

```http request
GET /admin/reservations HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
```

**Response**

```http request
HTTP/1.1 201
Content-Type: application/json

[
    {
        "id": 1,
        "date": "2099-07-01",
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
            "name": "미르"
        },
        "status": "RESERVED"
    }
]
```

---

### 관리자의 예약 조건에 따라 조회 API

**Request**

```http request
GET /admin/reservations/search?start=2024-06-12&end=2024-06-13&memberId=1&themeId=1 HTTP/1.1
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

### 관리자의 모든 예약 대기 조회 API

**Request**

```http request
GET /admin/waitings HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI

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

### 관리자의 예약 취소 API

**Request**

```http request
DELETE /admin/waitings/{idWaiting} HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI

```

**Response**

```http request
HTTP/1.1 200 OK

```

---

### 관리자의 예약 대기 취소 API

**Request**

```http request
DELETE /admin/reservations/{idReservation} HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI

```

**Response**

```http request
HTTP/1.1 200 OK

```

---

### 모든 예약 시간 조회 API

**Request**

```http request
GET /times HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI

```

**Response**

```http request
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "startAt": "10:00"
    },
    {
        "id": 2,
        "startAt": "11:00"
    }
]

```

---

### 예약 가능 여부를 포함한 시간 조회 API

**Request**

```http request
GET /times-available?date=2024-06-12&themeId=1 HTTP/1.1

```

**Response**

```http request
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "startAt": "10:00",
        "alreadyBooked": true
    },
    {
        "id": 2,
        "startAt": "11:00",
        "alreadyBooked": false
    }
]

---

### 관리자의 시간 저장 API

**Request**

```http request
POST /admin/times HTTP/1.1
Content-Type: application/json

{
    "startAt": "10:00"
}

```

**Response**

```http request
HTTP/1.1 201 Created

```

---

### 관리자의 시간 삭제 API

**Request**

```http request
DELETE /admin/times/{idTime} HTTP/1.1

```

**Response**

```http request
HTTP/1.1 204 No Content

```

---

### 모든 테마 조회 API

**Request**

```http request
GET /themes HTTP/1.1

```

**Response**

```http request

HTTP/1.1 200 OK

[
    {
        "id": 1,
        "name": "Theme 1",
        "description": "Description 1",
        "thumbnail": "https://www.google.jpg"
    },
    {
        "id": 2,
        "name": "Theme 2",
        "description": "Description 2",
        "thumbnail": "https://www.google.jpg"
    }
]

```

---

### 인기 테마 조회 API

**Request**

```http request
GET /themes/ranking HTTP/1.1

```

**Response**

```http request
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "id": 1,
        "name": "Theme 1",
        "description": "Description 1",
        "thumbnail": "https://www.google.jpg"
    },
    {
        "id": 2,
        "name": "Theme 2",
        "description": "Description 2",
        "thumbnail": "https://www.google.jpg"
    }
]

```

---

### 관리자의 테마 저장 API

**Request**

```http request
POST /admin/themes HTTP/1.1
Content-Type: application/json

{
    "name": "Theme 1",
    "description": "Description 1",
    "thumbnail": "https://www.google.jpg"
}

```

**Response**

```http request
HTTP/1.1 201 Created

```

---

### 관리자의 테마 삭제 API

**Request**

```http request
DELETE /admin/themes/{idTheme} HTTP/1.1

```

**Response**

```http request
HTTP/1.1 204 No Content

```
