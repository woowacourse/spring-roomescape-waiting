# Step2 구현 계획 — 타임아웃 · 예외 처리 · 멱등 재시도

---

## 결론 먼저

> 변경이 필요한 레이어는 4곳이다.
> `TossPaymentConfig` (타임아웃 설정) →
> `TossPaymentGateway` (예외 분류 + 멱등키) →
> `PaymentService` (uncertain 상태 저장) →
> 내역 조회 API (신규)
>
> `orderId`는 이미 Reservation에 있으므로 멱등키 저장 작업은 없다.

---

## 현재 코드 상태 파악

### 무엇이 없는가

| 위치 | 없는 것 |
|------|---------|
| `TossPaymentConfig` | `SimpleClientHttpRequestFactory`, timeout 설정 |
| `TossPaymentGateway` | `Idempotency-Key` 헤더, timeout 예외 분류 |
| `PaymentService` | uncertain 상태 분기, 상태 저장 |
| `Payment` domain | `PaymentStatus` 필드 |
| `ErrorCode` | `PAYMENT_CONNECTION_TIMEOUT`, `PAYMENT_READ_TIMEOUT` |
| DB schema | `payment.status` 컬럼 |
| API | 내역 조회 엔드포인트 |

### 무엇이 이미 있는가

- `Reservation.orderId` → 멱등키로 그대로 쓸 수 있다. 주문 생성 시 UUID가 이미 발급된다.
- `TossPaymentGateway.mapToErrorCode()` → Toss 비즈니스 에러 분류 완성.
- `PaymentService.handlePaymentFail()` → Toss 리다이렉트 실패 콜백 처리 완성.

---

## 구현 순서

### Step 1 — ErrorCode 추가

`global/exception/ErrorCode.java`에 두 가지를 추가한다.

```java
PAYMENT_CONNECTION_TIMEOUT(HttpStatus.SERVICE_UNAVAILABLE, "결제 서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요."),
PAYMENT_READ_TIMEOUT(HttpStatus.ACCEPTED, "결제 요청을 전송했으나 응답이 없습니다. 결제 내역에서 상태를 확인해 주세요."),
```

- `CONNECTION_TIMEOUT`: 연결조차 못 했으니 확정 실패 → `503`
- `READ_TIMEOUT`: 서버가 처리했을 수도 있는 불확실한 상태 → `202`(Accepted)
  클라이언트에게 "됐는지 모르니 내역을 확인하라"는 의미를 담는다.

---

### Step 2 — 타임아웃 설정 (`TossPaymentConfig`)

#### 방식 선택

요구사항은 `simple` 또는 `apache` 팩토리를 권장한다.
`jdk` 팩토리는 응답 바디 지연을 read timeout으로 잡지 못하기 때문이다.
→ **`SimpleClientHttpRequestFactory` 코드 방식**을 선택한다.

property 방식(`spring.http.clients.*`)은 Spring Boot 4.0 이상 기능으로,
현재 프로젝트가 3.4.x이므로 코드 방식이 더 명확하다.

#### 변경 내용

`application.properties` 추가:
```properties
toss.connect-timeout=3000
toss.read-timeout=10000
```

`TossPaymentConfig.java` 변경:
```java
@Value("${toss.connect-timeout}")
private int connectTimeout;

@Value("${toss.read-timeout}")
private int readTimeout;

@Bean
public RestClient tossRestClient() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(connectTimeout);
    factory.setReadTimeout(readTimeout);

    String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
    return RestClient.builder()
            .requestFactory(factory)
            .baseUrl("https://api.tosspayments.com")
            .defaultHeader("Authorization", "Basic " + encoded)
            .defaultHeader("Content-Type", "application/json")
            .build();
}
```

타임아웃 값 근거:
- `connectTimeout=3000ms`: 연결 자체가 3초 이상 걸리면 서버 자체 문제이므로 빠르게 포기.
- `readTimeout=10000ms`: 토스 응답 p99가 수초 수준임을 감안해 여유 있게 설정.
  실제 운영에서는 부하 테스트 결과 기반으로 조정해야 한다.

---

### Step 3 — 멱등키 + 예외 분류 (`TossPaymentGateway`)

#### 멱등키

`confirm(PaymentConfirmation confirmation)` 호출부에 헤더 추가:

```java
TossPaymentResponse response = restClient.post()
        .uri(CONFIRM_PATH)
        .header("Idempotency-Key", confirmation.orderId())  // 추가
        .body(request)
        .retrieve()
        .onStatus(...)
        .body(TossPaymentResponse.class);
```

`orderId`는 이미 주문 생성 시 UUID로 고정 발급되어 있으므로,
재시도 시에도 같은 `orderId`가 전달된다 → 멱등성 보장.

#### 예외 분류

`ResourceAccessException`의 root cause로 상황을 구분한다.

```java
try {
    TossPaymentResponse response = restClient.post()
            ...
            .body(TossPaymentResponse.class);
    ...
} catch (ResourceAccessException e) {
    Throwable cause = e.getCause();
    if (cause instanceof SocketTimeoutException ste && ste.getMessage().contains("Read")) {
        throw new CustomException(ErrorCode.PAYMENT_READ_TIMEOUT);
    }
    throw new CustomException(ErrorCode.PAYMENT_CONNECTION_TIMEOUT);
}
```

| 상황 | 예외 | root cause | ErrorCode |
|------|------|-----------|-----------|
| connect timeout / 연결 거부 | `ResourceAccessException` | `ConnectException` / `SocketTimeoutException`(connect) | `PAYMENT_CONNECTION_TIMEOUT` |
| read timeout (응답 지연) | `ResourceAccessException` | `SocketTimeoutException`("Read timed out") | `PAYMENT_READ_TIMEOUT` |
| Toss 비즈니스 거절 | `CustomException` (이미 처리됨) | — | 기존 코드 유지 |

**핵심**: `PAYMENT_READ_TIMEOUT`은 "실패"가 아니라 "불확실"이다.
`PaymentService`에서 이 예외를 잡아 UNCERTAIN 상태로 저장해야 한다.

---

### Step 4 — PaymentStatus 추가

#### `PaymentStatus.java` (신규)

```java
public enum PaymentStatus {
    CONFIRMED,   // 정상 승인
    UNCERTAIN    // read timeout - 승인됐는지 모름
}
```

#### `Payment.java` 변경

`status` 필드 추가. 기존 생성자는 `CONFIRMED`를 기본값으로 유지해 하위 호환을 깨지 않는다.

```java
private final PaymentStatus status;

// 기존 생성자: status = CONFIRMED
// 신규 생성자: status 명시 가능
```

#### DB schema 변경

`src/main/resources/schema.sql`에 `payment` 테이블 컬럼 추가:
```sql
ALTER TABLE payment ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED';
```
또는 CREATE TABLE 문에 `status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'` 컬럼 추가.

#### `JdbcPaymentRepository` 변경

`save()` 시 `status` 컬럼 포함, `rowMapper`에 `status` 매핑 추가.

---

### Step 5 — uncertain 분기 처리 (`PaymentService`)

`confirmPayment()`에서 `PAYMENT_READ_TIMEOUT` 발생 시 예약을 PENDING 유지한 채 UNCERTAIN Payment를 저장한다.

```java
@Transactional
public Payment confirmPayment(PaymentConfirmation confirmation) {
    Reservation reservation = reservationRepository.findByOrderId(confirmation.orderId())
            .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

    if (!confirmation.amount().equals(reservation.getAmount())) {
        throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
    }

    try {
        PaymentResult result = paymentGateway.confirm(confirmation);
        Payment payment = paymentRepository.save(
                new Payment(reservation.getId(), result.paymentKey(), result.orderId(), result.amount(), PaymentStatus.CONFIRMED)
        );
        reservation.confirm();
        reservationRepository.updateStatus(reservation);
        // ThemeSlot update 유지
        ...
        return payment;

    } catch (CustomException e) {
        if (e.getErrorCode() == ErrorCode.PAYMENT_READ_TIMEOUT) {
            // 불확실 상태: 예약은 PENDING 유지, UNCERTAIN 기록만 저장
            return paymentRepository.save(
                    new Payment(reservation.getId(), null, confirmation.orderId(), confirmation.amount(), PaymentStatus.UNCERTAIN)
            );
        }
        throw e;
    }
}
```

**왜 예약을 PENDING으로 유지하는가?**
read timeout 시 토스가 이미 승인을 처리했을 수 있다.
예약을 CANCELLED로 바꾸면 실제로 결제는 됐는데 예약이 취소되는 더 큰 문제가 생긴다.
사용자가 내역 페이지에서 확인하고 안전하게 재시도(같은 멱등키로)할 수 있도록 놔둔다.

---

### Step 6 — 내역 조회 API

요구사항: "로그인한 사용자의 주문(예약) 목록에 예약 정보 + 결제 상태 표시"

#### 엔드포인트

`GET /payment/history` (또는 `/reservations/mine` 패턴에 통합)

로그인 사용자의 `name`(또는 memberId)으로 Reservation을 조회하고,
각 Reservation에 연결된 Payment를 JOIN 또는 개별 조회해서 내려준다.

#### 응답 DTO

```java
public record ReservationPaymentResponse(
    Long reservationId,
    String themeName,
    LocalDate date,
    String timeValue,
    String reservationStatus,
    String orderId,
    String paymentKey,       // UNCERTAIN이면 null
    Long amount,
    String paymentStatus     // "CONFIRMED" | "UNCERTAIN" | "PENDING(결제 전)"
) {}
```

#### 구현 위치

- `PaymentService.getPaymentHistory(String memberName)` 또는 `ReservationService` 통합
- `PaymentController.GET /payment/history`

---

## 변경 파일 목록 요약

| 파일 | 변경 유형 | 내용 |
|------|----------|------|
| `application.properties` | 추가 | `toss.connect-timeout`, `toss.read-timeout` |
| `ErrorCode.java` | 추가 | `PAYMENT_CONNECTION_TIMEOUT`, `PAYMENT_READ_TIMEOUT` |
| `PaymentStatus.java` | 신규 | `CONFIRMED`, `UNCERTAIN` |
| `Payment.java` | 수정 | `status` 필드 추가 |
| `TossPaymentConfig.java` | 수정 | `SimpleClientHttpRequestFactory` + timeout |
| `TossPaymentGateway.java` | 수정 | `Idempotency-Key` 헤더, timeout 예외 분류 |
| `PaymentService.java` | 수정 | uncertain 분기 처리, 내역 조회 메서드 |
| `JdbcPaymentRepository.java` | 수정 | `status` 컬럼 CRUD |
| `schema.sql` | 수정 | `payment.status` 컬럼 |
| `PaymentController.java` | 수정 | `GET /payment/history` 추가 |

---

## 테스트 전략

### TossPaymentGateway — MockWebServer 슬라이스 테스트

```java
// connect timeout: 블랙홀 IP(10.255.255.1)는 테스트 환경에서 쓰기 어려우므로
// MockWebServer에서 응답을 보내기 전에 소켓을 close해서 거부 상황 재현
server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));

// read timeout: 응답 지연으로 재현
server.enqueue(new MockResponse()
    .setBodyDelay(readTimeout + 500, TimeUnit.MILLISECONDS)
    .setBody("{}"));
```

검증 포인트:
- connect 실패 → `CustomException(PAYMENT_CONNECTION_TIMEOUT)`
- read timeout → `CustomException(PAYMENT_READ_TIMEOUT)`
- 멱등키 헤더 → `RecordedRequest.getHeader("Idempotency-Key")` == orderId

### PaymentService — 단위 테스트

- `paymentGateway.confirm()` Mock이 `PAYMENT_READ_TIMEOUT` 던질 때
  → UNCERTAIN Payment 저장, 예약 상태 PENDING 유지
  → `reservation.confirm()` 호출되지 않음

---

## 추가 고민할 것

1. `UNCERTAIN` 상태를 어떻게 `CONFIRMED`로 수렴시킬 것인가?
   - 결제 조회 API(`GET /v1/payments/{orderId}`)로 실제 상태를 폴링하는 배치나 스케줄러가 필요하다.
   - 이번 단계에서는 "사용자가 내역에서 확인 후 재시도"로 처리한다.

2. `Idempotency-Key` 유효기간(15일) 이후 재시도는?
   - orderId를 새로 발급해야 한다. → 현재는 미지원 범위.

3. `PaymentController.confirm()`의 파라미터가 `@RequestParam`인데
   Toss 리다이렉트는 query string으로 내려오므로 맞다.
   단, `amount`가 `Long`이어서 Toss가 문자열 `"50000"`을 보낼 때 자동 변환되는지 확인 필요.
