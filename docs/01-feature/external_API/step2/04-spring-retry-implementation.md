# Spring Retry 구현 — TossPaymentGateway 재시도 전략

---

## 결론 먼저

> `@Retryable`은 AOP 프록시 기반이라 예외 타입으로 재시도 여부를 분류한다.
> 재시도 가능 / 불가 예외를 **타입으로 분리**하고,
> 멱등키는 호출 전에 고정해야 안전하게 재시도할 수 있다.

---

## 왜 직접 루프 대신 Spring Retry인가

직접 루프로 재시도를 구현하면 서비스 로직에 재시도 정책이 섞인다.

```java
// 직접 루프 — 재시도 정책이 비즈니스 로직에 섞임
for (int i = 0; i < 3; i++) {
    try {
        return paymentGateway.confirm(confirmation);
    } catch (ResourceAccessException e) {
        if (i == 2) throw e;
        Thread.sleep(1000 * (i + 1));
    }
}
```

`@Retryable`을 쓰면 재시도 정책을 어노테이션으로 선언하고, 실제 로직에는 단순한 메서드 호출만 남는다.

---

## 설정

### 의존성 (`build.gradle`)

```groovy
implementation 'org.springframework.retry:spring-retry'
implementation 'org.springframework:spring-aspects'  // AOP 프록시 필요
```

### 활성화

```java
@EnableRetry
@Configuration
public class TossPaymentConfig { ... }
```

---

## 예외 분리 전략

`@Retryable`은 **예외 타입**으로 재시도 여부를 구분한다.
`retryFor`와 `noRetryFor`에 같은 타입을 쓸 수 없으므로,
재시도 가능 여부에 따라 예외 타입 자체를 나눠야 한다.

### 재시도 가능 예외 (신규 정의)

```java
// gateway 레이어 전용 — 일시적 실패, 결과 불확실
public class RetryablePaymentException extends RuntimeException {
    public RetryablePaymentException(String message) {
        super(message);
    }
}
```

### 재시도 불가 예외 (기존 `CustomException` 그대로)

비즈니스 거절(카드 한도 초과, 인증 키 오류 등)은 재시도해도 결과가 같으므로 `CustomException`을 그대로 던져 즉시 실패시킨다.

---

## `TossPaymentGateway` 적용

### 예외 분류 지점 변경

```java
} catch (ResourceAccessException e) {
    Throwable cause = e.getCause();
    if (cause instanceof SocketTimeoutException && cause.getMessage().contains("Read")) {
        // read timeout: 서버가 처리했을 수도 있음 → 재시도 가능
        throw new RetryablePaymentException("read timeout");
    }
    // connect 실패: 연결조차 안 됨 → 재시도 가능 (서버 미처리 확정)
    throw new RetryablePaymentException("connection failed");
}
// Toss 비즈니스 거절: onStatus()에서 CustomException → 재시도 없이 즉시 실패
```

### `@Retryable` 선언

```java
@Retryable(
    retryFor  = { RetryablePaymentException.class },  // 재시도 O
    noRetryFor = { CustomException.class },           // 재시도 X
    maxAttempts = 3,
    backoff = @Backoff(
        delay      = 1000,   // 첫 재시도 대기: 1초
        multiplier = 2.0,    // 지수 백오프: 1s → 2s → 4s
        random     = true    // 지터: thundering herd 방지
    )
)
@Override
public PaymentResult confirm(PaymentConfirmation confirmation) { ... }
```

### `@Recover` — 3회 모두 실패 시

```java
@Recover
public PaymentResult recoverConfirm(RetryablePaymentException e, PaymentConfirmation confirmation) {
    // 재시도를 모두 소진했을 때 → PaymentService에서 UNCERTAIN으로 처리
    throw new CustomException(ErrorCode.PAYMENT_READ_TIMEOUT);
}
```

---

## 전체 호출 흐름

```
PaymentService.confirmPayment()
    └─ TossPaymentGateway.confirm()  ← @Retryable 적용

        ┌─ 성공 ────────────────────────────────────────► PaymentResult 반환
        │
        ├─ RetryablePaymentException (1회)
        │       ↓ 1초 + jitter 대기
        ├─ RetryablePaymentException (2회)
        │       ↓ 2초 + jitter 대기
        ├─ RetryablePaymentException (3회)
        │       ↓ @Recover 호출
        │   CustomException(PAYMENT_READ_TIMEOUT)
        │       ↓
        │   PaymentService → UNCERTAIN Payment 저장, 예약 PENDING 유지
        │
        └─ CustomException (비즈니스 거절)
                ↓ 즉시 실패 (noRetryFor)
            PaymentService → 예외 전파
```

---

## 멱등키와 재시도의 관계

`@Retryable`이 재시도할 때 **같은 `confirmation` 객체를 그대로 재사용**한다.
`confirmation.orderId()`가 이미 주문 생성 시 고정된 UUID이므로,
재시도 3회 모두 동일한 `Idempotency-Key`가 헤더에 붙는다.
토스 서버는 같은 키를 받으면 첫 응답을 그대로 돌려주므로 이중 승인이 발생하지 않는다.

```
재시도 1회: Idempotency-Key: order-uuid-abc → 토스 처리 중
재시도 2회: Idempotency-Key: order-uuid-abc → 첫 응답 캐시 반환 (멱등)
재시도 3회: Idempotency-Key: order-uuid-abc → 첫 응답 캐시 반환 (멱등)
```

---

## 주의사항

### AOP 프록시 한계

`@Retryable`은 스프링 빈을 통한 호출에만 동작한다.
같은 클래스 내에서 `this.confirm()`을 직접 호출하면 프록시를 우회하여 재시도가 동작하지 않는다.

```java
// 동작 안 함 — 프록시 우회
public void someMethod() {
    this.confirm(confirmation);  // @Retryable 무시
}

// 동작함 — 빈 주입을 통한 호출
public void someMethod(TossPaymentGateway gateway) {
    gateway.confirm(confirmation);  // @Retryable 적용
}
```

### 현재 코드에 바로 적용하기 전 고려할 것

현재 `PaymentService`는 `PAYMENT_READ_TIMEOUT`을 받으면 UNCERTAIN Payment를 저장한다.
여기에 Gateway 레벨 재시도까지 추가하면, **하나의 `confirmPayment()` 호출이 토스에 최대 3번 요청**을 보낼 수 있다.

| 상황 | 재시도 없음 | 재시도 있음 |
|------|------------|------------|
| 토스 일시 불안정 | 즉시 UNCERTAIN 저장 | 자동 복구 가능, 성공 확률↑ |
| 사용자 대기 시간 | 짧음 (타임아웃 1회) | 최대 1+2+4 = 7초 |
| 토스 요청 횟수 | 1회 | 최대 3회 |

결제처럼 민감한 작업은 재시도 횟수를 보수적으로(2~3회) 잡고,
전체 데드라인(예: 10초)을 넘지 않도록 backoff 값을 설계해야 한다.
