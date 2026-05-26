# Cycle 2 - PR Review v2 - 수정 사항

---

## ✅ 리팩토링 할 것 목록

- [x] **1. EOF 확인하기**
- [x] **2. DTO 패키지 도메인별 분리**
  - `controller/dto/reservation/` — ReservationRequest, ReservationUpdateRequest, ReservationResponse, ReservationsResponse, AdminReservationResponse, AdminReservationsResponse
  - `controller/dto/time/` — ReservationTimeRequest, ReservationTimeResponse, ReservationTimesResponse, AvailableReservationTimeResponse, AvailableReservationTimesResponse
  - `controller/dto/theme/` — ThemeRequest, ThemeResponse, ThemesResponse
  - `ErrorResponse`는 루트(`controller/dto/`)에 유지
- [x] **3. GlobalExceptionHandler 테스트 작성**
  - `@WebMvcTest` + 인라인 더미 컨트롤러 방식으로 각 예외별 HTTP status / 응답 body 검증
  - 대상: `IllegalArgumentException` → 400, `ReservationConflictException` → 409, `PastReservationException` → 422, `MethodArgumentNotValidException` → 400, `HttpMessageNotReadableException` → 400, `NoResourceFoundException` → 404, `Exception` → 500
- [x] **4. IllegalArgumentException 처리 개선**
  - 도메인 전용 커스텀 예외(`InvalidInputException` 등) 생성
  - 비즈니스 로직에서 `IllegalArgumentException` 대신 커스텀 예외 사용
  - `IllegalArgumentException` 핸들러는 예상치 못한 경우로 분리 — `e.getMessage()` 노출 제거, 경고 로그 + 제네릭 메시지 반환
- [x] **5. 페이징 적용**
  - `GET /admin/reservations` — 전체 예약 조회, 운영 기간이 길어질수록 무한 증가 (최우선)
- [x] **6. Reservation 생성자 통합 — restore() 제거**
  - 과거 날짜 체크를 `create()`에서 private 생성자의 `validate()`로 이동
  - `createdAt`이 파라미터로 들어오므로 신규/복원 모두 동일 조건식으로 처리 가능
  - `restore()` 팩토리 메서드 제거, 단일 생성 경로로 통일
- [x] **7. 예외 클래스 발생 위치로 이동 — exception 패키지 제거**
  - `PastReservationException` → `domain` 패키지 (핵심 규칙이 `Reservation`에 있음)
  - `ReservationConflictException` → `service` 패키지 (`ReservationService`에서만 발생)
- [x] **8. 로컬 변수 final 일관성 정리**
  - 현재 `update()`에서 `reservation`만 `final` 누락 — 혼용 상태
  - 전체 로컬 변수에서 `final` 제거로 통일 (필드 `final`은 유지)
- [x] **9. 과거 날짜 검증을 도메인으로 이동**
  - `save()`는 `Reservation.create()`가 이미 검증 — 도메인 ✅
  - `update()` (서비스 48-49번 줄), `delete()` (서비스 61-62번 줄)는 서비스가 직접 체크 — 도메인으로 이동 필요
  - `Reservation.withUpdated(date, newTime, now)` 추가 — 과거 날짜 체크 후 새 Reservation 반환
  - `Reservation.validateCancellable(now)` 추가 — 취소 가능 여부 도메인이 판단
  - 서비스는 충돌 체크(DB 조회 필요)만 유지, 나머지는 오케스트레이션만 담당
- [x] **10. delete() 멱등성 처리 — 존재하지 않는 예약 삭제 시 성공 응답**
  - 존재하지 않으면 조용히 반환 (`return`) — 목적(예약 없는 상태) 달성으로 간주
  - `isCanceled` 소프트 딜리트는 이력 보존 요구사항이 생길 때 도입 — 지금은 오버엔지니어링
- [x] **11. 커스텀 예외 사용 기준 정립 및 적용**
  - 현재: HTTP 상태 코드가 400이 아닐 때만 커스텀 예외 사용 — 명확한 기준 없음
  - 기준: 도메인 규칙 위반은 모두 커스텀 예외, `IllegalArgumentException`은 예상치 못한 프로그래밍 오류에만
  - `ReservationNotFoundException` 등 도메인 의미를 담은 예외로 교체 (4번 항목과 연계)
- [x] **12. `LocalDateTime.now()` → `Clock` 주입으로 교체**
  - `Clock` 빈 등록 (`Clock.systemDefaultZone()`)
  - `ReservationService`에 `Clock` 주입, `LocalDateTime.now(clock)`으로 교체
  - 테스트에서 `Clock.fixed()`로 고정 시간 사용 → 과거/미래 분기 테스트 가능
- [ ] ~~**13. schema.sql 유니크 제약 수정**~~ ❌ 요구사항 오류 — 방 여러 개이므로 같은 시간에 다른 테마 예약 가능해야 함. 현재 `UNIQUE (date, time_id, theme_id)` 유지가 올바름.
