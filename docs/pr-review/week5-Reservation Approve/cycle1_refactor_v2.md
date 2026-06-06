# Cycle 1 - PR Review v2 - 수정 사항

---

## ✅ 리팩토링 할 것 목록

- [ ] **1. POST /reservations/waiting 응답 타입을 `ReservationResponse` → `ReservationWaitingResponse`로 변경 (waitingNumber 포함)**
- [ ] **2. `ReservationDao` 반복되는 SELECT + JOIN 절을 `BASE_QUERY` 상수로 추출**
- [ ] **3. `ReservationService.delete` 존재하지 않는 예약 시 `ifPresent` → `orElseThrow`로 변경 (update와 일관성 통일)**
- [ ] **4. `ReservationService.update`에 `validateCancellable` 추가 (과거 예약 수정 방지 + `approveFirstWaitingIfExists` 최적화 전제 보장)**
- [ ] **5. `approveFirstWaitingIfExists` while 루프 제거: 슬롯 시간이 과거면 즉시 return, 미래면 첫 대기 단 1회 조회 후 승인**
