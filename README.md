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

## 3단계

- [x] 예약 대기 요청
    - [x] 동일한 시간에 중복 예약 불가
    - [x] 동일한 시간에 중복 예약 대기 불가
- [x] 예약 대기 목록 조회
    - [x] 예약 날짜 순서대로 정렬
    - [x] 몇 번째 대기인지 표시
- [x] 예약 대기 취소

---

## API 명세

### 내 예약 목록 조회 (예약 대기 포함)

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
        "id": 1,
        "theme": "테마1",
        "date": "2024-03-01",
        "time": "10:00",
        "status": "예약"
    },
    {
        "id": 2,
        "theme": "테마1",
        "date": "2024-03-01",
        "time": "10:00",
        "status": "1번째 예약대기"
    }
]
```

### 예약 대기 요청

#### request

```http request
POST /waiting
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
Content-Type: application/json

{
    "date": "2021-07-01",
    "themeId": 1,
    "timeId": 1
}
```

#### response

```http request
HTTP/1.1 201
Content-Type: application/json
Location: /waiting/2

{
    "id": 2,
    "theme": "테마1",
    "date": "2024-03-01",
    "time": "10:00",
    "status": "1번째 예약대기"
}
```

### 사용자 예약 대기 취소

#### request

```http request
DELETE /waiting/2
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
```

#### response

```http request
HTTP/1.1 204
```
