# Cycle 2 - PR Review v3 - 수정 사항

---

## ✅ 리팩토링 할 것 목록

- [x] **1. DTO 패키지를 도메인별(reservation/time/theme)에서 request/response 기준으로 재편**

- [x] **2. `queryForObject` nullable 반환값 NPE 방어 처리 — `Objects.requireNonNullElse(count, 0)` 적용**
  - `ReservationDao.java:52` — `count()` 반환 시 auto-unboxing
  - `ReservationDao.java:82` — `existsByTimeId`
  - `ReservationDao.java:88` — `existsByThemeId`
  - `ReservationDao.java:94` — `existsByDateAndTimeIdAndThemeId`
  - `ReservationTimeDao.java:41` — `existsByStartAt`

- [x] **3. `Reservation.withUpdated` — `createdAt` 덮어쓰기 버그 수정**
  - 생성자에서 `validateNotPast` 제거
  - `create`에서 명시적으로 호출 (기존 동작 유지)
  - `withUpdated`는 `now`로 검증하고 `this.createdAt` 보존
  - `ReservationService.java:51` — `withUpdated` 호출부는 변경 없음 (`now` 파라미터 유지)

- [x] **4. 잘못된 예외 사용 수정**

  **404로 변경 (존재하지 않는 리소스)**
  - `ReservationService.java:35` — 존재하지 않는 예약 시간 (save)
  - `ReservationService.java:37` — 존재하지 않는 테마 (save)
  - `ReservationService.java:50` — 존재하지 않는 예약 시간 (update)

  **409로 변경 (충돌/비즈니스 규칙)**
  - `ReservationTimeService.java:30` — 이미 존재하는 예약 시간 (중복)
  - `ReservationTimeService.java:38` — 예약에 사용 중인 시간 삭제
  - `ThemeService.java:46` — 예약에 사용 중인 테마 삭제

- [x] **5. `Reservation.create` — id 없는 팩토리 메서드 추가**
  - `ReservationService.java:41` — `null`을 직접 전달하는 대신 id 파라미터 없는 `create` 오버로드 추가
  - 기존 `create(Long id, ...)` 는 DAO 복원용으로 유지

- [x] **6. 동작을 담은 변수명 개선**
  - `ReservationService.java:60` — `found` → `ifPresent`로 변수 제거
  - `ReservationService.java:51` — `updated` → 변수 제거, `reservation.getId()` 직접 사용
  - `ReservationTimeDao.java:35` — `results` → `times`

- [x] **7. `findAll`과 `countAll`을 단일 트랜잭션으로 묶기**
  - `AdminReservationController.java:26,30` — 두 호출이 별개 트랜잭션이라 그 사이 예약 추가/삭제 시 count와 data 불일치 발생
  - 서비스에서 `findAllWithCount(page, size)` 하나로 합쳐 `@Transactional(readOnly = true)` 안에서 처리

- [x] **8. `AppConfig` 개선**
  - 이름: `Clock` 빈 하나만 등록하므로 `AppConfig` → `ClockConfig` 또는 `TimeConfig`로 변경
  - 타임존: `Clock.systemDefaultZone()` → `Clock.system(ZoneId.of("Asia/Seoul"))` — 서버 환경에 따라 타임존이 달라지지 않도록 명시적으로 고정

- [x] **9. `ErrorResponse`에서 `code` 필드 제거**
  - 프론트가 `code`로 분기하는 대신 HTTP 상태 코드(200 여부)로 성공/실패 판단
  - 실패 시 서버의 `message`를 그대로 표시 → `code` 불필요
  - `ErrorResponse.java` — `code` 필드 제거
  - `user.html` — `code` 기반 분기 로직 → `message` 표시로 단순화
  - `GlobalExceptionHandler` — `ErrorResponse` 생성 시 `code` 인자 제거
