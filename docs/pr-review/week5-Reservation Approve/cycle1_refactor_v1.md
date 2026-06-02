# Cycle 1 - PR Review v1 - 수정 사항

---

## ✅ 리팩토링 할 것 목록

### Schema
- [x] `reservation_waiting` 테이블 제거
- [x] `reservation` 테이블에 `status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'` 추가

### Domain
- [x] `ReservationStatus` enum 신규 추가 (`CONFIRMED`, `WAITING`)
- [x] `Reservation`에 `status` 필드 추가
- [x] `ReservationWaiting` 도메인 클래스 삭제

### DAO
- [x] `ReservationWaitingDao` 전체 삭제
- [x] `ReservationDao` — `NOT IN (SELECT reservation_id FROM reservation_waiting)` → `status = 'CONFIRMED'` 으로 전부 교체
- [x] `ReservationDao` — `save()` INSERT에 `status` 컬럼 포함
- [x] `ReservationDao` — 대기 관련 메서드 흡수: `findWaitingById`, `findAllWaitingByName`, `existsWaitingBy...`

### Service
- [x] `ReservationService` — `saveEntry()` 제거
- [x] `ReservationWaitingService` — `saveWaiting()`: 단일 테이블 INSERT (status='WAITING')
- [x] `ReservationWaitingService` — `deleteWaiting()`: 단일 DELETE

### Controller / DTO
- [x] `ReservationWaitingResponse` — `from(ReservationWaiting)` → `from(Reservation, long waitingNumber)` 로 변경

### Test
- [x] `ReservationWaitingDaoTest` 삭제 (또는 `ReservationDao`로 이관)
- [x] `ReservationWaitingServiceTest` — 변경된 service 시그니처에 맞게 수정
- [ ] `ReservationDaoTest` — 추가된 메서드 테스트 추가
