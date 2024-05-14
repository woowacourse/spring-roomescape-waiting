## 기능 요구사항
- [ ] JPA를 활용하여 데이터베이스에 접근하도록 수정하세요.
  - [x] gradle 의존성 추가  
    - to be: spring-boot-starter-data-jpa
- [ ] 엔티티 매핑  
    - Member 
    - ReservationTime
    - Theme 
    - Reservation
- [ ] 연관관계 매핑
  - Member : Reservation = 1 : N
  - Theme : Reservation = 1 : N
  - ReservationTime : Reservation = 1 : N
  
