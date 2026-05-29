# Cycle 1 - PR Review v1 - 수정 사항

---

## ✅ 리팩토링 할 것 목록

- [ ] **1. `ObjectMapper` Bean 전역 설정으로 `@JsonFormat(pattern = "HH:mm")` 중복 제거**
  - `JacksonConfig` 설정 클래스 생성 — `LocalTime` 기본 포맷을 `HH:mm`으로 전역 등록
  - 대상:
    - `AdminReservationResponse.java` — 생성자 파라미터 `LocalTime time`
    - `ReservationTimeResponse.java` — 필드 `LocalTime startAt`
    - `ReservationResponse.java` — 생성자 파라미터 `LocalTime time`
    - `AvailableReservationTimeResponse.java` — 필드 `LocalTime startAt`
    - `ReservationWaitingResponse.java` — 생성자 파라미터 `LocalTime time`

- [ ] **2. `ReservationDao` → `service.exception` 역방향 의존 제거**
  - `ReservationDao`의 `save`, `saveWaiting`에서 `DuplicateKeyException` catch 제거 — 예외 그대로 전파
  - `ReservationService`에서 `DuplicateKeyException`을 잡아 `ReservationConflictException`으로 변환
  - 비즈니스 예외 변환 책임을 service 레이어로 이동

- [ ] **3. Reservation / ReservationWaiting 도메인 완전 분리**
  - **Domain**: `ReservationWaiting`에 자체 `id` 필드 추가 (`reservation_waiting` PK)
  - **DAO**: `ReservationWaitingDao` 신규 생성 — waiting 관련 메서드 5개 이전
    - `saveWaiting` → 반환 타입 `ReservationWaiting`으로 변경
    - `findByWaitingId` → 반환 타입 `Optional<ReservationWaiting>`으로 변경
    - `deleteWaiting`, `findAllWaitingByName`, `existsByDateAndTimeIdAndThemeIdAndName` 이전
    - `ReservationDao`에서 `jdbcWaitingInsert`, `reservationWaitingRowMapper` 제거
  - **Service**: `ReservationWaitingService` 신규 생성 — waiting 메서드 3개 이전
    - 의존 방향: `ReservationWaitingService` → `ReservationService` (단방향)
    - `ReservationService`에서 `saveWaiting`, `deleteWaiting`, `findAllWaitingByName` 제거
  - **Controller**: `ReservationWaitingController` 신규 생성 — `/reservations/waiting` 엔드포인트 3개 이전
    - `ReservationController`에서 waiting 엔드포인트 제거

- [ ] **4. `ReservationService.saveWaiting` 검증 순서 재정렬**
  - 자원 존재 확인(`validateReservationTime`, `validateTheme`)을 비즈니스 검사보다 먼저 수행
  - 올바른 순서: 자원 검사 → 예약 존재 여부 → 사용자 예약 중복 → 대기 중복

- [x] **5. 테스트 메서드명과 실제 검증 상태코드 불일치 수정**
  - `ReservationControllerTest.java:67` — `존재하지_않는_시간으로_예약하면_400` → `404`로 수정
  - `ReservationControllerTest.java:85` — `존재하지_않는_테마로_예약하면_400` → `404`로 수정
  - `AdminThemeControllerTest.java:58` — `예약에_사용중인_테마_삭제하면_400` → `409`로 수정
  - `ReservationTimeControllerTest.java:75` — `이미_존재하는_시간이면_400` → `409`로 수정
  - `ReservationTimeControllerTest.java:90` — `예약에_사용중인_시간_삭제하면_400` → `409`로 수정

- [ ] **6. 대기(waiting) 관련 API 명세 문서 추가**
  - 대기 신청 / 대기 취소 / 대기 목록 조회 엔드포인트 명세 작성
  - 요청/응답 형식, 상태코드, 예외 케이스 포함

- [ ] **7. gitignore에 .DS_Store파일 추가하기.**
