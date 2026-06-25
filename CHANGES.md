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

### **TODO**

- 자동 승격 예약 결제 처리
