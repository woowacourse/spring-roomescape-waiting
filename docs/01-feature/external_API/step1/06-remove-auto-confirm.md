# 결제 연동 3단계 — ThemeSlot 자동 confirm 제거

## 구현 목표

`ThemeSlot.addReservation()`이 결제 없이 CONFIRMED 상태를 만들지 않도록 변경한다.
이후 `PaymentService`(6단계)가 결제 완료 시점에 `reservation.confirm()`을 호출한다.

---

## 변경 파일

### `src/main/java/roomescape/domain/ThemeSlot.java`

**Before:**
```java
public Reservation addReservation(String name) {
    reservations.validateDuplicate(name);
    Reservation reservation = new Reservation(name, this.id, this.date, this.time, this.theme);
    if (reservations.hasNoActiveReservation()) {
        reservation.confirm();   // 자동 confirm
        this.isReserved = true;  // 슬롯 예약 상태 세팅
    }
    reservations.add(reservation);
    return reservation;
}
```

**After:**
```java
public Reservation addReservation(String name) {
    reservations.validateDuplicate(name);
    Reservation reservation = new Reservation(name, this.id, this.date, this.time, this.theme);
    reservations.add(reservation);
    return reservation;
}
```

- 첫 번째 예약도 항상 PENDING으로 생성.
- `isReserved = true` 세팅은 `PaymentService`가 결제 승인 후 처리(6단계).
- `cancelReservation`의 대기자 승격(`Reservation::confirm()`)은 그대로 유지. 대기자 승격은 재결제가 필요 없기 때문.

---

## 수정된 테스트

### `ThemeSlotTest.java`

| 변경 전 | 변경 후 |
|---|---|
| 첫 예약 → CONFIRMED, isReserved=true 기대 | 첫 예약 → PENDING, isReserved=false 기대 |
| 취소 후 재예약 → CONFIRMED 기대 | 취소 후 재예약 → PENDING 기대 |
| 두 번째 예약 → isReserved=true 기대 | 두 번째 예약 → isReserved=false 기대 |

### `ReservationServiceTest.java`

| 테스트 | 변경 내용 |
|---|---|
| `saveReservationByNotExistsThemeSlot` | 기대값 CONFIRMED → PENDING, isReserved true → false |
| `reservationStatusCancelWhenReservationIsPending` | isReserved 기대값 true → false |
| `reservationStatusCancelWhenReservationIsConfirmAndExistsPendingReservation` | `saveReservation`으로 Confirmed를 만들 수 없으므로 `fakeReservationRepository`에 직접 저장 |
| `returnInvalidCancelledCommandWhenCancelCompletedReservation` | PENDING → complete() 불가이므로 Confirmed를 직접 저장 후 complete 처리 |
| `giveOrderByApplicationOrder` | 첫 예약도 PENDING이므로 대기 순번 3개 → 4개 |
| `showWaitingOrderForPendingReservationsOnly` | 첫 예약이 PENDING에 포함되므로 waiting order 재계산 |

**`fakeReservationRepository` 필드 승격**: Confirmed/Completed 픽스처를 직접 저장해야 하는 테스트가 생겨 `setUp`의 지역 변수에서 인스턴스 필드로 변경.

---

## 설계 결정 메모

| 결정 | 이유 |
|---|---|
| `cancelReservation`의 대기자 confirm은 유지 | 대기자 승격은 기존 결제자가 취소한 빈 자리를 채우는 것. 대기자에게 재결제를 요구하지 않는 정책. |
| isReserved 세팅을 PaymentService로 이관 | isReserved=true는 "확정된 예약이 있다"는 의미. 결제 미완료 PENDING은 확정이 아니므로 결제 완료 시점에 세팅한다. |
