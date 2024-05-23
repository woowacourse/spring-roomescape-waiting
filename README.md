# 방탈출 예약 관리

## API 명세

| Role     | Method | Endpoint                                                | Description           | File Path                              | Controller Type   |
|----------|--------|---------------------------------------------------------|-----------------------|----------------------------------------|-------------------|
|          | GET    | `/`                                                     | 인기 테마 페이지 요청          | `templates/index.html`                 | `@Controller`     |
|          | GET    | `/reservation`                                          | 사용자 예약 페이지 요청         | `templates/reservation.html`           | `@Controller`     |
|          | GET    | `/reservation-mine`                                     | 내 예약 조회 페이지 요청        | `templates/reservation-mine.html`      | `@Controller`     |
| `ADMIN`  | GET    | `/admin`                                                | 어드민 페이지 요청            | `templates/admin/index.html`           | `@Controller`     |
| `ADMIN`  | GET    | `/admin/reservation`                                    | 예약 관리 페이지 요청          | `templates/admin/reservation-new.html` | `@Controller`     |
| `ADMIN`  | GET    | `/admin/reservationTime`                                | 예약 시간 관리 페이지 요청       | `templates/admin/reservationTime.html` | `@Controller`     |
| `ADMIN`  | GET    | `/admin/theme`                                          | 테마 관리 페이지 요청          | `templates/admin/theme.html`           | `@Controller`     |
|          | GET    | `/login`                                                | 로그인 페이지 요청            | `templates/login.html`                 | `@Controller`     |
|          | POST   | `/login`                                                | 로그인 요청                |                                        | `@RestController` |
|          | GET    | `/login/check`                                          | 인증 정보 조회              |                                        | `@RestController` |
| `MEMBER` | GET    | `/token-reissue`                                        | JWT 토큰 재발급            |                                        | `@RestController` |
| `ADMIN`  | GET    | `/reservations`                                         | 예약 정보 조회              |                                        | `@RestController` |
| `MEMBER` | GET    | `/reservations-mine`                                    | 내 예약 정보 조회            |                                        | `@RestController` |
| `ADMIN`  | GET    | `/reservations/search?themeId&memberId&dateFrom&dateTo` | 예약 정보 조건 검색           |                                        | `@RestController` |
|          | GET    | `/reservations/themes/{themeId}/reservationTimes?date`  | 특정 날짜의 특정 테마 예약 정보 조회 |                                        | `@RestController` |
| `MEMBER` | POST   | `/reservations`                                         | 예약 추가                 |                                        | `@RestController` |
|          | DELETE | `/reservations/{id}`                                    | 예약 취소                 |                                        | `@RestController` |
|          | GET    | `/reservationTimes`                                     | 예약 시간 조회              |                                        | `@RestController` |
|          | DELETE | `/reservationTimes/{id}`                                | 예약 시간 추가              |                                        | `@RestController` |
|          | POST   | `/reservationTimes`                                     | 예약 시간 삭제              |                                        | `@RestController` |
|          | GET    | `/themes`                                               | 테마 정보 조회              |                                        | `@RestController` |
|          | GET    | `/themes/top?today`                                     | 특정 기간의 인기 테마 조회       |                                        | `@RestController` |
|          | POST   | `/themes`                                               | 테마 추가                 |                                        | `@RestController` |
|          | DELETE | `/themes/{id}`                                          | 테마 삭제                 |                                        | `@RestController` |

---

### 로그인 요청 API

- Request

```
POST /login HTTP/1.1
Content-Type: application/json

{
        "password": "password",
        "email": "admin@email.com"
}
```

- Response

```
HTTP/1.1 200 
Content-Type: application/json
Keep-Alive: timeout=60
Set-Cookie: accessToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
Set-Cookie: refreshToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
```

---

### JWT 토큰 재발급 API

- Request

```
GET /token-reissue HTTP/1.1
Cookie:
accessToken=eyJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJJZCI6MSwiaWF0IjoxNzE1NjE1OTMyLCJleHAiOjE3MTU2MTc3MzJ9.nfu6IZlKBccnmBbMtKDTP-5TbNWUMhcVY_ee09aNwhE; 
refreshToken=eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3MTU2MTU5MzIsImV4cCI6MTcxNTYxNzczMn0.U0ZhUSmvOjCAAKD6-9F8dYO1K-LskyxPnMYe7ZJGaQA
```

- Response

```
HTTP/1.1 200 
Content-Type: application/json
Keep-Alive: timeout=60
Set-Cookie: accessToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
Set-Cookie: refreshToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
```

---

### 인증 정보 조회 API

- Request

```
GET /login/check HTTP/1.1
Cookie: _ga=GA1.1.48222725.1666268105; _ga_QD3BVX7MKT=GS1.1.1687746261.15.1.1687747186.0.0.0; Idea-25a74f9c=3cbc3411-daca-48c1-8201-51bdcdd93164; 
accessToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM;
```

- Response

```
HTTP/1.1 200 OK
Connection: keep-alive
Content-Type: application/json
Date: Sun, 03 Mar 2024 19:16:56 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

"data": {
    "name": "이름"
}
```

---

### 예약 / 예약 대기 정보 조회 API

- Request

```
GET /reservations HTTP/1.1
```

- Response

```
HTTP/1.1 200 
Content-Type: application/json

[
    {
        "id": 1,
        "name": "브라운",
        "date": "2023-08-05",
        "reservationTime": {
            "id": 1,
            "startAt": "10:00"
        }
    }
]
```

---

### 내 예약 정보 조회 API

- Request

```
GET /reservations-mine HTTP/1.1
Cookie: accessToken=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM;
```

- Response

```
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

---

### 예약 정보 조회 API

- Request

```
GET /reservations/search?themeId=1&memberId=1&dateFrom='2024-05-05'&dateTo='2024-05-08' HTTP/1.1
GET /reservations/search?themeId=1&memberId=1&dateFrom='2024-05-05' HTTP/1.1
GET /reservations/search?themeId=1&memberId=1 HTTP/1.1
GET /reservations/search?themeId=1 HTTP/1.1
GET /reservations/search HTTP/1.1
```

- Response

```
HTTP/1.1 200 
Content-Type: application/json

[
    {
        "id": 1,
        "name": "브라운",
        "date": "2023-08-05",
        "reservationTime": {
            "id": 1,
            "startAt": "10:00"
        }
    }
]
```

---

### 특정 날짜의 특정 테마 예약 정보 조회

- Request

```
GET /reservations/themes/1/reservationTimes?date=2024-12-31 HTTP/1.1
```

---

- Response

```
[
    {
        "timeId": 1,
        "startAt": "17:00",
        "alreadyBooked": false
    },
    {
        "timeId": 2,
        "startAt": "20:00",
        "alreadyBooked": false
    }
]
```

---

### 예약 추가 API

- Request

```
POST /reservations HTTP/1.1
content-type: application/json

{
    "date": "2023-08-05",
    "name": "브라운",
    "timeId": 1
}
```

- Response

```
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "name": "브라운",
    "date": "2023-08-05",
    "reservationTime" : {
        "id": 1,
        "startAt" : "10:00"
    }
}
```

---

### 예약 취소 API

- Request

```
DELETE /reservations/1 HTTP/1.1
```

- Response

```
HTTP/1.1 204
```

---

### 예약 시간 조회 API

- Request

```
GET /reservationTimes HTTP/1.1
```

- Response

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

---

### 예약 시간 추가 API

- Request

```
POST /reservationTimes HTTP/1.1
content-type: application/json

{
    "startAt": "10:00"
}
```

- Response

```
HTTP/1.1 201
Content-Type: application/json

{
    "id": 1,
    "startAt": "10:00"
}
```

---

### 예약 시간 삭제 API

- Request

```
DELETE /reservationTimes/1 HTTP/1.1
```

- Response

```
HTTP/1.1 204
```

---

### 테마 정보 조회 API

- Request

```
GET /themes HTTP/1.1
```

- response

```
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

---

### 특정 기간의 인기 테마 조회 API

- Request

```
GET /themes/top?today=2024-01-01 HTTP/1.1
```

- response

```
HTTP/1.1 200
Content-Type: application/json

[
    {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    },
    ...8개 생략
    {
        "id": 10,
        "name": "레벨10 탈출",
        "description": "우테코 레벨10를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    }
]
```

---

### 테마 추가 API

- Request

```
POST /themes HTTP/1.1
content-type: application/json

{
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

- response

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

---

### 테마 삭제 API

- Request

```
DELETE /themes/1 HTTP/1.1
```

- response

```
HTTP/1.1 204
```


### 예약 정책

모든 예약은 대기중으로 생성된다.
만약 저장된 예약중에서 (X날,Y시간,Z테마)를 갖는 `예약된` 예약이 없다면
자동으로 `예약된`상태로 변경 후 저장한다.

대기중인 예약과 완료된 예약이 혼재되어 성능상의 불이익이 있을 수도 있으나
가장 심플한 방법으로 일단 구현해보고 리펙터링 해보자

아직까진 테이블을 두 개로 가져가야할 이유를 모르겠다. 도메인도 또 만들어야하고..

예약 API와 예약대기 API의 구분이 없다.
애초에 구분할 필요가 있을까?

에약할 수 있는데 일부러 예약 대기를 걸어놓는 경우는 이상하다.

모든 예약을 대기중으로 생성해도 될까?
즉, 클라이언트의 입장에서 예약과 예약 대기를 구분하지 못하게 해도 될까?

예약이라는 도메인에 대해 생각을 해보면,
사용자가 원하는건 예약 대기가 아닌 예약이다.
예약대기는 차선책일 뿐이다. 예약이 없는데 예약 대기를 한다는 것은 식당에서 줄이 없는데 혼자서 줄을 서고 있는 꼴이다.

입구가 하나밖에 없는 식당을 생각해보면,
수많은 손님들이 몰려올 때 손님이 원하는건 앉아서 밥을 먹는것이지 줄서서 기다리는건 아니다.
따라서, 예약 대기의 제어권을 개발자가 갖는 것이 맞다. 다른 말로, 예약과 예약 대기에 대한 API를 따로 구분할 필요가 없다.

유효하지 않은 예약(시간이 지난 예약)에 대해서는 기각하면 되고,
유효한 예약에 대해서는 모든 예약을 `대기`상태로 생성한다.

요청이 들어왔을 때, 들어온 요청은 순서가 있다고 가정한다. 즉, 동시성 문제는 일단 배제한다.
`대기`상태의 요청이 들어오면, 컨트롤러를 지나 비즈니스 로직을 담당하는 서비스에서 예약을 어떻게 처리할 것인지 결정한다.

예약 상태는 몇가지로 구분할 수 있다.

1. X일, Y시간, Z테마가 이미 예약되어 있는 경우
   1.1 사용자가 예약 대기요청을 한 경우 <- (X일, Y시간, Z테마)인 예약을 생성한다. 
   1.2 사용자가 예약을 한 경우 <- 유효하지 않은 경우로 취급한다. 예외 발생 

2. X일, Y시간, Z테마가 예약되어 있지 않은 경우
   1.1 사용자가 예약 대기요청을 한 경우 <- 유효하지 않은 경우로 취급한다. 예외 발생
   1.2 사용자가 예약을 한 경우 <- (X일, Y시간, Z테마)인 예약을 생성한다.


API를 구분하지 않을거면 예약 대기 버튼과 예약 버튼은 둘 다 열어놓으면 안된다. <- 클라이언트단에서 버튼을 비활성화해야
하고, 만약 그러한 요청이 서버까지 들어왔다면, 예외를 던지는게 맞다.
