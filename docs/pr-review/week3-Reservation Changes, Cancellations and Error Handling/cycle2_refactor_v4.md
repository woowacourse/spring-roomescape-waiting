# Cycle 2 - PR Review v4 - 수정 사항

---

## ✅ 리팩토링 할 것 목록

- [x] **1. `ReservationPage` → `Page<T>` 제네릭으로 교체**
- [x] **2. `NotFoundException` 공통 부모 예외 도입 → `ReservationNotFoundException`, `ReservationTimeNotFoundException`, `ThemeNotFoundException` 상속하도록 변경, `GlobalExceptionHandler`에서 세 핸들러를 하나로 통합**
- [x] **3. `Reservation.create()` 팩토리 메서드 제거 → 생성자 public으로 열고 `new Reservation(...)` 직접 사용**
- [x] **4. `withUpdated()` 반환값 사용 → `Reservation updated = reservation.withUpdated(...)` 로 받고, DAO도 객체를 받도록 수정**
