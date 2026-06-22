# PR Description — 타임아웃 / 멱등성 / 재시도 / 결제 내역

## 구현 내용

외부 결제 API(Toss Payments) 연동에서 발생할 수 있는 네트워크 불확실성을 안전하게 처리합니다.
타임아웃 설정, 예외 분류, 멱등키, UNCERTAIN 상태, 재시도 정책, 결제 내역 조회를 구현했습니다.

---

## 1. 타임아웃 설정

`jdk` 기반 HTTP 팩토리는 응답 바디 읽기 지연을 감지하지 못합니다.
`SimpleClientHttpRequestFactory`를 명시적으로 사용해 connect/read 타임아웃을 모두 제어합니다.

```properties
toss.connect-timeout=3000   # 연결 타임아웃 (ms)
toss.read-timeout=10000     # 읽기 타임아웃 (ms)
```

---

## 2. 타임아웃 예외 분류

`ResourceAccessException`의 원인 예외를 분석해 두 케이스를 다르게 처리합니다.

| 예외 | 의미 | 처리 |
|------|------|------|
| `SocketTimeoutException("Read…")` | 토스가 처리했을 수도 있음 (불확실) | UNCERTAIN Payment 저장, 예약 PENDING 유지 |
| `ConnectException` | 연결 자체가 안 됨 (미처리 확정) | 503 에러 반환 |

---

## 3. 멱등키 (Idempotency-Key)

재시도 시 이중 승인을 방지하기 위해 토스 요청에 `Idempotency-Key: {orderId}` 헤더를 추가합니다.
`orderId`는 예약 생성 시 발급된 UUID로 고정되므로, 재시도 횟수와 관계없이 토스는 첫 처리 결과를 동일하게 반환합니다.

---

## 4. PaymentStatus — UNCERTAIN 상태

read timeout 발생 시 결제가 됐는지 모르는 상태이므로, 섣불리 취소하지 않습니다.

```java
public enum PaymentStatus {
    CONFIRMED,   // 정상 승인
    UNCERTAIN    // read timeout — 결과 불명확
}
```

- 예약은 PENDING 유지 (CANCELLED로 바꾸면 결제된 사용자 피해)
- Payment 레코드는 `status = UNCERTAIN`, `payment_key = NULL`로 저장
- 사용자에게 202 응답과 함께 "결제 내역 확인" 안내

---

## 5. 슬롯 이중 confirm 방지

자동 confirm 제거 이후, 동일 슬롯에 여러 PENDING 예약이 존재할 수 있습니다.
두 사용자가 순차적으로 결제하면 슬롯에 CONFIRMED가 두 개 생기는 버그를 방지합니다.

`confirmPayment()` 내 토스 API 호출 전에 슬롯 선점 여부를 검증합니다.

```
슬롯 isReserved == true → PAYMENT_SLOT_ALREADY_CONFIRMED (409)
→ PaymentGateway.confirm() 미호출 (실제 결제 발생 X)
```

> 동시에 두 요청이 진입하는 레이스 컨디션은 DB 수준 락(`SELECT FOR UPDATE`)이 필요하며, 동시성 제어 단계에서 다룰 예정입니다.

---

## 6. 결제 내역 조회 API

```
GET /payment/history?name={사용자이름}
```

예약 목록과 각 예약에 연결된 결제 상태를 함께 반환합니다.
결제 기록이 없는 예약(결제 전 상태)도 포함되며, `paymentStatus / paymentKey`는 null로 반환됩니다.

---

## 7. Spring Retry — 자동 재시도

일시적인 네트워크 실패 시 자동으로 재시도합니다.

### 예외 타입 분리

`@Retryable`은 예외 타입으로 재시도 여부를 구분하므로 타입 자체를 나눕니다.

```
RetryablePaymentException (재시도 O)  ← read timeout, connect timeout
CustomException           (재시도 X)  ← 카드 거절, 인증 오류 등 비즈니스 거절
```

### 재시도 정책

- 최대 3회, 지수 백오프: 1초 → 2초 → 4초 + random jitter
- 3회 소진 시 `@Recover`에서 `CustomException`으로 변환 → `PaymentService`가 에러 코드 타입에 따라 UNCERTAIN 저장 또는 에러 전파

```
confirm()  ← @Retryable
    ├─ 성공 ────────────────────────────────► PaymentResult 반환
    │
    ├─ RetryablePaymentException (1회) ↓ 1초
    ├─ RetryablePaymentException (2회) ↓ 2초
    ├─ RetryablePaymentException (3회) → @Recover
    │       read timeout  → CustomException(PAYMENT_READ_TIMEOUT)        → UNCERTAIN 저장
    │       connect 실패  → CustomException(PAYMENT_CONNECTION_TIMEOUT)  → 503 에러
    │
    └─ CustomException (비즈니스 거절) → noRetryFor → 즉시 실패
```

---

## 테스트

### PaymentService 단위 테스트

- read timeout → UNCERTAIN Payment 저장, 예약 PENDING 유지
- connection timeout → 예외 전파
- 슬롯 이미 선점 → `PAYMENT_SLOT_ALREADY_CONFIRMED`, 게이트웨이 미호출 확인
- `getPaymentHistory` → CONFIRMED / UNCERTAIN / 결제없음 혼합 목록 반환

### TossPaymentGateway 슬라이스 테스트

- read timeout → `RetryablePaymentException(PAYMENT_READ_TIMEOUT)`
- connect 실패 → `RetryablePaymentException(PAYMENT_CONNECTION_TIMEOUT)`

### PaymentController 입력 검증 테스트

- `name` 빈 문자열 → 400
- `name` 파라미터 누락 → 400 (`MissingServletRequestParameterException` 핸들러 추가)

---

## 고민한 점

**read timeout을 즉시 실패가 아닌 UNCERTAIN으로 처리하는 이유**

토스 서버가 처리를 완료한 뒤 응답을 보내는 도중 타임아웃이 날 수 있습니다.
이 상태에서 예약을 취소하면 "결제는 됐지만 예약이 없는" 상황이 발생합니다.
UNCERTAIN으로 남겨 사용자가 결제 내역 페이지에서 직접 확인할 수 있도록 했습니다.

**`@Retryable`에서 예외 타입을 나눈 이유**

Spring Retry의 `retryFor`와 `noRetryFor`에 같은 타입을 동시에 지정할 수 없습니다.
비즈니스 거절은 재시도해도 결과가 같으므로 즉시 실패해야 하고,
네트워크 실패는 재시도할 가치가 있습니다.
두 경우를 타입 자체로 구분(`RetryablePaymentException` vs `CustomException`)해 정책을 선언적으로 관리합니다.
