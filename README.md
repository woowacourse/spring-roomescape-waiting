# 방탈출 예약 대기

## 뷰
### 내 예약 페이지
- [x] /reservation-mine 요청 시 내 예약 페이지를 응답한다
- [x] /admin/waiting 요청 시 예약대기 관리 페이지를 응답한다

## 예약
- [x] 로그인한 사용자의 예약 목록을 조회한다

## 예약 대기
- [x] 이미 예약이 존재하는 건에 대한 예약대기 내역을 생성한다
  - [x] 날짜, 시간, 테마 및 멤버가 동일할 경우 중복으로 예약대기를 생성할 수 없다
- [x] 로그인한 사용자의 예약대기 목록을 예약 내역과 함께 조회한다
- [x] 예약대기 내역을 취소한다

## 예약 대기 관리
- [x] 예약대기 목록을 조회한다
- [x] 어드민 권한일 경우 모든 예약대기 내역을 취소할 수 있다
- [x] 예약 취소가 발생할 경우 해당 건에 존재하는 예약대기를 자동으로 승인한다

## API 명세
### 내 예약 목록 조회
request
```
GET /member/reservations HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
```
response
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

### 예약대기 생성
request
```
POST /reservations/waitings HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
Content-Type: application/json

{
    "date": "2025-05-27",
    "timeId": 1,
    "themeId": 1
}
```
response
```
HTTP/1.1 201
Content-Type: application/json
Location: /reservations/waitings/1

{
    "id": 1,
    "date": "2025-05-27",
    "member": {
        "id": 2,
        "name": "회원1"
    },
    "theme": {
        "id": 1,
        "name": "테마",
        "description": "테마입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    },
    "time": {
        "id": 1,
        "startAt": "10:00"
    }
}
```

### 예약대기 삭제
request
```
DELETE /reservations/waitings/1 HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
```
response
```
HTTP/1.1 204
```

### 예약대기 목록 조회
request
```
GET /reservations/waitings HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
```
response
```
HTTP/1.1 200
Content-Type: application/json

[
    {
        "id": 1,
        "date": "2025-05-27",
        "member": {
            "id": 2,
            "name": "회원1"
        },
        "theme": {
            "id": 1,
            "name": "테마 A",
            "description": "테마 A입니다.",
            "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        },
        "time": {
            "id": 1,
            "startAt": "10:00"
        }
    }
]
```
