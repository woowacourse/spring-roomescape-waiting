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
