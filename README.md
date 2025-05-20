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
- [x] 화면 응답
  - [2단계 클라이언트 코드 커밋](https://github.com/woowacourse/spring-roomescape-member/commit/849391b31f2dfa359e851e95f5f2a64e21650cc8)

### 3단계 - 예약 대기 기능
- [ ] 예약 대기 요청 기능
  - request
    ```
    POST /reservations/waitings HTTP/1.1
    cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
    content-type: application/json
    
    {
        "date": "2025-04-29",
        "theme": 1,
        "time": 1
    }
    ```
  - response
    ```
    HTTP/1.1 201
    Location: /reservations/waitings/1
    Content-Type: application/json
    ```
- [ ] 예약 대기 취소 기능
  - request
    ```
    DELETE /reservations/waitings/1 HTTP/1.1
    cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
    ```
  - response
    ```
    HTTP/1.1 204
    ```
- [ ] 내 예약 목록 조회 시 예약 대기 목록 포함하도록 수정
  - [ ] 심화: 몇 번째 대기인지 함께 표시 
  - request
    ```
    GET /reservations-mine HTTP/1.1
    cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
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
          },
          {
              "reservationId": 4,
              "theme": "테마2",
              "date": "2024-03-02",
              "time": "12:00",
              "status": "2번째 예약대기"
          }
      ]
      ```
- [ ] 중복 예약 불가능 기능
- [ ] 화면 응답
  - [3단계 클라이언트 코드 커밋](https://github.com/woowacourse/spring-roomescape-member/commit/ce6b8eef9072409e89b91411b3ef144d4de0b48c)