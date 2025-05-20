# 방탈출 예약 대기

# 화면

- 로그인 페이지: localhost:8080/login
- 회원가입 페이지: localhost:8080/signup
- 인기 테마 페이지: localhost:8080
- 사용자 예약 페이지: localhost:8080/reservation
- 관리자 메인 페이지: localhost:8080/admin
- 예약 관리 페이지: localhost:8080/admin/reservation
- 시간 관리 페이지: localhost:8080/admin/time

# API 명세

### 회원가입

```
Request
POST /api/members HTTP/1.1
content-type: application/json

{
    "name": string,
    "email": string,
    "password": string
}

Response
HTTP/1.1 200
Content-Type: application/json

{
    "id": number,
    "name": string,
    "email": string
}
```

### 로그인

```
Request
POST /api/auth/login HTTP/1.1
content-type: application/json

{
    "email": string,
    "password": string
}

Response
HTTP/1.1 200
Content-Type: application/json
Set-Cookie: token={accessToken}
```

### 로그아웃

```
Request
POST /api/auth/logout HTTP/1.1

Response
HTTP/1.1 200
Set-Cookie: token=
```

### 관리자 예약 목록 조회

```
Request
GET /api/admin/reservations HTTP/1.1
Query Parameters: 
    - memberId
    - themeId
    - dateFrom 
    - dateTo
cookie: token={accessToken}

Response
HTTP/1.1 200 
Content-Type: application/json
[
    {
        "id": number,
        "name": string,
        "date": string (YYYY-MM-DD),
        "timeSlot": {
            "id": number,
            "startAt" : string (HH:mm)
        },
        "themeName": string
    }
]
```

### 예약 추가

```
Request
Content-Type: application/json
POST /api/reservations
{
    "date": string (YYYY-MM-DD),
    "timeId": number,
    "themeId": number
}

Response
Content-Type: application/json
HTTP/1.1 200 
{
    "id": number,
    "name": string,
    "date": string (YYYY-MM-DD),
    "timeSlot": {
        "id": number,
        "startAt" : string (HH:mm)
    },
    "themeName" : string
}

```

### 관리자 예약 추가

```
Request
Content-Type: application/json
POST /api/admin/reservations
{
    "memberId": number,
    "date": string (YYYY-MM-DD),
    "timeId": number,
    "themeId": number
}

Response
Content-Type: application/json
HTTP/1.1 200 
{
    "id": number,
    "name": string,
    "date": string (YYYY-MM-DD),
    "timeSlot": {
        "id": number,
        "startAt" : string (HH:mm)
    },
    "themeName" : string
}

```

### 예약 취소

```
Request
DELETE /api/reservations/{id} HTTP/1.1

Response
HTTP/1.1 200
```

### 시간 추가

```
Request
POST /api/times HTTP/1.1
content-type: application/json

{
    "startAt": string (HH:mm)
}

Response
HTTP/1.1 200
Content-Type: application/json

{
    "id": number,
    "startAt": string (HH:mm)
}
```

### 시간 조회

```
Request
GET /api/times HTTP/1.1

Response
HTTP/1.1 200 
Content-Type: application/json

[
   {
        "id": number,
        "startAt": string (HH:mm)
    }
]
```

### 시간 삭제

```
Request
DELETE /api/times/1 HTTP/1.1

Response
HTTP/1.1 200
```

### 테마 목록 조회

```
Request
GET /api/themes HTTP/1.1

Response
HTTP/1.1 200 
Content-Type: application/json

[
   {
        "id": number,
        "name": string,
        "description": string,
        "thumbnail": string
    }
]
```

### 테마 추가

```
Request
POST /api/themes HTTP/1.1
content-type: application/json

{
    "name": string,
    "description": string,
    "thumbnail": string
}

Response
HTTP/1.1 201
Location: /themes/1
Content-Type: application/json

{
    "id": number,
    "name": string
    "description": string,
    "thumbnail": string
}
```

### 테마 삭제

```
Request
DELETE /api/themes/1 HTTP/1.1

Response
HTTP/1.1 204
```

### 예약 가능한 시간 조회

```
Request
GET /api/times/theme/{themeId}?date={date} HTTP/1.1

Response
HTTP/1.1 200 
Content-Type: application/json

[
   {
        "id": number,
        "startAt": string (HH:mm)
        "alreadyBooked": boolean
    }
]
```

### 인기 테마 조회

```
Request
GET /api/theme/rank

Response
HTTP/1.1 200 
Content-Type: application/json

[
   {
        "id": number
        "name": string
        "description": string
        "thumbnail": string
    }
]
```

### 내 예약 목록 조회

```
Request
GET /api/reservation/my
cookie: token={accessToken}

Response
HTTP/1.1 200 
Content-Type: application/json

{
    "id": number,
    "name": string,
    "date": string (YYYY-MM-DD),
    "timeSlot": {
        "id": number,
        "startAt" : string (HH:mm)
    },
    "themeName" : string
    "status" : string
}
```