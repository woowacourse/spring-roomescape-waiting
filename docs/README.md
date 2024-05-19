# 방탈출 예약 관리

## API 명세

### 예약 조회 API

**Request**

```http request
GET /reservations HTTP/1.1
```

<br>

**Response**

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
        }
    },
    {
        "id": 2,
        "name": "브라운",
        "date": "2023-01-02",
        "time": {
            "id": 1,
            "startAt": "10:00"
        }
    }
]
```

<br>

### 예약 추가 API

**Request**

```http request
POST /reservations HTTP/1.1
content-type: application/json

{
    "date": "2023-08-05",
    "name": "브라운",
    "timeId": 1
}
```

<br>

**Response**

```
HTTP/1.1 201
Content-Type: application/json
Location: reservations/{id}

{
    "id": 1,
    "name": "브라운",
    "date": "2023-08-05",
    "time": {
        "id": 1,
        "startAt": "10:00"
    }
}

```

<br>

### 예약 취소 API

**Request**

```http request
DELETE /reservations/1 HTTP/1.1
```

**Response**

```
HTTP/1.1 204
```

<br>

### 시간 추가 API

**Request**

```http request
POST /times HTTP/1.1
content-type: application/json

{
    "startAt": "10:00"
}
```

**Response**

```
HTTP/1.1 201
Content-Type: application/json
Localtion: 
{
    "id": 1,
    "startAt": "10:00"
}
```

<br>

### 시간 조회 API

**Request**

```http request
GET /times HTTP/1.1
```

**Response**

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

<br>

### 가능 시간 조회 API

**Request**

```http request
GET /times/availability?date={}&themeId={}
```

**Response**

```
HTTP/1.1 200 
Content-Type: application/json

[
    {
        "id": 1,
        "startAt": "15:00",
        "booked": false
    },
    {
        "id": 2,
        "startAt": "16:00",
        "booked": false
    }
]
```


<br>

### 시간 삭제 API

**Request**

```http request
DELETE /times/1 HTTP/1.1
```

**Response**

```
HTTP/1.1 204
```

<br>

### 테마 조회 API

**request**

```http request
GET /themes HTTP/1.1
```

**response**

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

<br>

### 테마 추가 API

**request**

```http request
POST /themes HTTP/1.1
content-type: application/json

{
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}

```

**response**

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

<br>

### 테마 삭제 API

**request**

```http request
DELETE /themes/1 HTTP/1.1
```

**response**

```
HTTP/1.1 204
```

<br>

### 인기 테마 API

**request**

```http request
GET /themes/popular?from={}&until={}&limit={} HTTP/1.1
```

**response**

```http request
HTTP/1.1
Content-Type: application/json

[
    {
        name: "theme1",
        thumbnail: "https://abc.com/thumb.png",
        description: "spring desc"
    },
    ...
]
```

### 나의 예약 조회 API

**request**

```http request
GET /reservations/mine HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```

**response**

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
