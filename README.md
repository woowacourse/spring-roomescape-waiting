# 요구사항 문서

- [x] API 명세를 현재 프론트엔드 코드가 잘 동작할 수 있도록 수정
- [x] 예약 시간에 대한 제약 조건 추가
    - [x] 중복된 예약 시간 생성 요청 시 에러
    - [x] ISO 8601 표준에 따른 hh:mm 포맷에 해당하지 않는 요청 시 에러
    - [x] 예약이 있는 예약 시간을 삭제 요청 시 에러
- [x] 예약에 대한 제약 조건 추가
    - [x] 동일한 날짜와 시간, 테마에 예약 생성 요청 시 에러
    - [x] 존재하지 않는 시간에 예약 생성 요청 시 에러
    - [x] ISO 8601 표준에 따른 YYYY-MM-dd 포맷에 해당하지 않는 날짜가 포함된 예약 생성 요청 시 에러
    - [x] 지나간 날짜와 시간의 예약 요청 시 에러
    - [x] 이름이 비어있는 예약 요청 시 에러
    - [x] 존재하지 않는 테마 예약 생성 요청시 에러
    - [x] 테마 값이 비어있는 예약 요청 시 에러

- [x] 테마에 대한 제약 조건 추가
    - [x] 테마 이름, 설명, 썸네일 이미자가 비어 있을 경우 에러
    - [x] 중복된 이름의 테마 생성 요청시 에러
    - [x] 예약이 있는 테마를 삭제 요청시 에러

- [x] 사용자 예약 기능 추가
- [x] 인기 테마 기능 추가

- [x] 사용자 기능 추가
  - [ ] ~~회원가입~~
  - [x] 로그인 기능 추가
  - [x] 로그아웃 기능 추가
  - [x] 인증 정보 확인 기능 추가
- [x] 테마 검색 기능 추가

# API 명세

## 예약 조회 API

### Request

> GET /reservations HTTP/1.1

### Response

> HTTP/1.1 200
>
> Content-Type: application/json

``` JSON 
[
    {
        "id": 1,
        "name": "브라운",
        "date": "2023-08-05",
        "time": {
            "id": 1,
            "startAt": "10:00"
        }
        "theme" : {
            "id": 1,
            "name": "이름",
            "description": "설명",
            "thumbnail": "썸네일"
        }
    }
]
```

## 예약 검색 API

### Request

> GET /reservations/search?themeId=${themeId}&memberId=${memberId}&dateFrom=${dateFrom}&dateTo=${dateTo} HTTP/1.1

### Response

> HTTP/1.1 200
>
> Content-Type: application/json

``` JSON 
[
    {
        "id": 1,
        "name": "브라운",
        "date": "2023-08-05",
        "time": {
            "id": 1,
            "startAt": "10:00"
        }
        "theme" : {
            "id": 1,
            "name": "이름",
            "description": "설명",
            "thumbnail": "썸네일"
        }
    }
]
```

## 예약 추가 API - 사용자

### Request

> POST /reservations HTTP/1.1  
> content-type: application/json  
> cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI  
> host: localhost:8080  

```JSON
{
  "date": "2023-08-05",
  "timeId": 1,
  "themeId": 1
}
```

### Response

> HTTP/1.1 201
>
> Content-Type: application/json
> Location: /reservations/{id}

```JSON
{
  "id": 1,
  "name": "브라운",
  "date": "2023-08-05",
  "time": {
    "id": 1,
    "startAt": "10:00"
  },
  "theme": {
    "id": 1,
    "name": "이름",
    "description": "설명",
    "thumbnail": "썸네일"
  }
}
```

## 예약 추가 API - 관리자

### request

> POST /admin/reservations HTTP/1.1  
> content-type: application/json  
> cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI  
> host: localhost:8080  

```JSON
{
  "date": "2024-03-01",
  "themeId": 1,
  "timeId": 1,
  "memberId": 1
}
```

### response

> HTTP/1.1 201
>
> Content-Type: application/json
> Location: /reservations/{id}

```JSON
{
  "id": 1,
  "name": "브라운",
  "date": "2023-08-05",
  "time": {
    "id": 1,
    "startAt": "10:00"
  },
  "theme": {
    "id": 1,
    "name": "이름",
    "description": "설명",
    "thumbnail": "썸네일"
  }
}
```

## 예약 취소 API

### Request

> DELETE /reservations/1 HTTP/1.1

### Response

> HTTP/1.1 204

## 시간 추가 API

### request

> POST /times HTTP/1.1
> content-type: application/json

```JSON
{
  "startAt": "10:00"
}
```

### response

> HTTP/1.1 201
> Content-Type: application/json
> Location: /times/{id}

```JSON
{
  "id": 1,
  "startAt": "10:00"
}
```

## 시간 조회 API

### request

> GET /times HTTP/1.1

### response

> HTTP/1.1 200
> Content-Type: application/json

```JSON
[
  {
    "id": 1,
    "startAt": "10:00"
  }
]
```

## 시간 삭제 API

### request

> DELETE /times/1 HTTP/1.1

### response

> HTTP/1.1 204

## 테마 조회 API

### request

> GET /themes HTTP/1.1

### response

> HTTP/1.1 200
> Content-Type: application/json

```json
[
  {
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  }
]
```

## 테마 추가 API

### request

> POST /themes HTTP/1.1
> content-type: application/json

```json
{
  "name": "레벨2 탈출",
  "description": "우테코 레벨2를 탈출하는 내용입니다.",
  "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

### response

> HTTP/1.1 201
> Location: /themes/1
> Content-Type: application/json

```json
{
  "id": 1,
  "name": "레벨2 탈출",
  "description": "우테코 레벨2를 탈출하는 내용입니다.",
  "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

## 테마 삭제 API

### request

> DELETE /themes/1 HTTP/1.1

### response

> HTTP/1.1 204

## 예약 가능 시간 조회 API

### request

> GET /availableTimes?date=${date}&themeId=${themeId}

### response

> HTTP/1.1 200
> Content-Type: application/json

```json
[
  {
    "id": 0,
    "startAt": "02:53",
    "isBooked": false
  }
]
```

## 로그인 API

### request

> POST /login HTTP/1.1  
> content-type: application/json  
> host: localhost:8080

```json
{
  "password": "password",
  "email": "admin@email.com"
}
```

### response

> HTTP/1.1 200 OK  
> Content-Type: application/json  
> Keep-Alive: timeout=60  
> Set-Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly

## 인증 정보 조회

### request

> GET /login/check HTTP/1.1  
> cookie: _ga=GA1.1.48222725.1666268105; _ga_QD3BVX7MKT=GS1.1.1687746261.15.1.1687747186.0.0.0; Idea-25a74f9c=3cbc3411-daca-48c1-8201-51bdcdd93164; token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM  
> host: localhost:8080  

### response
> HTTP/1.1 200 OK  
> Connection: keep-alive  
> Content-Type: application/json  
> Date: Sun, 03 Mar 2024 19:16:56 GMT  
> Keep-Alive: timeout=60  
> Transfer-Encoding: chunked  

```json 
{
  "name": "어드민"
}
```
