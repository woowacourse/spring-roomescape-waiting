# 변경 명세 — spring-roomescape-waiting

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

### **TODO**

- 예외 종류 / 매핑 추가
- 자동 승격 예약 결제 처리