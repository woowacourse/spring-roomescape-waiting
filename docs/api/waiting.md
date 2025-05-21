# 예약 대기 API 목록

## 예약 대기 생성 API

### Request

```
POST /waitings HTTP/1.1
content-type: application/json
cookie: token={member-access-token}

{
    "date": "2024-03-01",
    "themeId": 1,
    "timeId": 1
}
```

### Response

```
HTTP/1.1 201
Location: /waitings/1
Content-Type: application/json

{
    "id": 1,
    "reservation": {
        "id": 1,
        "member" : {
            "id": 1,
            "name": "브라운",
            "email": "aaa@gmail.com"xo
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
    },
    "member" : {
        "id": 2,
        "name": "포비",
        "email": "bbb@gmail.com"xo
    },
    createdAt: "2023-08-05 10:00:00"
}
```

## 내 예약 대기 취소 API

### Request

```
DELETE /waitings/1 HTTP/1.1
cookie: token={member-access-token}
```

### Response

```
HTTP/1.1 204
```
