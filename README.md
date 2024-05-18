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
  - GET /reservations-mine HTTP/1.1
    - {
      "reservationId": 1,
      "theme": "테마1",
      "date": "2024-03-01",
      "time": "10:00",
      "status": "예약"
      },