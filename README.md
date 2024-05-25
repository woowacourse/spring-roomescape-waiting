# 방탈출 사용자 대기

## 1-2단계 요구사항


- 기존의 JDBC Template JPA로 전환하여 데이터베이스에 접근하도록 수정
- 내 예약 목록을 조회하는 API를 구현


  - gradle 의존성 추가
  - build.gradle 파일을 이용하여 다음 의존성을 대체
    - as is: spring-boot-starter-jdbc
    - to be: spring-boot-starter-data-jpa


### **세부 요구사항**

- **0 단계**
- @Dobby-Kim 의 이전 단계에서 작성된 코드를 Base로 하여 미션을 진행한다.

- **1 단계**
- [x] 기존의 JDBC Template를 JPA로 대체한다.

- **2 단계**
- [x] 사용자의 `내 예약 조회` 기능을 추가한다.
  - [x] 클라이언트가 `내 예약` 페이지를 요청할 때, 해당 페이지를 반환한다.
  - [x] 사용자의 본인의 예약 내역 정보를 반환한다.
    - [x] 사용자 쿠키 토큰에 존재하는 정보로 해당 정보를 조회하여 반환한다. 
    - [x] 예약 내용에는 `테마 이름`, `예약 일자`, `예약 시간`, `예약 상태`를 포함한다.




## 추가 API 명세서

- Request
```http request
GET /reservations/mine HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```

- Response
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
    "theme": "테마2",
    "date": "2024-03-01",
    "time": "12:00",
    "status": "예약"
  },
  {
    "id": 3,
    "theme": "테마3",
    "date": "2024-03-01",
    "time": "14:00",
    "status": "예약"
  }
]
```

## 3단계 요구사항

- [x] 예약에 관한 기본 정보(테마, 날짜, 시간)는 ReservationDetail로 분리한다.
- [x] 이미 예약이 있는 테마, 날짜, 시간 인 경우 예약 대기만 된다.
- [ ] 예약 대기도 취소할 수 있다.
- [ ] 내 예약 목록 조회 시, 예약 대기 목록도 포함한다.
- [x] 중복 예약(테마, 날짜, 시간 동일)은 불가능하다.
- [ ] 다른 사용자가 이미 예약한 예약인 경우, 예약 대기가 가능하다.
  - [ ] 테마, 날짜, 시간만 동일할 경우, 예약 대기 가능
  - [ ] 멤버, 테마, 날짜, 시간 모두 동일한 경우 예약 대기 불가능
