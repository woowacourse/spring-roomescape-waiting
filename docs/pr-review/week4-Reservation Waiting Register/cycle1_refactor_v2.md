# Cycle 1 - PR Review v2 - 수정 사항

---

## ✅ 리팩토링 할 것 목록

- [x] **1. `ReservationWaitingService`에서 `ReservationDao`, `ReservationTimeDao`, `ThemeDao` 직접 의존 제거**
  - `ReservationService`에 `existsByDateAndTimeIdAndThemeId`, `existsByDateAndTimeIdAndThemeIdAndName` 위임 메서드 추가
  - `ReservationWaitingService`의 `validateReservationTime`, `validateTheme` 제거 — `ReservationService`에 이미 존재하므로 중복
  - `ReservationWaitingService` 생성자에서 `ReservationDao`, `ReservationTimeDao`, `ThemeDao` 3개 제거
  - 최종 의존: `ReservationWaitingService` → `ReservationWaitingDao`, `ReservationService`, `Clock`

- [ ] **2. `reservation_waiting` 테이블 스키마 개선 — `reservation` FK 참조 구조로 전환**
  - 현재: `reservation_waiting`이 `reservation`을 참조하지 않고 (name, date, time_id, theme_id)를 독립 중복 저장 → `Reservation.id`에 waiting PK를 채워넣는 ID 모호성 발생
  - `reservation_waiting` 테이블에 `reservation_id BIGINT FK → reservation.id` 컬럼 추가, 중복 컬럼(name, date, time_id, theme_id) 제거
  - `reservation` 테이블의 `UNIQUE (date, time_id, theme_id)` 제약 제거 — 예약/대기 모두 수용하기 위해
  - `ReservationWaiting` 도메인 및 `ReservationWaitingDao` RowMapper 수정

- [x] **3. `.DS_Store` git 추적 해제**
  - `.gitignore`에 추가해도 이미 추적 중인 파일은 계속 추적됨
  - `git rm --cached .DS_Store` 실행 — 인덱스에서만 제거, 로컬 파일은 유지
