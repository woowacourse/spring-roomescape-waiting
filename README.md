## 방탈출 예약 대기

### 1단계 - JPA 전환
- [x] gradle 의존성 추가 (jdbc -> data-jpa)
- [x] 엔티티 매핑
  - [x] `Theme`
  - [x] `ReservationTime`
  - [x] `Member`
  - [x] `Reservation`
  - [x] `Role`
- [x] 연관관계 매핑
  - [x] `Theme` - `Reservation` : 일대다, 주인은 예약
  - [x] `ReservationTime` - `Reservation` : 일대다, 주인은 예약
  - [x] `Member` - `Reservation` : 일대다, 주인은 예약
  - [x] `Member` - `Role` : 일대다, 주인은 멤버

### 2단계 - 내 예약 목록 조회 기능
- [x] 내 예약 목록 조회 기능
- [x] 새로운 DTO 생성
  - request
    ```
    GET /reservations-mine HTTP/1.1
    cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
    host: localhost:8080    
    ```
  - response
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
- [ ] 화면 응답
  - [2단계 클라이언트 코드 커밋](https://github.com/woowacourse/spring-roomescape-member/commit/849391b31f2dfa359e851e95f5f2a64e21650cc8)
