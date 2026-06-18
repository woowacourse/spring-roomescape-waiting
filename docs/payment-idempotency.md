# 결제 승인 멱등성 처리

## 목적

Toss 결제 승인 API는 `POST /v1/payments/confirm`이다. POST 요청은 기본적으로 멱등성이 보장되지 않으므로, 같은 승인 요청이 두 번 호출되면 중복 처리 위험이 생긴다.

이를 막기 위해 Toss 승인 요청에 `Idempotency-Key` 헤더를 보낸다. 같은 `Idempotency-Key`로 같은 요청을 다시 보내면 Toss는 첫 요청의 응답을 재사용하므로, 타임아웃 후 재시도나 successUrl 새로고침으로 승인 API가 다시 호출돼도 이중 승인되지 않는다.

## 저장 방식

멱등키는 주문 생성 시점에 만든다.

```text
예약 생성 요청
-> Reservation(PAYMENT_PENDING) 저장
-> orderId 생성
-> idempotencyKey UUID 생성
-> PaymentOrder 저장
```

저장 위치는 `payment_order.idempotency_key`다.

```sql
idempotency_key VARCHAR(300) NOT NULL,
UNIQUE (idempotency_key)
```

Toss 기준으로 멱등키는 300자 이하이고 15일 동안 유효하다. 현재 구현은 UUID 문자열을 사용하므로 길이 조건을 만족한다.

## 구현 위치

멱등키 생성 포트는 다음 인터페이스다.

```java
public interface IdempotencyKeyGenerator {

    String generate();
}
```

운영 구현체는 UUID를 생성한다.

```java
@Component
public class UuidIdempotencyKeyGenerator implements IdempotencyKeyGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
```

`ReservationService.createReservation(...)`에서 `orderId`와 함께 `idempotencyKey`를 생성하고 `PaymentOrder`에 저장한다.

## 승인 요청 흐름

결제 성공 콜백이 들어오면 `PaymentService`가 `orderId`로 저장된 주문을 조회한다.

```text
successUrl 콜백
-> orderId로 PaymentOrder 조회
-> amount 검증
-> 저장된 idempotencyKey를 PaymentConfirmation에 포함
-> PaymentGateway.confirm(...)
```

`TossPaymentGateway`는 `PaymentConfirmation.idempotencyKey()` 값을 `Idempotency-Key` 헤더로 보낸다.

```java
.header("Idempotency-Key", confirmation.idempotencyKey())
```

중요한 점은 매 호출마다 새 UUID를 만들지 않는다는 것이다. 멱등키는 주문에 저장된 값을 계속 재사용한다.

## 재시도 상황

예를 들어 승인 요청 후 read timeout이 발생할 수 있다.

```text
우리 서버 -> Toss: 승인 요청 전송
Toss: 승인 처리 완료 가능성 있음
우리 서버: 응답을 받지 못함
```

이때 같은 주문을 다시 승인해야 한다면 저장된 같은 `idempotencyKey`로 재시도한다. Toss는 같은 키로 들어온 요청을 첫 요청과 같은 요청으로 판단하므로 중복 승인을 막을 수 있다.

같은 주문을 다시 확인하거나 재시도할 때도 DB에 저장된 같은 `idempotencyKey`를 사용한다.

## ALREADY_PROCESSED_PAYMENT와의 관계

`Idempotency-Key`는 중복 승인 방지를 위한 1차 방어다.

`ALREADY_PROCESSED_PAYMENT` 처리는 이미 처리된 결제에 대한 2차 방어다.

둘의 역할은 다르다.

| 방어 | 목적 |
| --- | --- |
| `Idempotency-Key` | 같은 승인 요청의 중복 처리를 Toss 쪽에서 막음 |
| `ALREADY_PROCESSED_PAYMENT` 처리 | 이미 처리된 결제가 다시 승인 요청됐을 때 우리 서비스가 별도 예외로 구분함 |

따라서 둘을 같이 두는 것이 안전하다.

현재 코드는 `ALREADY_PROCESSED_PAYMENT`를 `TossPaymentException.AlreadyProcessed`로 매핑한다. 다만 이를 Toss 결제 조회로 검증한 뒤 성공으로 흡수하는 흐름은 아직 구현되어 있지 않다.

## 테스트

관련 테스트는 다음을 확인한다.

- 예약 생성 시 `PaymentOrder`에 `idempotencyKey`가 저장된다.
- 결제 승인 시 저장된 `idempotencyKey`가 `PaymentConfirmation`에 전달된다.
- Toss 승인 요청의 `Idempotency-Key` 헤더가 `orderId`가 아니라 저장된 `idempotencyKey`를 사용한다.
- JDBC 저장소가 `idempotency_key` 컬럼을 저장하고 조회한다.
