# 변경 명세 — spring-roomescape-waiting

## 개요

| 구분                | 변경 내용                                                                                                   |
|-------------------|---------------------------------------------------------------------------------------------------------|
| **결제 ACL**        | `PaymentGateway` 포트 + `TossPaymentGateway` 어댑터 신규 도입. 토스 HTTP 타입이 서비스/도메인으로 누출되지 않도록 격리                 |
| **결제 승인 흐름**      | `POST /reservations` 시 `paymentKey·orderId·amount` 수신 → Toss `/v1/payments/confirm` 호출 후 예약 저장          |
| **orderId 서버 발급** | 클라이언트 생성 UUID 대신 `POST /payments/prepare` 가 서버에서 UUID를 생성·`pending_payment` 에 저장 후 반환                   |
| **금액 위변조 방어**     | 결제 승인 전 `pending_payment.amount` ↔ 요청 `amount` 비교. 불일치 시 `PaymentAmountMismatchException` (400)         |
| **결제 취소 정리**      | `DELETE /payments/prepare/{orderId}` 로 결제 실패·취소 시 pending 레코드 삭제                                        |
| **예외 구조**         | `TossPaymentException` 8종 중첩 클래스 (AlreadyProcessed·CardRejected·Retryable 등), RFC 9457 ProblemDetail 응답 |
| **DB 스키마**        | `reservation` 에 `amount·payment_key` 추가. `pending_payment` 테이블 신규                                       |

---

## 1. Toss Payments 결제 연동

### 1-1. 도메인 객체

| 파일                                        | 설명                                                                    |
|-------------------------------------------|-----------------------------------------------------------------------|
| `payment/domain/PaymentConfirmation.java` | 결제 승인 요청값 record (`paymentKey`, `orderId`, `amount`)                  |
| `payment/domain/PaymentResult.java`       | 결제 승인 결과 record (`paymentKey`, `orderId`, `status`, `approvedAmount`) |
| `payment/domain/PaymentStatus.java`       | 결제 상태 enum (DONE / CANCELED 등 8종 + UNKNOWN fallback)                  |

### 1-2. 헥사고날 포트/어댑터

| 파일                                                  | 설명                                                                           |
|-----------------------------------------------------|------------------------------------------------------------------------------|
| `payment/gateway/PaymentGateway.java`               | 단일 메서드 인터페이스 `confirm(PaymentConfirmation)`                                  |
| `payment/gateway/toss/TossPaymentGateway.java`      | `RestClient`로 `POST /v1/payments/confirm` 호출, 오류 시 `TossPaymentException` 변환 |
| `payment/gateway/toss/TossPaymentException.java`    | 에러 코드별 8개 정적 중첩 클래스 (AlreadyProcessed / CardRejected 등)                      |
| `payment/gateway/toss/dto/TossPaymentResponse.java` | Toss 응답 record (`@JsonIgnoreProperties`)                                     |
| `payment/gateway/toss/dto/TossErrorResponse.java`   | Toss 에러 응답 record                                                            |

### 1-3. RestClient 설정

**파일**: `config/TossClientConfig.java`

- Basic Auth: `Base64(secretKey + ":")`
- `SimpleClientHttpRequestFactory` 사용 (JDK 기본은 응답 body 지연 감지 불가)
- 타임아웃: `connect 3s` / `read 30s` (application.properties 주입)

### 1-4. application.properties 추가 항목

```properties
toss.base-url=https://api.tosspayments.com
toss.secret-key=test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6
toss.connect-timeout-ms=3000
toss.read-timeout-ms=30000
```

> **주의**: `secret-key`는 서버 내부 전용. 외부 노출 금지.

---

## 2. Reservation 도메인 변경

**파일**: `domain/Reservation.java`

- `amount: Long` 필드 추가 (결제 금액)
- `paymentKey: String` 필드 추가 (nullable, Toss 발급 키)
- 생성자 5-arg: `(id, name, session, amount, paymentKey)`
- `transientOf` 팩토리 4-arg: `(name, session, amount, paymentKey)`
- `reschedule()`: `paymentKey` 유지

---

## 3. DB 스키마 변경

**파일**: `src/main/resources/schema.sql`

```sql
ALTER TABLE reservation
    ADD COLUMN amount BIGINT NOT NULL DEFAULT 0;
ALTER TABLE reservation
    ADD COLUMN payment_key VARCHAR(255) NULL;
```

---

## 4. 예약 POST DTO 분리

**파일**: `controller/dto/PaymentReservationRequest.java`

기존 `ReservationRequest` (PUT/PATCH 재사용)와 분리. POST 전용 필드 추가:

| 필드           | 제약          |
|--------------|-------------|
| `paymentKey` | `@NotBlank` |
| `orderId`    | `@NotBlank` |
| `amount`     | `@NotNull`  |

---

## 5. SessionService 결제 흐름

**파일**: `service/SessionService.java`

`makeReservation(PaymentReservationRequest)`:

1. `paymentGateway.confirm(PaymentConfirmation)` 호출 → 실패 시 `TossPaymentException` throw (예약 저장 안 됨)
2. 성공 시 `reservationService.save(name, session, amount, paymentKey)` 호출

대기 승격(`promoteWaitingIfExists`)은 결제 없이 `(name, session, 0L, null)` 저장.

---

## 6. 예외 처리

**파일**: `exception/ProblemDetailsAdvice.java`

`TossPaymentException` 핸들러 추가. `exception.getStatus()` / `exception.getCode()`로 RFC 9457 ProblemDetail 응답 반환.

---

## 7. JdbcReservationRepository 변경

**파일**: `repository/JdbcReservationRepository.java`

- SELECT: `r.amount`, `r.payment_key` 추가
- INSERT: `HashMap` 사용 (null 허용, `Map.of`는 null 불가)
- UPDATE SQL: `payment_key = ?` 추가

---

---

# [2차] 결제 무결성 강화

> 요구사항: orderId 서버에서 생성, 금액 위변조 방어, 결제 취소 시 대기 레코드 정리

---

## 8. pending_payment 테이블

**요구사항**: 클라이언트가 제출한 금액을 그대로 Toss에 전달하면 위변조 가능. 결제 시작 전에 서버가 금액을 DB에 저장하고, 승인 시점에 비교해야 한다.

**파일**: `src/main/resources/schema.sql`, `src/test/resources/test-setup.sql`

```sql
CREATE TABLE pending_payment
(
    order_id   VARCHAR(64) NOT NULL,
    amount     BIGINT      NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id)
);
```

---

## 9. PendingPayment 도메인 및 레포지토리

**요구사항**: orderId → 저장 금액 조회, 결제 완료/취소 시 삭제.

| 파일                                             | 설명                                                   |
|------------------------------------------------|------------------------------------------------------|
| `domain/PendingPayment.java`                   | `record PendingPayment(String orderId, Long amount)` |
| `repository/PendingPaymentRepository.java`     | `save` / `findByOrderId` / `deleteByOrderId` 인터페이스   |
| `repository/JdbcPendingPaymentRepository.java` | `SimpleJdbcInsert` + `JdbcTemplate` 구현               |

---

## 10. PaymentAmountMismatchException

**요구사항**: Toss 승인 API 호출 전에 저장 금액 ≠ 요청 금액이면 차단 (승인 후 불일치 검증은 의미 없음).

**파일**: `payment/PaymentAmountMismatchException.java`

- `RuntimeException` 상속, 생성자에서 "저장된 금액 vs 요청 금액" 메시지 포함
- `ProblemDetailsAdvice`에 핸들러 추가 → HTTP 400 + code `PAYMENT_AMOUNT_MISMATCH`

---

## 11. POST /payments/prepare — orderId 서버 발급

**요구사항**: 클라이언트가 orderId를 생성하면 임의 값 삽입 가능. 서버가 UUID로 생성하고 금액과 함께 `pending_payment`에 저장해야 한다.

**파일**: `controller/PaymentController.java`, `controller/dto/PreparePaymentRequest.java`

```
POST /payments/prepare          { "amount": 50000 }
→ 200 { "orderId": "uuid" }   (pending_payment 행 삽입)

DELETE /payments/prepare/{orderId}
→ 204                          (pending_payment 행 삭제, 존재하지 않으면 no-op)
```

`SessionService.preparePayment(Long)` / `cancelPreparedPayment(String)` 위임.

---

## 12. SessionService.makeReservation — 금액 검증 추가

**요구사항**: Toss 승인 전에 DB 저장 금액과 요청 금액을 비교해 위변조를 차단한다.

**파일**: `service/SessionService.java`

변경 후 실행 순서:

1. `findSessionOrThrow` — 세션 존재 확인
2. `pendingPaymentRepository.findByOrderId` — 없으면 `IllegalArgumentException` (400)
3. **금액 비교** — 불일치 시 `PaymentAmountMismatchException` (400), Toss 호출 없음
4. `paymentGateway.confirm` — Toss 승인
5. `pendingPaymentRepository.deleteByOrderId` — 대기 레코드 정리
6. `reservationService.save` — 예약 저장 (과거 날짜 검증은 여기서 발생)

---

## 13. 테스트 변경

**요구사항**: 기존 단위/통합 테스트가 새 흐름(pending_payment 선행 필요)을 통과해야 한다.

| 파일                                                       | 변경 내용                                                                                                     |
|----------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `test/repository/FakePendingPaymentRepository.java` (신규) | 단위 테스트용 인메모리 구현. `deleteByOrderId` no-op (클린업은 통합 테스트 관심사)                                                |
| `test/service/SessionServiceTest.java`                   | 생성자 7번째 인자 추가, `@BeforeEach`에 `("order_test", 0L)` 사전 저장, `PaymentAmountMismatchException` 테스트 추가         |
| `test/integration/RoomescapeIntegrationTest.java`        | `insertTestData()`에 `pending_payment ('order_test', 0)` 삽입 추가 (POST /reservations 테스트가 pending 조회를 통과하도록) |

---

---

# [3차] 타임아웃 방어 · 멱등 재시도 · 주문/결제 내역

> 1단계에서 붙인 토스 호출에 타임아웃·멱등 재시도 방어를 더한다. 느린 호출이 스레드를 무한정
> 잡지 못하게 하고, 결과가 불명확한 read timeout을 "실패"로 단정하지 않으며, 주문당 고정
> Idempotency-Key로 중복 승인을 막고, 사용자가 주문/결제 상태를 확인할 수 있게 한다.

## 14. 학습 테스트 — learning-test-2-timeout

**파일**: `src/test/java/roomescape/learning/TimeoutLearningTest.java`

본 구현 전에 타임아웃의 감각을 먼저 잡는 학습 테스트. `MockWebServer`로 느린 서버를 흉내 내,
connect/read timeout을 걸었을 때 **얼마나 기다렸다 어떤 예외로 실패하는지**를 경과 시간 단언으로 확인한다.

| 테스트             | 재현                                | 확인                                                                                                                                |
|-----------------|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| read timeout    | 바디 지연(3s) > read timeout(0.5s)    | read timeout만큼만 기다린 뒤 `RestClientException`(root `SocketTimeoutException` "Read timed out")                                       |
| connect timeout | SYN 무응답 블랙홀 IP(`10.255.255.1:81`) | connect timeout(1s)만큼만 기다린 뒤 `ResourceAccessException`(root `SocketTimeoutException` "Connect timed out") — OS 기본값(수십 초)까지 잡히지 않음 |
| TPS             | 빠른 3건 + 느린 1건                     | 느린 1건이 read timeout으로 잘려, 전체가 서버 지연(3s)에 묶이지 않고 빠른 3건 성공                                                                          |

## 15. [요구사항 2] 타임아웃·연결 실패 예외 구분

**파일**: `payment/PaymentConnectionException.java`, `payment/PaymentResultUnknownException.java`,
`exception/ProblemDetailsAdvice.java`

타임아웃·연결 실패를 토스 에러 응답(`{code,message}`)과 구분하기 위한 도메인 예외 타입과
RFC 9457 ProblemDetail 응답 규약을 정의한다. (게이트웨이에서 이 타입으로 변환하는 연동은 §16)

| 도메인 예외                          | 상황 / 근본 원인                                                                                | 의미                                  | 응답                                              |
|---------------------------------|-------------------------------------------------------------------------------------------|-------------------------------------|-------------------------------------------------|
| `PaymentConnectionException`    | 연결 거부·connect timeout (`ConnectException` / `SocketTimeoutException` "Connect timed out") | 토스에 닿지 못함 → **안 됨, 재시도 안전**         | 503 `PAYMENT_GATEWAY_UNREACHABLE`               |
| `PaymentResultUnknownException` | read timeout (`SocketTimeoutException` "Read timed out")                                  | 승인 여부 불명확 → **"실패"로 단정하지 않고 확인 필요** | 504 `PAYMENT_RESULT_UNKNOWN` + "내역에서 확인/재시도" 안내 |

핵심: 결과가 불명확한 실패를 성공/실패 둘 중 하나로 성급히 결론짓지 않는다.

## 16. [요구사항 3] Idempotency-Key · 결제 원장 · 게이트웨이 연동

### 16-1. payment_order (결제 원장)

`pending_payment`(orderId·amount만 보유, 승인 후 삭제)를 주문 생애주기를 담는 `payment_order`로 확장.
승인 성공/실패 후에도 삭제하지 않고 상태를 남겨 내역 조회(§17)의 근거가 된다.

```sql
CREATE TABLE payment_order
(
    order_id        VARCHAR(64)  NOT NULL,
    amount          BIGINT       NOT NULL,
    idempotency_key VARCHAR(300) NOT NULL,                   -- 주문당 고정 멱등키(≤300자)
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- PENDING/CONFIRMED/FAILED/UNKNOWN
    name            VARCHAR(255) NULL,
    session_id      BIGINT NULL REFERENCES session(id),
    payment_key     VARCHAR(255) NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id)
);
```

| 파일                                                                          | 설명                                                            |
|-----------------------------------------------------------------------------|---------------------------------------------------------------|
| `domain/PaymentOrder.java`, `domain/PaymentOrderStatus.java`                | 주문 원장 도메인 + 상태 전이(`confirmed`/`failed`/`unknown`/`retryable`) |
| `repository/PaymentOrderRepository.java`, `JdbcPaymentOrderRepository.java` | save/update/findByOrderId/findByName/delete                   |
| `service/PaymentOrderService.java`                                          | prepare/cancel/confirm + 실패 기록                                |
| `service/dto/PaymentHistory.java`                                           | 내역 read-model (엔드포인트는 §17)                                    |

`PendingPayment` 도메인/레포 및 관련 테스트는 삭제, `FakePaymentOrderRepository`로 교체.

### 16-2. Idempotency-Key (주문당 고정 UUID)

**파일**: `payment/domain/PaymentConfirmation.java`, `payment/gateway/toss/TossPaymentGateway.java`

주문 생성(`/payments/prepare`) 시 멱등키(UUID)를 발급·저장하고, confirm 호출에 `Idempotency-Key`
헤더로 전송한다. 키는 주문에 고정되어 **재시도해도 같은 키** → 토스가 첫 응답을 그대로 반환해
이중 승인 방지. 1차의 `ALREADY_PROCESSED_PAYMENT` 처리와 함께 두 겹 방어.
추가로 우리 쪽에서도 주문이 이미 `CONFIRMED`면 토스를 다시 부르지 않고 기존 예약을 반환(success 새로고침 방어).

### 16-3. 게이트웨이 예외 변환 (§15 타입으로 연동)

`TossPaymentGateway`가 `RestClient`의 전송 실패(`RestClientException`)를 root cause로 분기해
§15의 `PaymentConnectionException`/`PaymentResultUnknownException`으로 변환한다. onStatus가 던지는
`TossPaymentException`은 `RestClientException`이 아니므로 토스 에러와 깔끔히 분리된다.

### 16-4. makeReservation 흐름 + 롤백돼도 살아남는 실패 기록

**파일**: `service/SessionService.java`, `service/PaymentOrderService.java`

`makeReservation`은 `@Transactional`이라 confirm 실패로 예외를 던지면 롤백된다.
"확인 필요(UNKNOWN)"/"실패(FAILED)" 상태가 사라지지 않도록 실패 기록은 `PaymentOrderService`의
`@Transactional(REQUIRES_NEW)`(별도 빈) 메서드로 독립 커밋한다. 성공 기록(CONFIRMED)은 예약 저장과
원자성이 필요하므로 외부 트랜잭션에 합류시킨다.

| confirm 결과   | 기록           | 상태                 |
|--------------|--------------|--------------------|
| 성공(DONE)     | 외부 tx 합류     | CONFIRMED + 예약 저장  |
| read timeout | REQUIRES_NEW | UNKNOWN (확인 필요)    |
| 토스 거절/오류     | REQUIRES_NEW | FAILED             |
| 연결 실패        | REQUIRES_NEW | PENDING 유지(재시도 안전) |

## 17. [요구사항 4] 주문/결제 내역 조회

**파일**: `controller/PaymentController.java` (`GET /payments?userName=`),
`controller/dto/PaymentHistoryResponse.java`

로그인 사용자의 주문 목록을 예약 정보(날짜·시간·테마)와 결제 상태로 함께 반환한다.
상태 라벨: `PENDING`→결제 대기, `CONFIRMED`→확정, `FAILED`→실패, `UNKNOWN`→**확인 필요**.
`orderId`·`paymentKey`(승인 시)·`amount` 포함. read timeout으로 불명확한 주문은 "실패"가 아닌
"확인 필요"로 보여 주며, §16의 멱등키로 안전한 재시도가 보장된다.
(화면은 외부 클라이언트가 이 API를 소비)

---

---

# [4차] Rate Limit (호출량 상한)

> 같은 토큰 버킷 알고리즘을 두 경계에 적용한다. 들어오는 요청이 몰리면 초과분을 429로 거부하고(서버 입장),
> 토스를 호출할 땐 나가는 호출량을 스스로 조절한다(클라이언트 입장). 토스가 429를 주면 Retry-After를
> 존중해 백오프 재시도한다. 요구사항별로 커밋을 나눴고, 아래 각 절이 그 단계에 대응한다.

## 18. 학습 테스트 — learning-test-3-ratelimit

**파일**: `src/test/java/roomescape/learning/RateLimitLearningTest.java`

토큰 버킷 하나로 순간 버스트(`capacity`)와 평균 처리량 상한(`refillPerSec`)을 어떻게 거는지,
같은 알고리즘을 방향만 바꿔 들어오는 요청과 나가는 호출에 어떻게 적용하는지를 결정적 가짜 시계로 확인한다.

## 19. [요구사항 1] 토큰 버킷

**파일**: `ratelimit/TokenBucketRateLimiter.java`

`capacity`(허용 버스트)와 `refillPerSec`(평균 TPS 상한)을 가진 토큰 버킷. 외부 의존성 없음.

- 보충 = "마지막 보충 이후 경과 시간 × refillPerSec", `capacity`를 넘지 않음
- `tryConsume()`: 토큰 ≥1이면 1개 소비 후 통과, 없으면 거부
- `retryAfterSeconds()`: 1개가 찰 때까지 필요한 초를 올림(`Math.ceil`)
- 시간은 `LongSupplier` 가짜 시계 주입으로 결정적 테스트, `synchronized`로 동시성 안전
  (테스트: 가짜 시계 보충 검증 + 동시 요청에서 정확히 `capacity`개만 통과)

## 20. [요구사항 2] 서버 관점 — 한도 초과 요청 거부

**파일**: `ratelimit/RateLimitInterceptor.java`, `config/RateLimitConfig.java`, `application.properties`

결제·예약 엔드포인트(`/reservations`, `/payments` 및 하위 경로)에 토큰 버킷을 적용한다.
`preHandle`에서 `tryConsume()`이 false면 컨트롤러를 호출하지 않고(false 반환) 429와 `Retry-After`
(= `retryAfterSeconds()`)를 응답한다. `capacity`/`refillPerSec`는 `rate-limit.*`로 외부화.

`WebMvcConfigurer` 생성자 `@Value`는 placeholder 해결 전에 인스턴스화돼 실패하므로,
`@Bean MappedInterceptor`로 등록한다(`@Bean` 메서드 파라미터 `@Value`는 정상 해결, `@WebMvcTest` 슬라이스도 비침투).
(테스트: 인터셉터 단위 테스트 + `rate-limit.capacity`만 바꿔 거부 시점이 달라지는 통합 테스트)

## 21. [요구사항 3] 클라이언트 관점 — 토스의 429에 백오프 재시도

**파일**: `ratelimit/RetryAfterInterceptor.java`, `payment/TossRateLimitException.java`,
`config/TossClientConfig.java`, `exception/ProblemDetailsAdvice.java`

토스 호출 `RestClient`에 `ClientHttpRequestInterceptor`를 등록한다. 응답이 429이고 시도 횟수가
`maxAttempts` 미만이면 `Retry-After`(초)만큼 대기 후 재시도하고, `Retry-After`가 없으면 고정 간격
(`toss.retry.fallback-seconds`, 기본 1초)으로 폴백한다. `maxAttempts`를 넘어도 429면
`TossRateLimitException`(503 `TOSS_RATE_LIMITED` + Retry-After)으로 실패한다(무한 재시도 금지).

재시도는 같은 요청을 그대로 다시 보내므로 2단계의 주문당 고정 `Idempotency-Key` 헤더가 유지된다.
`toss.retry.*`로 외부화. (테스트: 429→대기→200, 폴백, maxAttempts 초과 예외)

## 22. [요구사항 4] 클라이언트 관점 — 나가는 호출에 Rate Limit

**파일**: `ratelimit/OutboundRateLimitInterceptor.java`, `payment/OutboundRateLimitException.java`,
`config/TossClientConfig.java`, `exception/ProblemDetailsAdvice.java`

같은 `TokenBucketRateLimiter`(요구사항 1)를 방향만 바꿔 나가는 호출에 적용한다. 요구사항 3과 같은
게이트웨이 `RestClient`에 인터셉터를 하나 더 등록한다(재시도 바깥, 레이트리밋 안쪽 — 재시도마다 토큰 소비).
호출 전 `tryConsume()`으로 토큰을 소비하고, 없으면 외부로 보내지 않고 `OutboundRateLimitException`
(503 `PAYMENT_OUTBOUND_RATE_LIMITED` + Retry-After)으로 거부한다. 토큰이 보충되면 다시 나간다.

나가는 한도는 `outbound-rate-limit.*`로 들어오는 쪽(`rate-limit.*`)과 분리해 외부화한다.
들어오는/나가는 Rate Limit은 같은 알고리즘·다른 방향이다.

---

### **TODO**

- 자동 승격 예약 결제 처리
- 주문의 수렴: 결제 조회 API + 멱등 재시도로 성공/실패로 확정
