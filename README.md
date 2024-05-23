# 기능 요구사항

## 패어와 정한 컨벤션

- 클래스를 정의한 뒤 다음 줄은 공백으로 한다.
- @DisplayName ➡️ @Test 순으로 작성한다.
- final 키워드는 필드에만 적용한다.

## 1단계 JPA 전환

- [x] gradle 의존성 추가
    - as is: spring-boot-stater-jdbc
    - to be: spring-boot-starter-data-jpa
- [x] 다른 클래스를 의존하지 않는 클래스 엔티티 설정
  - [x] Member
  - [x] Theme
  - [x] TimeSlot
- [x] Reservation 클래스 엔티티 설정

## 2단계 - 내 예약 목록 조회 기능

- [x] 2단계 클라이언트 코드 추가
  - reservation-mine.html
  - reservation-mine.js
- [x] 내 예약 조회 페이지 응답 추가
- [x] 내 예약 조회 API 추가

## 3단계 - 예약 대기 기능 추가

- [x] 3단계 클라이언트 코드 추가
  - reservation.html
  - user-reservation.js
- [ ] 예약 대기 생성 API 추가
  - [ ] 중복 예약은 허용하지 않는다.
- [ ] 예약 대기 조회 API 추가
- [ ] 예약 대기 삭제 API 추가
