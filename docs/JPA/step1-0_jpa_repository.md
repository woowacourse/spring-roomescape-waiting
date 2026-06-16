# 1-0. JdbcTemplate → JPA Repository 전면 교체

JdbcTemplate 기반 ReservationDao를 JPA Repository로 전면 교체합니다.

## 체크리스트

- [x] #1 `ReservationRepository` 생성 + CRUD · 파생 쿼리 구현
  `JpaRepository<Reservation, Long>` 생성. 기본 CRUD(`save`/`findById`/`deleteById`), `countByStatus`, Pageable 기반 `findAllByStatus`, 그리고 status 인식 exist/find 파생 쿼리(`existsByTime_Id`, `existsByTheme_Id`, `existsByDateAndTime_IdAndTheme_IdAndStatus`, `existsByDateAndTime_IdAndTheme_IdAndNameAndStatus`, `findByNameAndStatus`, `findByIdAndStatus`, `existsByDateAndTime_IdAndTheme_IdAndNameAndStatus` (WAITING)) 구현.

- [x] #2 비관적 락 · JPQL `@Query` · `@Modifying` 구현
  `@Lock(PESSIMISTIC_WRITE)` for `findByIdForUpdate`, `findWaitingByIdForUpdate`, `findFirstWaitingByDateAndTimeIdAndThemeIdForUpdate`. JPQL `@Query`로 `findFirstWaitingByDateAndTimeIdAndThemeId`. `@Modifying @Query`로 `updateStatus`, `updateDateAndTime`.

- [x] #3 ROW_NUMBER 기반 대기 순번 쿼리 구현 (native SQL)
  `findAllWaitingByName`과 `findWaitingWithNumberById`는 H2 호환 native SQL로 구현. 결과를 `ReservationWaiting`으로 변환하는 로직 포함.

- [x] #4 서비스 주입 교체 + `ReservationDao` 제거
  서비스 4곳(`ReservationService`, `ReservationWaitingService`, `ReservationTimeService`, `ThemeService`)에서 `ReservationDao` → `ReservationRepository`로 교체. `ReservationDao.java` 삭제.
