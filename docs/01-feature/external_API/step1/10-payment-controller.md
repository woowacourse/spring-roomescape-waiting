# 결제 연동 7단계 — PaymentController 구현

## 구현 목표

Toss 결제 위젯의 successUrl / failUrl 콜백을 처리하는 컨트롤러를 추가한다.

---

## 추가 파일

### `roomescape/controller/PaymentController.java`

#### `POST /payment/confirm` — 결제 승인 콜백
```
Request Params: paymentKey, orderId, amount
→ PaymentConfirmation 생성 → PaymentService.confirmPayment()
→ 200 OK + PaymentConfirmResponse(reservationId, orderId, amount)
```

#### `GET /payment/fail` — 결제 실패 콜백
```
Request Params: code, message, orderId(nullable)
→ PaymentService.handlePaymentFail(orderId)
→ 200 OK + PaymentFailResponse(code, message)
```

### `roomescape/controller/dto/PaymentConfirmResponse.java`
```java
record PaymentConfirmResponse(Long reservationId, String orderId, Long amount)
```
`Payment.from()` 팩토리 메서드로 생성.

### `roomescape/controller/dto/PaymentFailResponse.java`
```java
record PaymentFailResponse(String code, String message)
```
Toss가 보내준 실패 코드와 메시지를 그대로 반환.

---

## 변경 파일

### `roomescape/service/PaymentService.java`

`handlePaymentFail(String orderId)` 추가:

```java
public void handlePaymentFail(String orderId) {
    if (orderId == null) {  // PAY_PROCESS_CANCELED 등 사용자 취소 시 orderId 없음
        return;
    }
    reservationRepository.findByOrderId(orderId).ifPresent(reservation -> {
        reservation.cancel();
        reservationRepository.updateStatus(reservation);
    });
}
```

- PENDING 예약만 존재하는 상태(결제 미완료)이므로 ThemeSlot 업데이트 불필요.
- 예약이 없으면 조용히 무시 (이미 취소됐거나 checkout 전 이탈).

---

## 설계 결정 메모

| 결정 | 이유 |
|---|---|
| `/payment/confirm`을 POST로 설계 | 결제 승인은 상태를 변경하는 write 연산. GET은 멱등성 의미를 갖지만 승인은 한 번만 처리돼야 함. |
| `/payment/fail`을 GET으로 설계 | Toss failUrl은 브라우저 리다이렉트이므로 GET. |
| orderId null 가드 | `PAY_PROCESS_CANCELED` (사용자 직접 취소) 시 Toss가 orderId를 보내지 않음. NPE 방지 필수. |
| 실패 시 예약 삭제 대신 CANCELLED 처리 | 결제 실패 이력을 보존하고 재시도 가능성 유지. delete는 추적 불가. |
| `handlePaymentFail`을 PaymentService에 배치 | 결제 흐름의 일부. ReservationService의 일반 취소(cancelReservation)와 목적이 다름 — ThemeSlot 승격 로직 불필요. |
