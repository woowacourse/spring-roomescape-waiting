# 결제 연동 6단계 — PaymentService 구현

## 구현 목표

결제 승인의 핵심 비즈니스 흐름을 담당하는 `PaymentService`를 구현한다.  
Toss를 모르고 포트(`PaymentGateway`)만 의존한다.

---

## 추가 파일

### `roomescape/service/PaymentService.java`

`confirmPayment(PaymentConfirmation)` 흐름:

```
1. orderId로 예약 조회 → 없으면 RESERVATION_NOT_FOUND
2. 금액 검증: confirmation.amount ≠ reservation.amount → PAYMENT_AMOUNT_MISMATCH (gateway 호출 전 차단)
3. paymentGateway.confirm(confirmation) → PaymentResult
4. Payment 저장 (paymentKey, orderId, amount)
5. reservation.confirm() → CONFIRMED 상태
6. reservationRepository.updateStatus(reservation)
7. ThemeSlot.isReserved = true 업데이트
```

의존: `PaymentGateway`, `ReservationRepository`, `PaymentRepository`, `ThemeSlotRepository`

### `roomescape/infra/JdbcPaymentRepository.java`

`PaymentRepository` 포트의 JDBC 구현체.

```sql
-- save
INSERT INTO payment (reservation_id, payment_key, order_id, amount) VALUES (?, ?, ?, ?)

-- findByOrderId
SELECT id, reservation_id, payment_key, order_id, amount FROM payment WHERE order_id = ?
```

---

## 추가 테스트 파일

### `FakePaymentRepository.java`

`ConcurrentHashMap` + `AtomicLong` 기반 인메모리 구현. 기존 Fake 레포지터리 패턴과 동일.

### `PaymentServiceTest.java`

| 테스트 | 검증 내용 |
|---|---|
| `confirmPayment_success_...` | 예약 CONFIRMED, 슬롯 isReserved=true, Payment 저장 |
| `confirmPayment_amountMismatch_throwsException` | PAYMENT_AMOUNT_MISMATCH 예외 발생 |
| `confirmPayment_amountMismatch_gatewayNotCalled` | 금액 불일치 시 gateway.confirm() 미호출 확인 |
| `confirmPayment_orderIdNotFound_throwsException` | RESERVATION_NOT_FOUND 예외 발생 |

`PaymentGateway`는 Mockito 없이 람다 스텁으로 처리 — 기존 테스트 패턴과 일관성 유지.

---

## 설계 결정 메모

| 결정 | 이유 |
|---|---|
| 금액 검증을 gateway 호출 전에 수행 | 금액 불일치는 서버 측 데이터 문제. Toss API를 불필요하게 호출하지 않고 차단. |
| ThemeSlot 업데이트에 ThemeSlotRepository.update() 재사용 | 별도 `updateIsReserved()` 메서드 추가 없이, `update()`가 theme+date+time 기준으로 매칭하므로 id 없이 업데이트 가능. |
| ThemeSlotRepository를 PaymentService에 추가 | isReserved=true 세팅은 결제 완료 시점의 책임. 계획서의 의존 목록에 누락됐지만 구현상 필요. |
| PaymentGateway 람다 스텁 사용 | 기존 Fake 패턴과 일관성. 외부 의존 없이 단위 테스트 가능. |
