# 방탈출 예약 대기

## 1단계 - JPA 전환

### 엔티티 매핑 
- 다른 클래스를 의존하지 않는 클래스 엔티티 설정
  
### 연관관계 매핑
- 다른 클래스에 의존하는 클래스는 연관관계 매핑 추가

## 2단계 - 내 예약 목록 조회 기능
- 내 예약 목록을 조회하는 API를 구현
  
- Request
```
GET /reservations-mine HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
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

### 3단계 - 예약 대기 기능
- 예약 대기 요청 기능을 구현
- 예약 대기 취소 기능도 함께 구현
- 내 예약 목록 조회 시 예약 대기 목록도 함께 포함
- 중복 예약이 불가능 하도록 구현
