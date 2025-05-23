## 🚀 1단계 - JPA 전환

### 요구사항

- build.gradle 파일을 이용하여 다음 의존성을 대체한다.
    - **as is: spring-boot-starter-jdbc**
    - **to be: spring-boot-starter-data-jpa**

- 엔티티 매핑
  각 엔티티의 연관관계를 매핑한다.

## 🚀 2단계 - 내 예약 목록 조회 기능

### 요구사항
- 내 예약 목록을 조회하는 API를 구현한다.

### Request
```http request
GET /reservations-mine HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```

### response
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
## 🚀 3단계 - 예약 대기 기능

### 요구사항
- 예약 대기 요청 기능을 구현한다.
- 예약 대기 취소 기능도 함께 구현한다.
- 내 예약 목록 조회 시 예약 대기 목록도 함께 포함하도록 구현한다.
- 중복 예약이 불가능 하도록 구현한다.

## 🚀 4단계 - 예약 대기 관리
### 요구사항
- 어드민에서 예약 대기 관리 기능을 구현한다.
- 어드민은 예약 대기 목록을 조회가능하도록 구현한다.
- 어드민은 예약 대기를 취소시킬 수 있도록 구현한다.