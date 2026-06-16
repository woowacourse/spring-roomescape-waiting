# 1-1. 매핑 변환 — JPA 전환 체크리스트

다른 클래스에 의존하지 않는 클래스부터 시작합니다 — Theme, ReservationTime 등.

- [x] #1 **build.gradle 의존성 교체**
  `spring-boot-starter-jdbc`를 `spring-boot-starter-data-jpa`로 교체

- [x] #2 **application.properties JPA 설정 추가**
  `show-sql`, `format_sql`, `ddl-auto=create-drop`, `defer-datasource-initialization=true` 추가

- [ ] #3 **독립 엔티티 매핑 (Theme, ReservationTime)**
  `@Entity`, `@Id`, `@GeneratedValue(strategy = IDENTITY)` 부여

- [ ] #4 **JpaRepository 인터페이스 작성 및 JdbcTemplate 기반 Repository 제거**
  `JpaRepository<T, Long>` 작성, `KeyHolder`·`SimpleJdbcInsert` 등 JdbcTemplate 잔재 제거
