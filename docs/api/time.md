# 시간 API 목록

## 시간 추가 API

### Request

```
POST /times HTTP/1.1
content-type: application/json

{
    "startAt": "10:00"
}
```

### Response

```
HTTP/1.1 201
Location: /times/1
Content-Type: application/json

{
    "id": 1,
    "startAt": "10:00"
}
```

## 시간 조회 API

### Request

```
GET /times HTTP/1.1
```

### Response

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

## 시간 삭제 API

### Request

```
DELETE /times/1 HTTP/1.1
```

### Response

```
HTTP/1.1 204
```

## 예약 가능 시간 조회 API

### Request

```
GET /times/availability?date=2025-03-30&themeId=1 HTTP/1.1
```

### Response

```
HTTP/1.1 200
Content-Type: application/json

[
    {
        "timeId": 1,
        "startAt": "10:00",
        "alreadyBooked": false
    }
]
```
