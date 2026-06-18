# Step3 PR Description — Toss Payments 결제 연동

## 구현 내용 요약

예약 시 결제 흐름을 Toss Payments API와 연동하여, 예약 생성 → 결제 승인 → 예약 확정의 라이프사이클을 완성했습니다.

---

## 구현 포인트

### 1. 예약 생성 시 orderId / amount 발급

예약을 저장할 때 UUID로 `orderId`를 생성하고, `ThemeSlot`에서 테마 가격(`Theme.price`)을 읽어 `amount`를 함께 저장합니다.  
클라이언트는 응답으로 받은 `orderId`와 `amount`를 그대로 Toss 위젯에 전달해 결제창을 열기 때문에, **서버가 금액의 출처**가 됩니다.

```
POST /reservations
→ Reservation(status=PENDING, orderId=UUID, amount=themePrice) 저장
→ ReservationResponse에 orderId, amount 포함하여 반환
```

### 2. 결제 확인 / 실패 콜백 엔드포인트

| 엔드포인트 | 역할 |
|---|---|
| `POST /payment/confirm?paymentKey=&orderId=&amount=` | Toss 승인 API 호출 후 예약 CONFIRMED |
| `GET /payment/fail?code=&message=&orderId=` | 결제 실패 시 예약 CANCELLED |

결제 승인 흐름:
1. `orderId`로 예약 조회 → 존재하지 않으면 `RESERVATION_NOT_FOUND`
2. 클라이언트가 전달한 `amount` ≠ 저장된 예약 금액 → `PAYMENT_AMOUNT_MISMATCH` (외부 API 호출 전 사전 검증)
3. Toss 승인 API 호출
4. `payment` 테이블에 결제 정보 저장
5. 예약 상태 → CONFIRMED, `theme_slot.is_reserved` → true 갱신

### 3. Port/Adapter 아키텍처로 외부 API 격리

도메인 계층에 `PaymentGateway` 인터페이스(포트)를 두고, 실제 Toss 통신은 `infra.toss.TossPaymentGateway`(어댑터)에서만 수행합니다.  
서비스 레이어는 어댑터 구현체를 전혀 알지 못하므로, 결제 PG를 교체하더라도 `PaymentService`는 변경하지 않아도 됩니다.

```
domain/payment/PaymentGateway       ← 포트 (인터페이스)
infra/toss/TossPaymentGateway       ← 어댑터 (Toss 전용 HTTP 구현)
service/PaymentService              ← 포트만 의존
```

### 4. Toss 에러 코드 → 내부 ErrorCode 매핑

Toss API는 4xx/5xx 오류 시 `{ "code": "...", "message": "..." }` 형태로 응답합니다.  
`TossPaymentGateway` 내에서 Toss 코드를 내부 `ErrorCode` enum으로 변환하여, 외부 API의 에러 체계가 서비스 레이어로 누출되지 않도록 했습니다.

```java
"ALREADY_PROCESSED_PAYMENT"           → PAYMENT_ALREADY_PROCESSED
"CARD_REJECTED" | "EXCEED_MAX_..."    → PAYMENT_CARD_REJECTED
"UNAUTHORIZED_KEY" | "INVALID_API_..."→ PAYMENT_UNAUTHORIZED_KEY
"NOT_FOUND_PAYMENT" | "..."           → PAYMENT_NOT_FOUND
"TOSS_PAYMENTS_ERROR"                 → PAYMENT_TOSS_INTERNAL_ERROR
그 외                                  → PAYMENT_UNKNOWN_ERROR
```

### 5. RestClient 설정 (TossPaymentConfig)

Spring 6.1의 `RestClient`를 빈으로 등록하고 Base URL과 Basic 인증 헤더를 한 곳에서 관리합니다.  
`secretKey + ":"` 를 Base64 인코딩한 값을 `Authorization: Basic {encoded}` 헤더로 설정합니다.

### 6. ThemeSlot 자동 confirm 제거

이전에는 예약 생성 즉시 `ThemeSlot.is_reserved = true`로 변경했지만,  
결제 전 상태(PENDING)에서는 슬롯을 점유하지 않도록 변경했습니다.  
슬롯은 **결제 승인 완료(`/payment/confirm`)** 시점에만 예약됨으로 표시됩니다.

---

## 외부 API 구현 방식

### HTTP 클라이언트: RestClient

- `RestClient`(Spring 6.1+)를 사용해 동기 HTTP 통신을 구현했습니다.
- `TossPaymentConfig`에서 baseUrl과 인증 헤더를 설정한 빈을 만들고, `TossPaymentGateway`에서 주입받아 사용합니다.

```java
RestClient.builder()
    .baseUrl("https://api.tosspayments.com")
    .defaultHeader("Authorization", "Basic " + encoded)
    .defaultHeader("Content-Type", "application/json")
    .build();
```

### 에러 핸들링 전략

`retrieve().onStatus(HttpStatusCode::isError, ...)` 콜백에서 응답 본문을 `TossErrorResponse`로 역직렬화한 뒤, `mapToErrorCode()`로 내부 예외로 변환합니다.  
Toss가 반환하는 에러 본문을 직접 읽기 위해 `ObjectMapper`를 수동으로 사용합니다(`clientResponse.getBody()`).

### DTO 분리

Toss API 전용 DTO(`TossPaymentRequest`, `TossPaymentResponse`, `TossErrorResponse`)는 `infra.toss.dto` 패키지에 격리되어 있으며, 도메인이나 서비스 레이어에서는 `PaymentConfirmation` / `PaymentResult` record만 사용합니다.

---

## 테스트 방식

### 1. TossPaymentGateway 슬라이스 테스트 — `MockRestServiceServer`

`@SpringBootTest` 없이 `RestClient.Builder`와 `MockRestServiceServer`만으로 Toss HTTP 통신을 모킹합니다.  
실제 네트워크 호출 없이 HTTP 요청/응답 시나리오를 검증하며, Toss 에러 코드별 `CustomException` 변환을 빠르게 테스트합니다.

```java
RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
mockServer = MockRestServiceServer.bindTo(builder).build();
gateway = new TossPaymentGateway(builder.build(), new ObjectMapper());

mockServer.expect(requestTo(CONFIRM_URL))
    .andRespond(withStatus(HttpStatus.BAD_REQUEST)
        .body("""{"code":"ALREADY_PROCESSED_PAYMENT","message":"..."}"""));

assertThatThrownBy(() -> gateway.confirm(...))
    .isInstanceOf(CustomException.class)
    .extracting(e -> ((CustomException) e).getErrorCode())
    .isEqualTo(ErrorCode.PAYMENT_ALREADY_PROCESSED);
```

검증 케이스:
- 정상 승인 → `PaymentResult` 반환
- `ALREADY_PROCESSED_PAYMENT` → `PAYMENT_ALREADY_PROCESSED`
- `CARD_REJECTED` → `PAYMENT_CARD_REJECTED`
- `UNAUTHORIZED_KEY` → `PAYMENT_UNAUTHORIZED_KEY`
- `TOSS_PAYMENTS_ERROR` (5xx) → `PAYMENT_TOSS_INTERNAL_ERROR`
- 미매핑 코드 → `PAYMENT_UNKNOWN_ERROR`

### 2. PaymentService 단위 테스트 — Fake 객체 + 람다 게이트웨이

`PaymentGateway`를 람다식으로 구현해 성공/실패 시나리오를 테스트합니다.  
Fake Repository로 DB 없이 서비스 비즈니스 로직만 검증합니다.

```java
// 성공 게이트웨이
PaymentGateway successGateway = confirmation ->
    new PaymentResult(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());

// 게이트웨이 호출 추적
List<PaymentConfirmation> called = new ArrayList<>();
PaymentGateway trackingGateway = confirmation -> {
    called.add(confirmation);
    ...
};
```

검증 케이스:
- 결제 승인 성공 → 예약 CONFIRMED, ThemeSlot 예약됨
- 금액 불일치 → `PAYMENT_AMOUNT_MISMATCH` 예외, **게이트웨이 미호출 확인**
- 없는 orderId → `RESERVATION_NOT_FOUND`
- 결제 실패(`handlePaymentFail`) → 예약 CANCELLED
- orderId `null` / 미존재 → 조용히 무시

### 3. 동시성 테스트 (H2) — `CountDownLatch` + `@RepeatedTest`

같은 슬롯에 5개 스레드가 동시에 예약을 시도할 때 CONFIRMED 상태가 정확히 하나임을 검증합니다.  
`CountDownLatch`로 스레드 출발을 동기화하고, `@RepeatedTest(10)`으로 10회 반복해 비결정적 타이밍에 의한 flaky를 최소화합니다.

```java
@RepeatedTest(10)
void 동시에_같은_슬롯에_예약하면_하나만_CONFIRMED가_된다() {
    CountDownLatch startLatch = new CountDownLatch(1);
    // 모든 스레드 준비 후 동시 출발
    startLatch.countDown();
    // CONFIRMED 수 == 1 검증
}
```

### 4. 동시성 테스트 (MySQL + Testcontainers) — `CyclicBarrier`

H2는 기본적으로 MVCC를 사용하지 않아 실제 운영 DB와 락 동작이 다를 수 있습니다.  
Testcontainers로 MySQL 8.0 컨테이너를 띄워 실제 운영 환경과 동일한 격리 수준에서 동시성을 검증합니다.

`CyclicBarrier`를 사용해 모든 스레드가 실제 HTTP 요청 직전에 정렬되도록 하여, CountDownLatch보다 더 정밀한 동시 출발을 보장합니다.

```java
CyclicBarrier barrier = new CyclicBarrier(threadCount);
// 각 스레드: barrier.await() 후 POST /reservations 호출
// → CONFIRMED == 1 검증
```

| 테스트 | DB | 동기화 | 반복 |
|---|---|---|---|
| `ConcurrencyTest` | H2 (in-memory) | CountDownLatch | @RepeatedTest(10) |
| `ConcurrencyWithMySqlTest` | MySQL 8.0 (Testcontainers) | CyclicBarrier | @RepeatedTest(5) |
