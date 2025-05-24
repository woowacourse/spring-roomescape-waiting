# 예약 대기 API 목록

## 사용자 예약 대기 추가 API

### Request

```
POST /reservations/waitings HTTP/1.1
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
Location: /reservations/waitings/1
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

## 어드민 예약 대기 조회 API

### Request

```
GET /admin/reservations/waitings HTTP/1.1
```

### Response

```
[
    {
        "id": 1,
        "member": {
            "id": 1,
            "email": "aaa@gmail.com",
            "name": "사용자1"
        },
        "date": "2025-05-25",
        "time": {
            "id": 1,
            "startAt": "10:00"
        },
        "theme": {
            "id": 1,
            "name": "테마1",
            "description": "테마1입니다.",
            "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        }
    }
]
```

## 어드민 예약 대기 승인 API

### Request

```
PATCH /admin/reservations/waitings/1 HTTP/1.1
content-type: application/json
```

### Response

```
HTTP/1.1 200
Location: /reservations/waitings/1
Content-Type: application/json

{
    "id": 1,
    "name" : {
        "id": 1,
        "name": "사용자1",
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

## 사용자 예약 대기 취소 API

### Request

```
DELETE /reservations/waitings/1 HTTP/1.1
```

### Response

```
HTTP/1.1 204
```

## 어드민 예약 대기 거절 API

### Request

```
DELETE /admin/reservations/waitings/1 HTTP/1.1
```

### Response

```
HTTP/1.1 204
```
