# 주문/결제 내역 표시

## 목적

사용자가 내 예약 화면에서 예약 정보와 결제 정보를 함께 확인할 수 있게 한다.

내 예약 목록에는 날짜, 시간, 테마 같은 예약 정보뿐 아니라 결제 상태, `orderId`, `paymentKey`, 결제 금액도 표시한다.

## 표시 대상

`GET /reservations/mine` 응답의 `reservations` 목록에는 대기 예약을 제외한 사용자의 예약이 포함된다.

포함되는 예약 상태는 다음과 같다.

| 예약 상태 | 의미 |
| --- | --- |
| `PAYMENT_PENDING` | 결제 대기 또는 결제 결과 확인 필요 |
| `RESERVED` | 결제 승인 완료 및 예약 확정 |
| `PAYMENT_FAILED` | 결제 실패 또는 취소 |

대기 예약(`WAITING`)은 기존처럼 `waitingReservations`에 따로 내려간다.

## 결제 상태 계산

결제 상태는 예약 상태와 결제 주문 정보를 조합해서 계산한다.

| 조건 | 결제 상태 | 화면 표시 |
| --- | --- | --- |
| 예약 상태가 `RESERVED` | `CONFIRMED` | 확정(CONFIRMED) |
| 예약 상태가 `PAYMENT_FAILED` | `FAILED` | 실패 |
| 예약 상태가 `PAYMENT_PENDING`이고 `paymentKey`가 없음 | `PAYMENT_PENDING` | 결제 대기 |
| 예약 상태가 `PAYMENT_PENDING`이고 `paymentKey`가 있음 | `CHECK_REQUIRED` | 확인 필요 |
| 결제 주문이 없음 | `NONE` | 결제 정보 없음 |

`CHECK_REQUIRED`는 read timeout처럼 결제 승인 결과가 불명확한 경우를 표현한다. 이 상태는 결제 실패가 아니다. 사용자가 결제를 완료했을 가능성이 있으므로 실패로 단정하지 않고 확인 필요로 보여준다.

## 응답 필드

`reservations`의 각 항목은 다음 결제 필드를 포함한다.

```json
{
  "id": 1,
  "name": "브라운",
  "themeName": "공포",
  "date": "2026-05-08",
  "time": "10:00:00",
  "reservationStatus": "PAYMENT_PENDING",
  "paymentStatus": "PAYMENT_PENDING",
  "orderId": "order_123456",
  "paymentKey": null,
  "amount": 37000
}
```

승인 완료 상태라면 `paymentKey`가 포함되고 `paymentStatus`는 `CONFIRMED`가 된다.

## 실패 처리

`failUrl`로 `orderId`가 돌아온 경우 결제 대기 예약을 삭제하지 않고 `PAYMENT_FAILED`로 변경한다. 이렇게 해야 사용자가 내 예약 화면에서 실패한 주문 내역을 확인할 수 있다.

단, 사용자가 결제창을 바로 닫아 `orderId`가 없는 경우에는 어떤 주문인지 식별할 수 없으므로 아무 것도 변경하지 않는다.

## read timeout 처리

승인 API 호출 중 read timeout이 발생하면 `TOSS_CONFIRMATION_UNKNOWN`으로 구분한다.

이때 `paymentKey`를 주문에 저장하고 예외를 유지한다. 예약은 아직 `PAYMENT_PENDING` 상태로 남는다.

내 예약 화면에서는 다음 조건으로 `CHECK_REQUIRED`를 표시한다.

```text
reservationStatus == PAYMENT_PENDING
paymentKey != null
```

이 방식은 결제 실패로 단정하지 않으면서, 사용자가 확인이 필요한 주문을 구분할 수 있게 한다.

## 프론트 표시

내 예약 테이블에는 다음 컬럼이 표시된다.

- ID
- 테마
- 날짜
- 시간
- 결제 상태
- orderId
- paymentKey
- 금액
- 액션

예약 확정 상태(`RESERVED`)만 수정/취소 버튼을 보여준다. 결제 대기, 확인 필요, 실패 상태는 결제 내역 확인 용도로만 표시한다.
