# 요구 사항 목록

## 1단계

- [x] JPA 전환
  - [x] `spring-boot-starter-data-jpa` gradle 의존성 추가
  - [x] 엔티티 매핑
  - [x] Reservation 연관관계 매핑

## 2단계

 - [x] 내 예약 목록 조회
   - [x] 내 예약 목록 조회 페이지 추가
   - [x] 내 예약 목록 데이터 응답

### API 명세

#### request

```http request
GET /reservations-mine HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```

#### response

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
    }
]
```
