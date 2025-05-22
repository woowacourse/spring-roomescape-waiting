# 방탈출 예약 대기

# 화면

- 로그인 페이지: localhost:8080/login
- 회원가입 페이지: localhost:8080/signup
- 인기 테마 페이지: localhost:8080
- 사용자 예약 페이지: localhost:8080/reservation
- 관리자 메인 페이지: localhost:8080/admin
- 예약 관리 페이지: localhost:8080/admin/reservation
- 시간 관리 페이지: localhost:8080/admin/time
- 내 예약 페이지: localhost:8080/reservation-mine
- 관리자 대기 관리 페이지: localhost:8080/admin/waiting

# API 명세

## Admin

<details>
<summary>관리자 예약 추가</summary>
<div markdown="1">

```
Request
Content-Type: application/json
POST /api/admin/reservations
{
    "memberId": number,
    "date": string (YYYY-MM-DD),
    "timeId": number,
    "themeId": number
}

Response
Content-Type: application/json
HTTP/1.1 200 
{
    "id": number,
    "name": string,
    "date": string (YYYY-MM-DD),
    "timeSlot": {
        "id": number,
        "startAt" : string (HH:mm)
    },
    "themeName" : string
}
```

</div>
</details>

<details>
<summary>관리자 예약 목록 조회</summary>
<div markdown="1">

모든 예약을 조회합니다.

```
Request
GET /api/admin/reservations HTTP/1.1
Query Parameters: 
    - memberId
    - themeId
    - dateFrom 
    - dateTo
cookie: token={accessToken}

Response
HTTP/1.1 200 
Content-Type: application/json
[
    {
        "id": number,
        "name": string,
        "date": string (YYYY-MM-DD),
        "timeSlot": {
            "id": number,
            "startAt" : string (HH:mm)
        },
        "themeName": string
    }
]
```

</div>
</details>

<details>
<summary>관리자 대기 목록 조회</summary>
<div markdown="1">

```
Request
GET /api/admin/waitings HTTP/1.1
cookie: token={accessToken}

Response
HTTP/1.1 200 
Content-Type: application/json
[
    {
        "id": number,
        "name": string,
        "date": string (YYYY-MM-DD),
        "timeSlot": {
            "id": number,
            "startAt" : string (HH:mm)
        },
        "themeName": string
    }
]
```

</div>
</details>

<details>
<summary>관리자 대기 승인</summary>
<div markdown="1">

기존의 예약을 삭제하고 가장 빠른 대기를 예약으로 전환합니다.

```
Request
POST /api/admin/waitings/{waitingId}

Response
HTTP/1.1 204
```

</div>
</details>

<details>
<summary>관리자 대기 거절</summary>
<div markdown="1">

대기를 거절하고 삭제합니다.

```
Request
DELETE /api/admin/waitings/{waitingId}

Response
HTTP/1.1 204
```

</div>
</details>

## Member

<details>
<summary>회원 가입</summary>
<div markdown="1">

새로운 사용자를 생성합니다.

```
Request
POST /api/members HTTP/1.1
content-type: application/json

{
    "name": string,
    "email": string,
    "password": string
}

Response
HTTP/1.1 200
Content-Type: application/json

{
    "id": number,
    "name": string,
    "email": string
}
```

</div>
</details>

## Auth

<details>
<summary>로그인</summary>
<div markdown="1">

```
Request
POST /api/auth/login HTTP/1.1
content-type: application/json

{
    "email": string,
    "password": string
}

Response
HTTP/1.1 200
Content-Type: application/json
Set-Cookie: token={accessToken}
```

</div>
</details>

<details>
<summary>로그아웃</summary>
<div markdown="1">

```
Request
POST /api/auth/logout HTTP/1.1

Response
HTTP/1.1 200
Set-Cookie: token=
```

</div>
</details>

## Reservation

<details>
<summary>예약 생성</summary>
<div markdown="1">

```
Request
Content-Type: application/json
POST /api/reservations
{
    "date": string (YYYY-MM-DD),
    "timeId": number,
    "themeId": number
}

Response
Content-Type: application/json
HTTP/1.1 200 
{
    "id": number,
    "name": string,
    "date": string (YYYY-MM-DD),
    "timeSlot": {
        "id": number,
        "startAt" : string (HH:mm)
    },
    "themeName" : string
}
```

</div>
</details>

<details>
<summary>예약 취소</summary>
<div markdown="1">

예약을 삭제합니다. 대기가 있을 경우 자동으로 가장 빠른 대기를 예약으로 전환합니다.

```
Request
DELETE /api/reservations/{id} HTTP/1.1

Response
HTTP/1.1 200
```

</div>
</details>

<details>
<summary>나의 예약 목록 조회</summary>
<div markdown="1">

```
Request
GET /api/reservation/my
cookie: token={accessToken}

Response
HTTP/1.1 200 
Content-Type: application/json

{
    "id": number,
    "name": string,
    "date": string (YYYY-MM-DD),
    "timeSlot": {
        "id": number,
        "startAt" : string (HH:mm)
    },
    "themeName" : string
}
```

</div>
</details>

## TimeSlot

<details>
<summary>예약 시간 생성</summary>
<div markdown="1">

```
Request
POST /api/times HTTP/1.1
content-type: application/json

{
    "startAt": string (HH:mm)
}

Response
HTTP/1.1 200
Content-Type: application/json

{
    "id": number,
    "startAt": string (HH:mm)
}
```

</div>
</details>

<details>
<summary>예약 시간 목록 조회</summary>
<div markdown="1">

```
Request
GET /api/times HTTP/1.1

Response
HTTP/1.1 200 
Content-Type: application/json

[
   {
        "id": number,
        "startAt": string (HH:mm)
    }
]
```

</div>
</details>

<details>
<summary>예약 시간 삭제</summary>
<div markdown="1">

```
Request
DELETE /api/times/1 HTTP/1.1

Response
HTTP/1.1 200
```

</div>
</details>

<details>
<summary>예약 가능한 시간 조회</summary>
<div markdown="1">
### 

```
Request
GET /api/times/theme/{themeId}?date={date} HTTP/1.1

Response
HTTP/1.1 200 
Content-Type: application/json

[
   {
        "id": number,
        "startAt": string (HH:mm)
        "alreadyBooked": boolean
    }
]
```

</div>
</details>

## Theme

<details>
<summary>테마 생성</summary>
<div markdown="1">

```
Request
POST /api/themes HTTP/1.1
content-type: application/json

{
    "name": string,
    "description": string,
    "thumbnail": string
}

Response
HTTP/1.1 201
Location: /themes/1
Content-Type: application/json

{
    "id": number,
    "name": string
    "description": string,
    "thumbnail": string
}
```

</div>
</details>

<details>
<summary>테마 목록 조회</summary>
<div markdown="1">

```
Request
GET /api/themes HTTP/1.1

Response
HTTP/1.1 200 
Content-Type: application/json

[
   {
        "id": number,
        "name": string,
        "description": string,
        "thumbnail": string
    }
]
```

</div>
</details>

<details>
<summary>테마 삭제</summary>
<div markdown="1">

### 테마 삭제

```
Request
DELETE /api/themes/1 HTTP/1.1

Response
HTTP/1.1 204
```

</div>
</details>

<details>
<summary>인기 테마 조회</summary>
<div markdown="1">

```
Request
GET /api/theme/rank

Response
HTTP/1.1 200 
Content-Type: application/json

[
   {
        "id": number
        "name": string
        "description": string
        "thumbnail": string
    }
]
```

</div>
</details>

## Waiting

<details>
<summary>예약 대기 생성</summary>
<div markdown="1">

```
Request
Content-Type: application/json
POST /api/waitings
{
    "date": string (YYYY-MM-DD),
    "timeId": number,
    "themeId": number
}

Response
Content-Type: application/json
HTTP/1.1 201
{
    "id": number,
    "name": string,
    "date": string (YYYY-MM-DD),
    "timeSlot": {
        "id": number,
        "startAt" : string (HH:mm)
    },
    "themeName" : string
}
```

</div>
</details>

<details>
<summary>나의 대기 목록 조회</summary>
<div markdown="1">

```
Request
GET /api/waitings/my
cookie: token={accessToken}

Response
HTTP/1.1 200 
Content-Type: application/json

{
    "id": number,
    "name": string,
    "date": string (YYYY-MM-DD),
    "timeSlot": {
        "id": number,
        "startAt" : string (HH:mm)
    },
    "themeName" : string
    "rank" : number
}
```

</div>
</details>