# 기능 명세

## Admin PAGE

- [X] `/admin` `GET` 요청 시 어드민 메인 페이지를 응답한다.
- [X] `/admin/reservation` `GET` 요청 시 예약 관리 페이지를 응답한다.
- [X] `/admin/time` `GET` 요청 시 시간 관리 페이지를 응답한다.
- [X] `/admin/theme` `GET` 요청 시 테마 관리 페이지를 응답한다.

## Reservation PAGE

- [X] `/reservation` `GET` 요청 시 예약 페이지를 응답한다.

## Reservation API

- [X] `/reservations` `GET` 요청 시 예약 목록을 조회하고 API 명세에 맞게 응답을 반환한다.
- [X] `/reservations` `POST` 요청 시 예약을 추가하고 API 명세에 맞게 응답을 반환한다.
    - [x] 예약자 명, 테마 아이디, 예약 날짜, 시간 아이디가 비어있으면 예외를 발생시키고 상태코드 400을 반환한다.
    - [x] 예약 날짜가 형식에 맞지 않는다면 예외를 발생시키고 상태코드 400을 반환한다.
    - [x] 테마 아이디가 자연수가 아니라면 예외를 발생시키고 상태코드 400을 반환한다.
    - [x] 예약 시간 아이디가 자연수가 아니라면 예외를 발생시키고 상태코드 400을 반환한다.
    - [x] 현재보다 이전 날짜 및 시간이라면 예외를 발생시키고 상태코드 400을 반환한다.
    - [x] 예약 시간 아이디가 시간 테이블에 없으면 예외를 발생시키고 상태코드 400을 반환한다.
    - [x] 해당 테마에 같은 날짜와 시간의 예약이 존재하면 예외를 발생시키고 상태코드 400을 반환한다.
- [X] `/admin/reservations` `POST` 요청 시 예약을 추가하고 API 명세에 맞게 응답을 반환한다.
    - [x] 토큰을 확인하고 권한이 어드민인 경우만 접근이 가능하다.
- [X] `/admin/reservations/{id}` `DELETE` 요청 시 예약을 삭제하고 API 명세에 맞게 응답을 반환한다.
    - [x] 토큰을 확인하고 권한이 어드민인 경우만 접근이 가능하다.
    - [x] 존재하지 않는 아이디를 삭제하려고 하면 예외를 발생시키고 상태코드 400을 반환한다.

## Time API

- [X] `/times` `GET` 요청 시 시간 목록을 조회하고 API 명세에 맞게 응답을 반환한다.
- [X] `/admin/times` `POST` 요청 시 시간을 추가하고 API 명세에 맞게 응답을 반환한다.
    - [x] 토큰을 확인하고 권한이 어드민인 경우만 접근이 가능하다.
    - [X] 시작 시간이 null이라면 예외를 발생시키고 상태코드 400을 반환한다.
    - [X] 시작 시간이 형식에 맞지 않는다면 예외를 발생시키고 상태코드 400을 반환한다.
    - [X] 시작 시간이 중복이라면 예외를 발생시키고 상태코드 400을 반환한다.
- [X] `/admin/times/{id}` `DELETE` 요청 시 시간을 삭제하고 API 명세에 맞게 응답을 반환한다.
    - [x] 토큰을 확인하고 권한이 어드민인 경우만 접근이 가능하다.
    - [X] 예약이 존재하는 시간을 삭제하려고 하면 예외를 발생시키고 상태코드 400을 반환한다.
    - [X] 존재하지 않는 아이디를 삭제하려고 하면 예외를 발생시키고 상태코드 400을 반환한다.
- [X] `/times/availability?date=${date}&themeId=${themeId}` `GET` 요청 시 시간 목록을 조회하고 API 명세에 맞게 응답을 반환한다.
    - [X] 날짜, 테마 아이디가 비어있으면 예외를 발생시키고 상태코드 400을 반환한다.
    - [X] 예약 날짜가 형식에 맞지 않는다면 예외를 발생시키고 상태코드 400을 반환한다.
    - [X] 테마 아이디가 자연수가 아니라면 예외를 발생시키고 상태코드 400을 반환한다.

## Theme PAGE

- [x] `/` `GET` 요청 시 인기 테마 페이지를 응답한다.

## Theme API

- [x] `/themes` `GET` 요청 시 테마 목록을 조회하고 API 명세에 맞게 응답을 반환한다.
- [x] `/admin/themes` `POST` 요청 시 테마를 추가하고 API 명세에 맞게 응답을 반환한다.
    - [x] 토큰을 확인하고 권한이 어드민인 경우만 접근이 가능하다.
    - [x] 테마 이름, 테마 설명, 테마 썸네일이 비어있다면 예외를 발생시키고 상태코드 400을 반환한다.
- [x] `/admin/themes` `DELETE` 요청 시 테마를 삭제하고 API 명세에 맞게 응답을 반환한다.
    - [x] 토큰을 확인하고 권한이 어드민인 경우만 접근이 가능하다.
    - [x] 예약이 존재하는 테마를 삭제하려고 하면 예외를 발생시키고 상태코드 400을 반환한다.
    - [x] 존재하지 않는 아이디를 삭제하려고 하면 예외를 발생시키고 상태코드 400을 반환한다.
- [X] `/themes/popular?start-date=2024-05-02&end-date=2024-05-09&count=10` `GET` 요청 시 예약이 많은 순서로 인기 테마 결과를 API 명세에 맞게
  반환한다.

## Member PAGE

- [x] `/signup` `GET` 요청 시 회원 가입 페이지를 응답한다.
- [x] `/login` `GET` 요청 시 로그인 페이지를 응답한다.

## Member API

- [x] `/members/signup` `POST` 요청 시 회원 가입을 요청하고 API 명세에 맞게 응답을 반환한다.
- [x] `/members/login` `POST` 요청 시 회원 로그인을 요청하고 API 명세에 맞게 응답을 반환한다.
- [x] `/members/login/check` `GET` 요청 시 토큰에 대한 사용자 정보를 조회하고 API 명세에 맞게 응답을 반환한다.
- [x] `/members/logout` `POST` 요청 시 토큰을 만료시키고 API 명세에 맞게 응답을 반환한다.
- [x] `/admin/members` `GET` 요청 시 토큰을 확인하고 회원 이름 정보를 응답한다.

# API 명세

## reservations

### 예약 목록 조회

```http
GET /reservations HTTP/1.1
cookie: token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDA4IiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.QBXPFjmHn0iteRzHlr0Pml3EJLOOW8QKYcT8FY9sTszJluHU0hC3frCUNIEc5A5h0hmJFZD1e4j2y3aj5u8uDg;
```

```http
[
  {
    "id": 1,
    "member": {
      "id": 1001,
      "name": "재즈"
    },
    "theme": {
      "id": 1002,
      "name": "테마2",
      "description": "테마2입니다",
      "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    },
    "date": "2100-01-21",
    "time": {
      "id": 1001,
      "startAt": "12:00"
    }
  }
[
```

### 예약 등록

```http
POST /reservations HTTP/1.1
content-type: application/json
cokie: token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyerOymiOyerOymiCJ9.t1Oa1y2_dhNdGuLy27Rm9ONAak-LGVpN0f3HROTJjLZh_CldzvJBEBN4fPUnCt1eDWNVpWTjUFdbPyJofoPZxA; Max-Age=1440; Expires=Thu, 09 May 2024 11:10:20 GMT; Path=/; HttpOnly
{
    "date": "2023-08-05",
    "themeId": 1,
    "timeId": 1
}
```

```http
HTTP/1.1 201
Content-Type: application/json
{
    "id": 1,
    "name": "브라운",
    "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    },
    "date": "2023-08-05",
    "time" : {
        "id": 1,
        "startAt" : "10:00"
    }
}
```

### 예약 삭제

```http
DELETE /admin/reservations/1 HTTP/1.1
cokie: token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyerOymiOyerOymiCJ9.t1Oa1y2_dhNdGuLy27Rm9ONAak-LGVpN0f3HROTJjLZh_CldzvJBEBN4fPUnCt1eDWNVpWTjUFdbPyJofoPZxA; Max-Age=1440; Expires=Thu, 09 May 2024 11:10:20 GMT; Path=/; HttpOnly
```

```http
HTTP/1.1 204
```

## times

### 예약 시간 목록 조회

```http
GET /times HTTP/1.1
```

```http
HTTP/1.1 200
Content-Type: application/json
[
   {
        "id": 1,
        "startAt": "10:00"
    }
]
```

### 예약 가능한 시간 상태 목록 조회

```http
GET /times/availability?date="2024-12-20"&themeId=1 HTTP/1.1
```

```http
HTTP/1.1 200
Content-Type: application/json
[
   {
        "id": 1,
        "startAt": "10:00"
        "alreadyBooked": true
    }
]
```

### 예약 시간 추가

```http
POST /admin/times HTTP/1.1
cokie: token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyerOymiOyerOymiCJ9.t1Oa1y2_dhNdGuLy27Rm9ONAak-LGVpN0f3HROTJjLZh_CldzvJBEBN4fPUnCt1eDWNVpWTjUFdbPyJofoPZxA; Max-Age=1440; Expires=Thu, 09 May 2024 11:10:20 GMT; Path=/; HttpOnly
content-type: application/json
{
    "startAt": "10:00"
}
```

```http
HTTP/1.1 201
Content-Type: application/json
{
    "id": 1,
    "startAt": "10:00"
}
```

### 예약 시간 삭제

```http
DELETE /admin/times/1 HTTP/1.1
cokie: token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyerOymiOyerOymiCJ9.t1Oa1y2_dhNdGuLy27Rm9ONAak-LGVpN0f3HROTJjLZh_CldzvJBEBN4fPUnCt1eDWNVpWTjUFdbPyJofoPZxA; Max-Age=1440; Expires=Thu, 09 May 2024 11:10:20 GMT; Path=/; HttpOnly
```

```http
HTTP/1.1 204
```

## theme

### 테마 목록 조회

```http
GET /themes HTTP/1.1
```

```http
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

### 테마 추가

```http
POST /admin/themes HTTP/1.1
content-type: application/json
cokie: token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyerOymiOyerOymiCJ9.t1Oa1y2_dhNdGuLy27Rm9ONAak-LGVpN0f3HROTJjLZh_CldzvJBEBN4fPUnCt1eDWNVpWTjUFdbPyJofoPZxA; Max-Age=1440; Expires=Thu, 09 May 2024 11:10:20 GMT; Path=/; HttpOnly

{
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

```http
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

### 테마 삭제

```http
DELETE /admin/themes/1 HTTP/1.1
cokie: token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyerOymiOyerOymiCJ9.t1Oa1y2_dhNdGuLy27Rm9ONAak-LGVpN0f3HROTJjLZh_CldzvJBEBN4fPUnCt1eDWNVpWTjUFdbPyJofoPZxA; Max-Age=1440; Expires=Thu, 09 May 2024 11:10:20 GMT; Path=/; HttpOnly
```

```http
HTTP/1.1 204
```

### 인기 테마 목록 조회 API (예약 순)

```http
GET /themes/popular?start-date=2024-05-01&end-date=2024-05-05&count=10 HTTP1.1
```

```http
HTTP/1.1 200 
Content-Type: application/json

[
  {
    "id": 1000,
    "name": "테마1",
    "description": "테마1입니다",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  },
  {
    "id": 2000,
    "name": "테마2",
    "description": "테마2입니다",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  },
  {
    "id": 3000,
    "name": "테마3",
    "description": "테마3입니다",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  }
]
```

## members

### 회원 가입

```http
POST /memebers/signup HTTP 1.1
content-type: application/json

{
    "email": "1234@1234.com",
    "password": "1234",
    "name": "재즈재즈"
}
```

```http
HTTP/1.1 201
```

### 로그인

```http
POST /members/login HTTP 1.1
Content-Type: application/json
host: localhost:8080

{
  "email": "1234@1234.com",
  "password": "1234"
}
```

```http
HTTP/1.1 200 
cookie: token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyerOymiOyerOymiCJ9.t1Oa1y2_dhNdGuLy27Rm9ONAak-LGVpN0f3HROTJjLZh_CldzvJBEBN4fPUnCt1eDWNVpWTjUFdbPyJofoPZxA; Max-Age=1440; Expires=Thu, 09 May 2024 11:10:20 GMT; Path=/; HttpOnly
Content-Length: 0
Date: Thu, 09 May 2024 10:46:20 GMT
```

### 토큰으로 유저 정보 확인

```http
GET /members/login/check HTTP 1.1
cokie: token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyerOymiOyerOymiCJ9.t1Oa1y2_dhNdGuLy27Rm9ONAak-LGVpN0f3HROTJjLZh_CldzvJBEBN4fPUnCt1eDWNVpWTjUFdbPyJofoPZxA; Max-Age=1440; Expires=Thu, 09 May 2024 11:10:20 GMT; Path=/; HttpOnly
```

```http
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 09 May 2024 10:46:55 GMT

{
  "name": "재즈재즈"
}
```

### 로그아웃

```http
GET /members/logout HTTP 1.1
cookie: token=asdzxcqwe...
```

```http
HTTP/1.1 200 
Set-Cookie: token=; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:10 GMT
Content-Length: 0
Date: Thu, 09 May 2024 10:47:09 GMT
```

### 유저 이름 목록 반환

```http
GET /admin/members HTTP 1.1
cokie: token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyerOymiOyerOymiCJ9.t1Oa1y2_dhNdGuLy27Rm9ONAak-LGVpN0f3HROTJjLZh_CldzvJBEBN4fPUnCt1eDWNVpWTjUFdbPyJofoPZxA; Max-Age=1440; Expires=Thu, 09 May 2024 11:10:20 GMT; Path=/; HttpOnly
```

```http
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 09 May 2024 10:46:55 GMT

[
  {
    "id": 1001,
    "name": "재즈"
  },
  {
    "id": 1002,
    "name": "러너덕"
  },
  {
    "id": 1008,
    "name": "어드민"
  }
]
```
