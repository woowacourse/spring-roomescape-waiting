# 방탈출 사용자 예약

## 1단계

### 입력 형식에 따른 예외

- [x] 시간 생성 시 시작 시간에 유효하지 않은 값이 입력되었을 때 400 응답
    - [x] 시작 시간이 `null`일 떄
    - [x] 시작 시간의 입력 형식이 `HH:mm`이 아닐 때
- [x] 예약 생성 시 예약자명이 유효하지 않은 값이 입력 되었을 때 400 응답
    - [x] 예약자명이 `null`일 때
    - [x] 예약자명이 빈 값일 때
- [x] 예약 생성시 날짜가 유효하지 않은 값이 입력 되었을 때 400 응답
    - [x] 날짜가 `null`일 때
    - [x] 날짜 형식이 `yyyy-MM-dd`가 아닐떄

### 서비스 정책에 따른 예외

- [x] 지나간 날짜와 시간에 대한 예약 생성을 할 수 없다 (400 응답)
- [x] 이미 존재하는 시간은 생성할 수 없다 (400 응답)
- [x] 중복 예약은 불가능하다 (400 응답)
    - [x] 예약의 시간과 날짜가 같으면 중복으로 간주한다
- [x] 특정 시간에 대한 예약이 존재한다면 그 시간은 삭제할 수 없다 (400 응답)

## 2단계

- [x] 테마 도메인을 추가한다
    - [x] 테마는 이름, 설명, 썸네일을 가진다
    - [x] db의 theme 테이블 구조는 다음과 같다
  ```angular2html
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL,
  thumbnail VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
  ```
- [x] 사용자 예약 시 원하는 테마를 선택할 수 있다.
- [x] 관리자가 테마를 관리할 수 있도록 기능을 추가한다
    - [x] 테마를 조회할 수 있다
  ```http request
  GET /themes HTTP/1.1
  ```
  ```http request
  HTTP/1.1 200 
  Content-Type: application/json
  
  [
     {
          "id": 1,
          "name": "레벨2 탈출",
          "description": "우테코 레벨2를 탈출하는 내용입니다.",
          "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
      }
  ]
  ```
    - [x] 테마를 생성할 수 있다.
  ```http request
  POST /themes HTTP/1.1
  content-type: application/json
  
  {
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  }
  ```
  ```http request
  HTTP/1.1 201
  Location: /themes/1
  Content-Type: application/json
  
  {
      "id": 1,
      "name": "레벨2 탈출",
      "description": "우테코 레벨2를 탈출하는 내용입니다.",
      "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  }
   ```
    - [x] 테마를 삭제할 수 있다
  ```http request
  DELETE /themes/1 HTTP/1.1
  ```
  ```http request
  HTTP/1.1 204
  ```

## 3단계

- [x] /reservation 요청 시 사용자 예약 페이지를 응답한다
- [x] 인기 테마 조회 기능을 추가한다
    - [x] / 요청 시 인기 테마 페이지를 응답한다
    - [x] 최근 일주일을 기준으로 하여 해당 기간 내에 방문하는 예약이 많은 테마 10개를 조회한다
- [x] 사용자는 날짜와 테마를 선택하면 예약 가능한 시간을 확인할 수 있다
    - [x] 이미 예약된 시간의 경우 isAlreadyBooked를 true로 설정하여 응답한다

---

## 4단계

- [x] 사용자 도메인을 추가
- [x] 로그인 기능을 구현
    - [x] 이메일과 비밀번호를 입력하면 로그인
    - [x] 로그인 인증시 jwt 토큰을 쿠키에 담아 반환
- [x] 로그인 후 Cookie를 이용하여 사용자의 정보를 조회하는 API를 구현.

### Request

```http request
POST /login HTTP/1.1
content-type: application/json
host: localhost:8080

{
    "password": "password",
    "email": "admin@email.com"
}
```

### Response

```http request
HTTP/1.1 200 OK
Content-Type: application/json
Keep-Alive: timeout=60
Set-Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
```

---

### Request

```http request
GET /login/check HTTP/1.1
cookie: _ga=GA1.1.48222725.1666268105; _ga_QD3BVX7MKT=GS1.1.1687746261.15.1.1687747186.0.0.0; Idea-25a74f9c=3cbc3411-daca-48c1-8201-51bdcdd93164; token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```

### Response

```http request
HTTP/1.1 200 OK
Connection: keep-alive
Content-Type: application/json
Date: Sun, 03 Mar 2024 19:16:56 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "name": "어드민"
}
```

---

## 5단계

- [x] Cookie에 담긴 인증 정보를 이용해서 멤버 객체를 만드는 로직을 분리
    - [x] HandlerMethodArgumentResolver을 활용
-[x] 사용자가 예약 생성 시, 로그인한 사용자 정보를 활용하도록 리팩터링
    - [x] 사용자가 보낸 쿠키 활용

### Request

```http request
POST /reservations HTTP/1.1
content-type: application/json
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
host: localhost:8080

{
    "date": "2024-03-01",
    "themeId": 1,
    "timeId": 1
}
```

---

-[x] 관리자가 예약 생성 시, 유저를 조회하여 선택 후 예약을 생성하도록 리팩터링

### Request

```http request
POST /admin/reservations HTTP/1.1
content-type: application/json
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
host: localhost:8080

{
    "date": "2024-03-01",
    "themeId": 1,
    "timeId": 1,
    "memberId": 1
}
```

---

## 6단계

- [x] 어드민 페이지 진입은 admin권한이 있는 사람만 할 수 있도록 제한.
    - ADMIN(o) / USER(x)
- [x] 관리자가 조건에 따라 예약을 검색할 수 있도록 기능을 추가.
    - 멤버 Id,테마 id,시작일,마지막일

---

# 방탈출 예약 대기

## 1단계

- [x] Spring Data JPA 적용.

## 2단계

- [x] 내 예약 목록 조회 기능 구현

### Request

```http request
GET /reservations-mine HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```

### Response

```http request
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

## 3단계

- [x] 예약 대기 요청 기능 구현

### Request

```http request
POST /reservations/waiting HTTP/1.1
content-type: application/json
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
host: localhost:8080

{
    "date": "2024-03-01",
    "themeId": 1,
    "timeId": 1
}
```
