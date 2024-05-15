## API 명세서

### 어드민 예약

<details>
<summary>POST <code>/admin/reservations</code> 어드민 예약 추가 API</summary>

#### Request

어드민 권한 필요

```http
POST /admin/reservations HTTP/1.1
content-type: application/json

{
    "name": "브라운",
    "date": "2023-08-05",
    "timeId": 1,
    "themeId": 1,
    "memberId": 1
}
```

#### Response

```http
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "date": "2023-08-05",
    "time" : {
        "id": 1,
        "startAt" : "10:00"
    },
    "theme" : {
        "id": 1,
        "name": "테마1",
        "thumbnail": "https://example.com/thumbnail.jpg"
    },
    "member" : {
        "id": 1,
        "email": "example@gmail.com",
        "name": "브라운",
        "role": "USER"
    }
}
```

</details>

### 인증

<details>

<summary>POST <code>/login</code> 로그인 API</summary>

#### Request

```http
POST /login HTTP/1.1
content-type: application/json

{
    "email": "example@gmail.com",
    "password": "password"
}

```

#### Response

이름이 "token"인 쿠키에 JWT 토큰을 저장한다.

```http
HTTP/1.1 200
Content-Type: application/json

{
    "id": 1,
    "email": "example@gmail.com",
    "name": "브라운",
    "role": "USER"
}

```

</details>

<details>

<summary>POST <code>/login/check</code> 로그인 체크 API</summary>

#### Request

```http
POST /login/check HTTP/1.1
```

#### Response

```http
HTTP/1.1 200
Content-Type: application/json

{
    "id": 1,
    "email": "example@gmail.com",
    "name": "브라운",
    "role": "USER"
}

```

</details>

<details>

<summary>POST <code>/logout</code> 로그아웃 API</summary>

이름이 "token"인 쿠키를 삭제한다.

#### Request

```http
POST /logout HTTP/1.1
```

#### Response

```http
HTTP/1.1 204
```

</details>

### 회원

<details>

<summary>POST <code>/members</code> 회원 가입 API</summary>

#### Request

```http
POST /members HTTP/1.1
content-type: application/json

{
    "email": "example@gmail.com",
    "password": "password",
    "name": "브라운"
}
```

#### Response

```http
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "email": "example@gmail.com",
    "name": "브라운",
    "role": "USER"
}
```

</details>

<details>

<summary>GET <code>/members</code> 전체 회원 조회 API</summary>

#### Request

```http
GET /members HTTP/1.1
```

#### Response

```http
HTTP/1.1 200
Content-Type: application/json

[
    {
        "id": 1,
        "email": "example@gmail.com",
        "name": "브라운",
        "role": "USER"
    }
]
```

</details>
        

### 예약

<details>
<summary>POST <code>/reservations</code> 예약 추가 API</summary>

#### Request

"token"인 쿠키 필요

```http
POST /reservations HTTP/1.1
content-type: application/json

{
    "date": "2023-08-05",
    "timeId": 1,
    "themeId": 1
}
```

#### Response

```http
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "date": "2023-08-05",
    "time" : {
        "id": 1,
        "startAt" : "10:00"
    },
    "theme" : {
        "id": 1,
        "name": "테마1",
        "thumbnail": "https://example.com/thumbnail.jpg"
    }, 
    "member" : {
        "id": 1,
        "email": "example@gmail.com",
        "name": "브라운",
        "role": "USER"
    }
}
```

</details>

<details>
<summary>GET <code>/reservations</code> 전체 예약 조회 API</summary>

#### Request

```http
GET /reservations HTTP/1.1
```

#### Response

```http
HTTP/1.1 200
Content-Type: application/json

[
  {
    "id": 1,
    "date": "2023-08-05",
    "time" : {
        "id": 1,
        "startAt" : "10:00"
    },
    "theme" : {
        "id": 1,
        "name": "테마1",
        "thumbnail": "https://example.com/thumbnail.jpg"
    }, 
    "member" : {
        "id": 1,
        "email": "example@gmail.com",
        "name": "브라운",
        "role": "USER"
    }
  }
]
```

</details>

<details>
<summary>GET <code>/reservations/mine</code> 나의 예약들 조회 API</summary>

#### Request

"token"인 쿠키 필요
나의 예약들을 조회한다.

```http
GET /reservations/mine HTTP/1.1
```

#### Response

```http
HTTP/1.1 200
Content-Type: application/json

[
  {
    "id": 1,
    "date": "2023-08-05",
    "time" : {
        "id": 1,
        "startAt" : "10:00"
    },
    "theme" : {
        "id": 1,
        "name": "테마1",
        "thumbnail": "https://example.com/thumbnail.jpg"
    }, 
    "member" : {
        "id": 1,
        "email": "user@gmail.com",
        "name": "브라운",
        "role": "USER"
    }
    "status": "예약 대기"
  }
]
```

</details>


<details>
<summary>DELETE <code>/reservations/{id}</code> 예약 삭제 API</summary>

#### Request

```http
DELETE /reservations/1 HTTP/1.1
```

#### Response

```http
HTTP/1.1 204
```

</details>

### 예약 시간

<details>
<summary>POST <code>/times</code> 예약 시간 추가 API</summary>

#### Request

```http
POST /times HTTP/1.1
content-type: application/json

{
    "startAt": "10:00"
}
```

#### Response

```http
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "startAt": "10:00"
}
```

</details>

<details>
<summary>GET <code>/times</code> 전체 예약 시간 조회 API</summary>

#### Request

```http
GET /times HTTP/1.1
```

#### Response

```http
HTTP/1.1 200 
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

</details>

<details>
<summary>DELETE <code>/times/{id}</code> 예약 시간 삭제 API</summary>

#### Request

```http
DELETE /times/1 HTTP/1.1
```

#### Response

```http
HTTP/1.1 204
```

</details>

<details>
<summary>GET <code>/times/available</code> 이용 가능한 시간 조회 API</summary>

#### Request

```http
GET /times/available?date={date}&themeId={themeId} HTTP/1.1

date: (필수) 예약 가능한 시간을 조회할 날짜 (형식: YYYY-MM-DD)
themeId: (필수) 조회할 테마의 ID
```

#### Response

```http
HTTP/1.1 200

[
    {
        "timeId": 1,
        "startAt": "10:00"
        "alreadyBooked": false
    },
    {
        "timeId": 2,
        "startAt": "10:40"
        "alreadyBooked": true
    }
]
```

</details>

### 테마

<details>
<summary>POST <code>/themes</code> 테마 추가 API</summary>

#### Request

```http
POST /themes HTTP/1.1
content-type: application/json

{
    "name": "테마1",
    "description": "테마1 설명",
    "thumbnail": "https://example.com/thumbnail.jpg"
}
```

#### Response

```http
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "name": "테마1",
    "description": "테마1 설명",
    "thumbnail": "https://example.com/thumbnail.jpg"
}
```

</details>

<details>
<summary>GET <code>/themes</code> 전체 테마 조회 API</summary>

#### Request

```http
GET /themes HTTP/1.1
```

#### Response

```http
HTTP/1.1 200
Content-Type: application/json

[
    {
        "id": 1,
        "name": "테마1",
        "description": "테마1 설명",
        "thumbnail": "https://example.com/thumbnail.jpg"
    },
    {
        "id": 2,
        "name": "테마2",
        "description": "테마2 설명",
        "thumbnail": "https://example.com/thumbnail-2.jpg"
    }
]
```

</details>

<details>
<summary>DELETE <code>/themes/{id}</code> 테마 삭제 API</summary>

#### Request

```http
DELETE /themes/1 HTTP/1.1
```

#### Response

```http
HTTP/1.1 204
```

</details>

<details>
<summary>GET <code>/themes/popular</code> 인기 테마 조회 API</summary>

#### Request

```http
GET /themes/popular?startDate={startDate}&endDate={endDate}&limit={limit} HTTP/1.1
```

시작날짜와 끝날짜를 입력받아 해당 기간 동안 예약이 가장 많은 테마 10개를 조회한다.

startDate: (required=false) 시작 날짜 (형식: YYYY-MM-DD)
endDate: (required=false) 끝 날짜 (형식: YYYY-MM-DD)
limit: (default=10) 조회할 테마의 개수
#### Response

```http
HTTP/1.1 200
Content-Type: application/json

[
    {
        "id": 1,
        "name": "테마1",
        "description": "테마1 설명",
        "thumbnail": "https://example.com/thumbnail.jpg"
    },
    {
        "id": 2,
        "name": "테마2",
        "description": "테마2 설명",
        "thumbnail": "https://example.com/thumbnail-2.jpg"
    }
]
```

</details>
