# Cycle 1 - PR Review 전 개인 수정 사항

---

## ✅ 리팩토링 할 것 목록

### 도메인 방어 (Domain Validation)

- [x] **1. `Theme` 생성자에 검증 로직 추가해야 함.**
  - `name`, `description`, `thumbnailUrl` 전부 null/blank 허용 상태
  - `Reservation`, `ReservationTime`처럼 생성자에서 검증 추가 필요

- [x] **2. `Reservation.validate()` - `theme` null 검증 추가해야 함.**
  - `name`, `date`, `time`은 검증하지만 `theme`은 검증 없음
  - `theme == null` 케이스 추가 필요

- [x] **3. `Reservation` - 과거 날짜 예약 검증 추가해야 함.**
  - 지금은 `date`가 오늘 이전이어도 예약 생성 가능
  - `date.isBefore(LocalDate.now())` 검증 추가 필요

- [x] **4. `Reservation.validate()` 가 `static` 메서드임**
  - 생성자 내부에서 호출되지만 외부에서도 직접 호출 가능한 구조
  - `static` 제거하고 생성자 내부 전용 `private` 메서드로 변경해야 함.

---

### 코드 일관성 (Code Consistency)

- [x] **5. `ThemeRequest` 필드명이 snake_case임. camel로 통일해야 함.**
  - `thumbnail_url` (snake_case) vs 나머지 DTO 필드들은 camelCase 사용

- [x] **6. `AvailableReservationTimeResponse` - `long` vs `Long` 혼용중임 -> long으로 통일**
  - `long id` (primitive) 사용 → 다른 DTO들은 모두 `Long id` (wrapper) 사용

- [x] **7. `date` 필드 타입 불일치 (Response DTO 간) -> String으로 통일**
  - `ReservationResponse`: `String date`
  - `AdminReservationResponse`: `LocalDate date`

- [x] **8. `ReservationController` - 응답 방식 혼용**
  - POST: `@ResponseStatus(HttpStatus.CREATED)` + return POJO
  - DELETE: `ResponseEntity<Void>` 반환
  - 이전 리뷰어 분이 때에 따라 따져 써야 하지만, 하나로 통일하는게 좋다 하셨으니 `ResponseEntity`를 사용하는 방식으로 통일

- [x] **9. `ReservationTimeController` - `@RequestMapping` 써줄 것.**
  - 다른 컨트롤러들은 클래스 레벨에 `@RequestMapping`을 사용하지만
  - `ReservationTimeController`는 메서드마다 `/times` 경로를 반복 선언

---

### Repository 코드 품질

- [x] **10. `ThemeDao` - 불필요한 try-catch 3곳 지우기**
  - `findAll()`, `findPopularThemes()`, `findAvailableTimeById()` 모두 `EmptyResultDataAccessException`을 catch
  - `JdbcTemplate.query()`는 결과가 없으면 빈 리스트를 반환하므로 해당 예외 발생 안 함
  - try-catch 블록 전부 제거 가능함.
  - 이거 누가 짰냐

- [x] **11. `ReservationTimeDao.existsByStartAt()` - 불필요한 null 체크**
  - `count != null && count > 0` 에서 `count != null` 부분 불필요
  - `queryForObject`는 null을 반환하지 않음

- [x] **12. `ReservationDao.existsByTimeId()` - 동일한 불필요한 null 체크**
  - 동일하게 `count != null &&` 제거 가능

---

### 서비스 로직 문제

- [x] **13. `ReservationService` - 중복 예약 검증 추가할 것.**
  - 같은 날짜 + 시간 + 테마 조합으로 중복 예약이 가능
  - `reservationDao`에 `existsByDateAndTimeIdAndThemeId()` 추가 후 서비스에서 검증 필요

- [x] **14. `ThemeService.getPopularThemes()` - 날짜 범위 계산 오류**
  - `startDate = today.minusDays(8)`, `endDate = today.minusDays(1)` → 몰랐는데, 찾아보니 마지막 날짜 포함해서 이건 8일치 범위임.

- [x] **15. `AdminThemeService.delete()` - 예약에 사용 중인 테마 삭제 가능**
  - `ReservationTimeService.delete()`에는 사용 중 여부 체크가 있는데 테마에는 없음

---

### DB 스키마 문제

- [x] **16. `reservation.date` 컬럼 타입이 `VARCHAR(255)`**
  - `DATE` 타입으로 변경

- [x] **17. `reservation_time.start_at` 컬럼 타입이 `VARCHAR(255)`**
  - `TIME` 타입으로 변경

- [x] **18. 중복 예약 방지 DB 제약 없음**
  - `(date, time_id, theme_id)` 조합에 UNIQUE 제약 조건 없음
  - 애플리케이션 레벨 검증과 함께 DB 레벨 제약도 추가해주면 2중으로 막혀서 좋을듯.

- [x] **19. `thumbnail_url`이 `VARCHAR(255)`로 사이즈가 작아보임**
  - 2048이나 TEXT로 바꾸기

---

### 컨트롤러 문제

- [ ] **20. `ThemeController` - `"popular"` 문자열 하드코딩**
  - `if ("popular".equals(condition))` 형태
  - 두 가지 방법 중에 고민이 됨:

  1. Enum으로 condition 파싱
  ```java
  public enum ThemeCondition { POPULAR }
  return switch (condition) {
      case POPULAR -> ResponseEntity.ok(themeService.getPopularThemes(size));
  };
  ```
  오버 엔지니어링인것 같긴 한데, 프론트 변경 안해도 되서 좋은 것 같기도 함.

  2. 별도 엔드포인트로 분리
  ```java
  @GetMapping("/popular")
  public ResponseEntity<List<ThemeResponse>> getPopularThemes(
          @RequestParam(defaultValue = "10") int size) {
      return ResponseEntity.ok(themeService.getPopularThemes(size));
  }
  ```
  이게 더 restful 한건가..? 근데 프론트는 여기저기 고치긴 해야함.
