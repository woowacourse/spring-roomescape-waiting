- [x] gradle 의존성 추가
- [x] 엔티티 매핑
  - [x] Theme
  - [x] Time
  - [x] Member
  - [x] Reservation
- [x] 연관관계 매핑
- [x] JDBC JPA로 전환
  - [x] Theme
  - [x] Time
  - [x] Member
  - [x] Reservation
- [x] 내 예약 목록 조회 기능 API
  - GET /reservations-mine
    - {
      "reservationId": 1,
      "theme": "테마1",
      "date": "2024-03-01",
      "time": "10:00",
      "status": "예약"
      },
- [x] 예약 대기 기능
  - [x] 예약 대기 요청 기능 API
    - POST /reservations/waiting
    - cookie: token
    - {
      "date": "2024-03-01",
      "themId": 1,
      "timeId": 1
      }
  - [x] 예약 대기 취소 기능 API
    - DELETE /reservations/waiting/1
    - [x] 예약 취소시 첫번째 대기요청 예약으로 자동 변환 
  - [x] 내 예약 목록 조회 시 예약 대기 목록도 함께 포함
- [x] 예약 대기 관리
  - [x] 예약 대기 목록을 조회 기능 API
  - [x] 예약 대기를 취소 기능 API

### 정책
### 예약 정책
- 회원은 동일 날짜에 테마당 하나의 예약 생성 가능하다.
### 예약 대기 정책
- 회원은 동일 날짜, 테마에 예약했을 시, 예약 대기가 불가능하다.
- 회원은 동일 날짜, 테마에 예약 대기했을 시, 예약이 불가능하다.
- 회원은 동일 날짜, 테마, 시간에 예약대기를 1개만 생성 가능하다.
- 예약 대기가 1순위인 경우, 앞 예약이 취소되면 자동으로 예약으로 전환된다.