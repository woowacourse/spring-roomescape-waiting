# 예약 API 목록

## 예약 추가 API

### Request

```
POST /reservations HTTP/1.1
content-type: application/json
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI

{
    "date": "2024-03-01",
    "themeId": 1,
    "timeId": 1
}
```

### Response

```
HTTP/1.1 201
Location: /reservations/1
Content-Type: application/json

{
    "id": 1,
    "name" : {
        "id": 1,
        "name": "브라운",
        "email": "aaa@gmail.com"
    },
    "date": "2023-08-05",
    "time" : {
        "id": 1,
        "startAt" : "10:00"
    },
    "theme" : {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    }
}

```

## 어드민 예약 조회 API

### Request

```
GET /admin/reservations?themeId=1&memberId=1&dateFrom='2023-08-05'&dateTo='2023-08-05' HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
```

### Response

```
[
    {
        "id": 1,
        "member" : {
            "id": 1,
            "name": "브라운",
            "email": "aaa@gmail.com"
        },      
        "date": "2023-08-05",
        "time": {
            "id": 1,
            "startAt": "10:00"
        },
        "theme" : {
            "id": 1,
            "name": "레벨2 탈출",
            "description": "우테코 레벨2를 탈출하는 내용입니다.",
            "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        }
    }
]
```

## 사용자 예약 조회 API

### Request

```
GET /reservations HTTP/1.1
```

### Response

```
[
    {
        "id": 1,
        "member" : {
            "id": 1,
            "name": "브라운",
            "email": "aaa@gmail.com"
        },      
        "date": "2023-08-05",
        "time": {
            "id": 1,
            "startAt": "10:00"
        },
        "theme" : {
            "id": 1,
            "name": "레벨2 탈출",
            "description": "우테코 레벨2를 탈출하는 내용입니다.",
            "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        }
    }
]
```

## 내 예약 목록 조회 API

### Request

```
GET /mine/reservations HTTP/1.1

cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```

### Response

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
        "status": "예약 대기"
    }
]
```

## 예약 취소 API

### Request

```
DELETE /reservations/1 HTTP/1.1
```

### Response

```
HTTP/1.1 204
```

