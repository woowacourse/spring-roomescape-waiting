## 기능 요구사항
- [x] JPA를 활용하여 데이터베이스에 접근하도록 수정하세요.
  - [x] gradle 의존성 추가  
    - to be: spring-boot-starter-data-jpa
  - [x] 엔티티 매핑  
      - Member 
      - ReservationTime
      - Theme 
      - Reservation
- [x] 연관관계 매핑
  - Member : Reservation = 1 : N
  - Theme : Reservation = 1 : N
  - ReservationTime : Reservation = 1 : N
  
<br>

- [x] 내 예약 목록 조회 API
  - [x] `/reservation/mine` 호출 시 내 예약 목록 조회 페이지 응답
  - [x] 테마, 날짜, 시간, 상태 조회
- [ ] 관리자가 예약을 생성하는 경우 name 값을 string으로 전달
