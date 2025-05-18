### 요구 사항
- [x] 정상적으로 동작하지 않는 기능
    - [x] 예약 시간 추가 시 응답 코드 수정
    - [x] 예약 시간 삭제 시 응답 코드 수정
    - [x] 예약 추가 시 응답 코드 수정
    - [x] 예약 삭제 시 응답 코드 수정
- [x] 테마 도메인
    - [x] 테마 저장 api 추가
    - [x] 테마 조회 api 추가
    - [x] 테마 조회 api 추가
- [x] 사용자 관련 기능
    - [x] 사용자는 날짜와 테마를 선택하면 예약 가능한 시간을 확인할 수 있습니다.
    - [x] 사용자는 예약 가능한 시간을 확인하고, 원하는 시간에 예약을 할 수 있습니다.
    - [x] 예약 시 사용자 구분은 어드민과 동일하게 사용자의 이름으로 합니다.
    - [x] /reservation 요청 시 사용자 예약 페이지를 응답합니다.
- [x] 인기 테마 기능
    - [x] 최근 일주일 기준으로 예약이 많은 테마 10개 조회 가능
        - 8일인 경우 1일부터 7일까지의 예약 건수 많은 순서대로 10개의 테마를 조회할 수 있다.
    - [x] `/` 요청 시 인기 테마 페이지를 응답한다.
        - `templates/index.html`
- [x] 내 예약 목록을 조회하는 API 구현
  - [x] 내 예약 목록 호출 시 GET /reservation-mine 요청한다.
  - [x] `reservation-mine.html` 페이지가 응답된다.

### 예외 사항
- [x] 시간 생성 시 시작 시간에 유효하지 않은 값이 입력되었을 때
- [x] 예약 생성 시 예약자명, 날짜, 시간에 유효하지 않은 값이 입력 되었을 때
    - [x] 이름은 null 이거나 비워 있을 수 없다.
    - [x] 날짜는 null 일 수 없다.
    - [x] 시간 번호는 null 일 수 없다.
- [x] 특정 시간에 대한 예약이 존재하는데, 그 시간을 삭제하려 할 때

### 정책
- [x] 지나간 날짜와 시간에 대한 예약 생성은 불가능하다.
- [x] 중복 예약은 불가능하다.


### JPA 전환
- [x] 엔티티 매핑
  - [x] Member
  - [x] Theme
  - [x] Reservation
  - [x] ReservationTime
- [x] 연관관계 매핑
  - [x] Reservation
- [x] 기존 레포지토리를 Spring Data Jpa 레포지토리로 교체
  - [x] ThemeRepository
  - [x] MemberRepository
  - [x] ReservationRepository
  - [x] ReservationTimeRepository

# API 명세

### 예약 목록 조회

```
Request
GET /reservations HTTP/1.1

Response
HTTP/1.1 200 
Content-Type: application/json
[
    {
        "id": Long,
        "name": String,
        "date": LocalDate (YYYY-MM-DD),
        "reservationTime": {
            "id": Long,
            "startAt" : LocalTime (HH:mm)
        },
        "themeName": String
    }
]
```

### 예약 추가

```
Request
Content-Type: application/json
POST /reservations
{
    "name": String,
    "date": LocalDate (YYYY-MM-DD),
    "timeId": Long,
    "themeId": Long
}

Response
Content-Type: application/json
HTTP/1.1 200 
{
    "id": Long
}

```

### 예약 삭제

```
Request
DELETE /reservations/1 HTTP/1.1

Response
HTTP/1.1 200
```

### 시간 추가

```
Request
POST /times HTTP/1.1
content-type: application/json

{
    "startAt": LocalTime (HH:mm)
}

Response
HTTP/1.1 200
Content-Type: application/json

{
    "id": Long
}
```

### 시간 조회

```
Request
GET /times HTTP/1.1

Response
HTTP/1.1 200 
Content-Type: application/json

[
   {
        "id": Long,
        "startAt": LocalTime (HH:mm)
    }
]
```

### 시간 삭제

```
Request
DELETE /times/1 HTTP/1.1

Response
HTTP/1.1 200
```

### 테마 목록 조회

```
Request
GET /themes HTTP/1.1

Response
HTTP/1.1 200 
Content-Type: application/json

[
   {
        "id": Long,
        "name": String,
        "description": String,
        "thumbnail": String
    }
]
```

### 테마 추가

```
Request
POST /themes HTTP/1.1
content-type: application/json

{
    "name": String
    "description": String,
    "thumbnail": String
}

Response
HTTP/1.1 201
Location: /themes/1
Content-Type: application/json

{
    "id": Long
}
```

### 테마 삭제

```
Request
DELETE /themes/1 HTTP/1.1

Response
HTTP/1.1 204
```

### 로그인 요청

```
Request
POST /login HTTP/1.1
content-type: application/json
host: localhost:8080

{
    "password": String,
    "email": String
}

Response
HTTP/1.1 200 OK
Content-Type: application/json
Keep-Alive: timeout=60
Set-Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
```

### 사용자 인증 정보 조회

```
Request
GET /login/check HTTP/1.1
cookie: _ga=GA1.1.48222725.1666268105; _ga_QD3BVX7MKT=GS1.1.1687746261.15.1.1687747186.0.0.0; Idea-25a74f9c=3cbc3411-daca-48c1-8201-51bdcdd93164; token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080

Response
HTTP/1.1 200 OK
Connection: keep-alive
Content-Type: application/json
Date: Sun, 03 Mar 2024 19:16:56 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "name": String
}
```

### 쿠키를 이용한 예약 생성
```
Request
POST /reservations HTTP/1.1
content-type: application/json
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
host: localhost:8080

{
    "date": LocalDate (YYYY-MM-DD),
    "themeId": Long,
    "timeId": Long
}
```

### 내 예약 목록 조회 기능


```
Request
GET /reservations-mine HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080

Response
HTTP/1.1 200 
Content-Type: application/json

[
    {
        "reservationId": Long,
        "theme": String,
        "date": LocalDate (YYYY-MM-DD),
        "time": LocalTime (HH-mm),
        "status": String
    }
]
```