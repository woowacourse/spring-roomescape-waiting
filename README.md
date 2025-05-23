# 방탈출 예약 대기

## 뷰
### 내 예약 페이지
- [x] /reservation-mine 요청 시 내 예약 페이지를 응답한다

## 예약
- [x] 로그인한 사용자의 예약 목록을 조회한다

## 예약 대기
- [ ] 로그인한 사용자의 예약대기 목록을 예약 내역과 함께 조회한다
- [ ] 예약대기 내역을 취소한다
- [ ] 날짜, 시간, 테마 및 멤버가 동일할 경우 중복으로 예약대기를 생성할 수 없다

## API 명세
### 내 예약 목록 조회
request
```json
GET /member/reservations HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```
response
```json
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
