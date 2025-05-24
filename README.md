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
GET /reservations/mine HTTP/1.1
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

- 이미 예약이 존재하는 경우, 예약 대기를 요청할 수 있다.
- 대기 중인 예약을 취소할 수 있다.
- 중복 예약 시도 시, 예외를 던진다.
- 내 예약 목록 조회 시, 예약 대기 목록도 함께 포함해야 한다.
- 예약 취소가 발생 시 그 다음 예약 대기자가 있는 경우, 예약 승인을 자동으로 처리한다.

## 🚀 4단계 - 예약 대기 관리

### 요구사항

- 어드민은 예약 대기 목록을 조회할 수 있다.
- 어드민은 예약 대기를 승인시킬 수 있다.
    - 승인 처리 시, 기존에 존재하던 예약은 취소 처리 후, 대기자를 승인시킨다.
- 어드민은 예약 대기를 취소시킬 수 있다.
