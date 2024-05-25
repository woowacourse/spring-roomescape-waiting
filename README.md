# 기능 구현

## 1단계 - JPA 전환

- [x] gradle 의존성을 추가한다
- [x] 엔티티를 매핑한다
- [x] 연관관계를 매핑한다
- [x] Dao 클래스를 JpaRepository를 상속받는 Repository로 대체한다

## 2단계 - 내 예약 목록 조회 기능

- [x] `내 예약 목록` 조회 API를 구현한다
- [x] 내 예약 확인 페이지를 제공한다
    - `GET /reservation-mine` 요청시 `reservation-mine.html` 페이지 응답

## 3단계 - 예약 대기 기능

- [x] 예약 대기 도메인을 추가한다
- [x] `예약 대기` 추가 API를 구현한다
    - [x] 본인이 예약한 예약에 대해선 예약 대기를 추가할 수 없음
    - [x] 같은 예약에 대해선 여러 번 예약 대기를 추가할 수 없음
    - [x] 존재하지 않는 예약에 대해선 예약 대기를 추가할 수 없음
    - [x] 지난 예약에 대해선 예약 대기를 추가할 수 없음
- [x] `예약 대기` 취소 API를 구현한다
    - [x] 본인의 예약 대기만 취소할 수 있음
- [x] `내 예약 목록` 조회 API 응답에 `예약 대기 목록`도 포함한다
    - [x] 예약 대기 상태에 몇 번째 대기인지도 함께 표시 (같은 테마, 날짜, 시간의 예약 대기 중 내 예약 대기보다 빨리 생성된 갯수를 함께 응답)
    - [x] 시간 순대로 정렬
- [x] 클라이언트 코드를 수정한다

## 4단계 - 예약 대기 관리

- [ ] 관리자용 예약 대기 목록 조회 API를 구현한다
- [ ] 관리자용 예약 대기 취소 API를 구현한다
- [ ] 예약 취소 API 요청 시, 예약 대기자가 자동으로 예약 승인이 된다

# API 명세

## 예외

Response

```
{
  "message": "토큰이 유효하지 않습니다."
}
```

## 인증

### 로그인 API

Request

```
POST /login
Content-Type: application/json

{
  "password": "password",
  "email": "admin@email.com"
}
```

Response

```
HTTP/1.1 200 OK
Content-Type: application/json
Set-Cookie: token=hello.example.token; Path=/; HttpOnly
```

### 로그아웃 API

Request

```
POST /logout
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 200 OK
```

### 인증 정보 조회 API

Request

```
GET /login/check
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "name": "어드민"
}
```

### 회원가입 API

Request

```
POST /signup
Content-Type: application/json

{
  "email": "admin@email.com",
  "password": "password",
  "name": "어드민"
}
```

Response

```
HTTP/1.1 201 OK
Content-Type: application/json

{
  "id": 1,
  "name": "브라운",
  "email": "admin@email.com"
}
```

## 사용자

### 사용자 목록 조회 API (접근 권한: 관리자)

Request

```
GET /members
```

Response

```
HTTP/1.1 200
Cookie: token=hello.example.token
Content-Type: application/json

{
  "members": [
    {
      "id": 1,
      "name": "관리자",
      "email": "admin@gmail.com"
    }
  ]
}
```

## 예약

### 예약 목록 조회 API (접근 권한: 관리자)

Request

```
GET /reservations?themeId={$}&memberId={$}&dateFrom={$}&dateTo={$}
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 200
Content-Type: application/json

{
  "reservations": [
    {
      "id": 1,
      "name": "관리자",
      "date": "2024-08-05",
      "time": {
        "id": 1,
        "startAt": "10:00"
      },
      "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
      }
    }
  ]
}
```

### 내 예약 목록 조회 API

Request

```
GET /reservations-mine
cookie: token=hello.example.token
```

Response

```
HTTP/1.1 200 
Content-Type: application/json

{
  "reservations": [
    {
      "reservationId": 1,
      "theme": "레벨2 탈출",
      "date": "2024-08-05",
      "time": "10:00:00",
      "status": "예약"
    }
  ]
}
```

### 예약 추가 API

Request

```
POST /reservations
Cookie: token=hello.example.token
Content-Type: application/json

{
  "date": "2024-08-06",
  "themeId": 1,
  "timeId": 1
}
```

Response

```
HTTP/1.1 201
Content-Type: application/json

{
  "id": 2,
  "name": "관리자",
  "date": "2024-08-06",
  "time": {
    "id": 1,
    "startAt": "10:00"
  },
  "theme": {
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  }
}
```

### 관리자용 예약 추가 API (접근 권한: 관리자)

Request

```
POST /admin/reservations
Cookie: token=hello.example.token
Content-Type: application/json

{
  "date": "2024-08-07",
  "themeId": 1,
  "timeId": 1,
  "memberId": 1
}
```

Response

```
HTTP/1.1 201
Content-Type: application/json

{
  "id": 3,
  "name": "관리자",
  "date": "2024-08-07",
  "time": {
    "id": 1,
    "startAt": "10:00"
  },
  "theme": {
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  }
}
```

### 예약 삭제 API (접근 권한: 관리자)

Request

```
DELETE /reservations/1
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 204
```

## 예약 대기

### 예약 대기 추가 API

Request

```
POST /reservations/waitings
Cookie: token=hello.example.token
Content-Type: application/json

{
  "date": "2023-08-05",
  "themeId": 1,
  "timeId": 1
}
```

Response

```
HTTP/1.1 201
Content-Type: application/json

{
  "waitingId": 1,
  "reservationId": 1
}
```

### 예약 대기 취소 API

Request

```
DELETE /reservations/waitings/1
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 204
```

## 예약 시간

### 예약 시간 목록 조회 API (접근 권한: 관리자)

Request

```
GET /times
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 200 
Content-Type: application/json

{
  "times": [
    {
      "id": 1,
      "startAt": "10:00"
    }
  ]
}
```

### 예약 가능 시간 목록 조회 API

Request

```
GET /times/available?date={&}&timeId={$}
```

Response

```
HTTP/1.1 200 
Content-Type: application/json

{
  "times": [
    {
      "id": 1,
      "startAt": "10:00",
      "alreadyBooked": false
    }
  ]
}
```

### 예약 시간 추가 API (접근 권한: 관리자)

Request

```
POST /times
Cookie: token=hello.example.token
Content-Type: application/json

{
  "startAt": "10:00"
}
```

Response

```
HTTP/1.1 201
Content-Type: application/json

{
  "id": 2,
  "startAt": "11:00"
}
```

### 예약 시간 삭제 API (접근 권한: 관리자)

Request

```
DELETE /times/1
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 204
```

## 테마

### 테마 목록 조회 API

Request

```
GET /themes
```

Response

```
HTTP/1.1 200 
Content-Type: application/json

{
  "themes": [
    {
      "id": 1,
      "name": "레벨2 탈출",
      "description": "우테코 레벨2를 탈출하는 내용입니다.",
      "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    }
  ]
}
```

### 인기 테마 목록 조회 API

Request

```
GET /themes/popular
```

Response

```
HTTP/1.1 200
Content-Type: application/json

{
  "themes": [
    {
      "id": 1,
      "name": "레벨2 탈출",
      "description": "우테코 레벨2를 탈출하는 내용입니다.",
      "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    }
  ]
}
```

### 테마 추가 API (접근 권한: 관리자)

Request

```
POST /themes
Cookie: token=hello.example.token
Content-Type: application/json

{
  "name": "레벨2 탈출",
  "description": "우테코 레벨2를 탈출하는 내용입니다.",
  "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

Response

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

### 테마 삭제 API (접근 권한: 관리자)

Request

```
DELETE /themes/1
Cookie: token=hello.example.token
```

Response

```
HTTP/1.1 204
```
