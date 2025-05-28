# 로그인 API 목록

## 로그인 API

### Request

```
POST /login HTTP/1.1
content-type: application/json

{
    "password": "password",
    "email": "admin@email.com"
}
```

### Response

```
HTTP/1.1 200 OK
Content-Type: application/json
Set-Cookie: token={access-token}; Path=/; HttpOnly
```

## 인증 정보 조회 API

### Request

```
GET /login/check HTTP/1.1
cookie: token={access-token}
```

### Response

```
HTTP/1.1 200 OK
Content-Type: application/json

{
    "name": "어드민"
}
```

## 로그아웃 API

### Request

```
POST /logout HTTP/1.1
cookie: token={access-token}
```

### Response

```
HTTP/1.1 204
cookie: token=null
```
