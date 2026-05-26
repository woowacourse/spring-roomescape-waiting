# Cycle 2 - PR Review v1 - 수정 사항

---

## ✅ 리팩토링 할 것 목록

- [x] **1. List 응답을 래퍼 객체로 감싸기**
  - `List<T>`를 직접 반환하는 모든 컨트롤러 엔드포인트
    - `AdminReservationController` → `AdminReservationsResponse`
    - `ReservationController` → `ReservationsResponse`
    - `ReservationTimeController` → `ReservationTimesResponse`
    - `ThemeController` → `ThemesResponse`, `AvailableReservationTimesResponse`
  - 이유: 배열을 직접 반환하면 이후 `totalCount`, `page` 등 필드 추가 시 API 스펙이 깨짐
- [x] **2. `Reservation` 생성/복원 분리 + `created_at` 테이블 추가**
    1. `reservation` 테이블에 `created_at` 컬럼 추가 — `createdAt`을 실제 도메인 데이터로 저장
    2. `Reservation.create()` (신규 생성, `date >= createdAt` 검증) / `Reservation.restore()` (DB 복원, 검증 없음) 정적 팩토리로 분리
    3. DAO가 `Reservation`을 받아 저장 후 ID 포함된 `Reservation` 반환 — 서비스에서 검증용 객체를 따로 만들고 버리는 구문 제거
- [x] **3. DAO 테스트에서 Spring Boot 컨텍스트 제거**
  - `DaoTest` 추상 클래스 삭제
  - 각 DAO 테스트에 `EmbeddedDatabaseBuilder`로 H2 직접 세팅 — 테스트마다 새 DB 인스턴스
  - `@Autowired` 제거, DAO를 `new JdbcTemplate(dataSource)`로 직접 주입
  - 웹 계층, Spring 컨텍스트 자체 없음, `truncate.sql` 불필요
- [x] **4. Tautological test 제거 — Mock 출력을 그대로 검증하는 테스트**
  - `ReservationServiceTest` — `findAll_전체_조회`, `findAllByName_이름으로_조회`
  - `ReservationTimeServiceTest` — `findAll_전체_조회`, `save_정상_시간_저장`
  - `ThemeServiceTest` — `getAllThemes_전체_테마_조회`, `getPopularThemes_인기_테마_조회`, `getAvailableTimes_예약_가능_시간_조회`, `save_정상_테마_저장`
- [x] **5. data.sql 하드코딩 의존 테스트 개선**
  - `ReservationDaoTest` — `findAll`(19→`isNotEmpty`), `delete`(18→before-1)
  - `ReservationTimeDaoTest` — `findAll`(13→`isNotEmpty`)
  - `ThemeDaoTest` — `findAll`(4→`isNotEmpty`), `save`(5L→`findById` 존재 확인), `findAvailableTimeById`(12→`size-1`)
  - `ReservationTimeControllerTest` — `API_예약_시간_조회`, `API_예약_시간_삭제` (13 하드코딩 → `notNullValue`)
- [x] **6. 잘못된 테스트 수정**
  - `ThemeControllerTest.API_예약_시간_조회` — `GET /themes?condition=popular&size=10` → `GET /themes/popular?size=10`으로 수정
