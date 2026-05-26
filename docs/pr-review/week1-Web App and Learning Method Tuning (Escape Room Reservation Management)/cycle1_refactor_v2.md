# Cycle 1 - PR Review v2 - 수정 사항

---

## ✅ 리팩토링 할 것 목록

- [x] **1. Lombok 지우기**
- [x] **2. 도메인 검증을 DB 저장 이전에 실행되도록 수정**
  - `Reservation`에 `static validate(name, date, time)` 메서드 추가
  - `ReservationService.save()`에서 `reservationDao.save()` 호출 전에 `Reservation.validate()` 먼저 호출
- [x] **3. 사용 중인 예약 시간 삭제 방지**
  - `ReservationDao`에 `existsByTimeId(Long timeId)` 메서드 추가
  - `ReservationTimeService`에 `ReservationDao` 주입
  - `ReservationTimeService.delete()`에서 삭제 전 `existsByTimeId()` 확인 후 예외 처리
- [x] **4. `ReservationTime` 생성자에 도메인 검증 추가**
  - `startAt`이 null이면 `IllegalArgumentException` 발생
- [x] **5. 패키지명 소문자로 수정**
  - `roomescape.reservationTime` → `roomescape.reservationtime`
  - `roomescape.reservationTime.dto` → `roomescape.reservationtime.dto`
