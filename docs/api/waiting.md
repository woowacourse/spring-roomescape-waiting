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

### 시나리오

- 예약 대기에 성공하면 201을 반환한다.
- 인증 정보가 올바르지 않을 경우 401을 반환한다.
- 요청한 예약을 찾을 수 없는 경우 400을 반환한다.
- 중복 예약 대기인 경우(이미 사용자가 생성했던 예약 대기인 경우) 400을 반환한다.
- 이미 사용자가 선점한 예약인 경우(예약 대기를 할 필요 없는 경우) 400을 반환한다.
- 지나간 시간/날짜에 예약 대기를 할 경우 400을 반환한다.

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

### 시나리오

- 내 예약 대기 취소에 성공하면 204를 반환한다.
- 인증 정보가 올바르지 않을 경우 401을 반환한다.
- 내가 생성한 예약 대기 ID가 아닐 경우 403을 반환한다.
