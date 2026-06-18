# Toss Payments 연동 구현 계획

## 현재 코드 상태 파악

### 현재 예약 생성 흐름
```
POST /reservations
  → ReservationController.createReservation()
  → ReservationService.saveReservation(name, themeSlotId)
  → ThemeSlot.addReservation(name)
      ├── 자리 없음 → Pending 상태로 저장 (대기)
      └── 자리 있음 → reservation.confirm() 호출 → Confirmed 상태로 저장
```

**문제**: 결제 없이 바로 CONFIRMED가 된다.  
**변경 목표**: 결제 성공 이후에만 CONFIRMED로 전환.

---

## 구현 항목 목록

### 1. DB 스키마 변경 (`schema.sql`)

#### `reservation` 테이블에 컬럼 추가
```sql
order_id VARCHAR(64) UNIQUE  -- 결제 전 서버가 생성하는 주문 ID (금액 검증 기준)
```

#### `payment` 테이블 신규 생성
```sql
CREATE TABLE IF NOT EXISTS payment (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT      NOT NULL,
    payment_key   VARCHAR(255) NOT NULL,
    order_id      VARCHAR(64)  NOT NULL,
    amount        BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation(id)
);
```

---

### 2. 도메인 레이어 — 포트 & 도메인 모델 신규 추가

#### 2-1. `PaymentGateway` 포트 인터페이스
- **위치**: `roomescape/domain/payment/PaymentGateway.java`
- **역할**: 애플리케이션 계층이 결제 승인을 요청하는 포트. Toss를 모른다.
```java
public interface PaymentGateway {
    PaymentResult confirm(PaymentConfirmation confirmation);
}
```

#### 2-2. `PaymentConfirmation` 도메인 모델
- **위치**: `roomescape/domain/payment/PaymentConfirmation.java`
- **필드**: `paymentKey`, `orderId`, `amount`
- **역할**: 결제 승인 요청 데이터 (브라우저가 넘긴 값 그대로)

#### 2-3. `PaymentResult` 도메인 모델
- **위치**: `roomescape/domain/payment/PaymentResult.java`
- **필드**: `paymentKey`, `orderId`, `amount`
- **역할**: 승인 완료 후 저장할 결제 결과

#### 2-4. `Payment` 도메인 모델
- **위치**: `roomescape/domain/payment/Payment.java`
- **필드**: `id`, `reservationId`, `paymentKey`, `orderId`, `amount`
- **역할**: 결제 저장 엔티티

---

### 3. 어댑터 레이어 — Toss 연동 구현체 (ACL)

#### 3-1. Toss DTO 클래스들
- **위치**: `roomescape/infra/toss/dto/`
- `TossPaymentRequest.java` — `paymentKey`, `orderId`, `amount`
- `TossPaymentResponse.java` — `paymentKey`, `orderId`, `totalAmount` (Toss 응답 역직렬화용)
- `TossErrorResponse.java` — `code`, `message`

#### 3-2. `TossPaymentGateway` 어댑터
- **위치**: `roomescape/infra/toss/TossPaymentGateway.java`
- **구현**: `PaymentGateway` 포트 구현체
- **역할**:
  - `RestClient`로 `POST https://api.tosspayments.com/v1/payments/confirm` 호출
  - Authorization 헤더: `Basic base64(secretKey + ":")`
  - `onStatus(isError)` → `TossErrorResponse` 역직렬화 → 도메인 예외 변환
  - `TossPaymentResponse` → `PaymentResult` 변환
  - **Toss DTO는 이 클래스 밖으로 절대 유출되지 않는다**

#### 3-3. RestClient 빈 설정
- **위치**: `roomescape/global/config/TossPaymentConfig.java`
- `RestClient.builder().baseUrl("https://api.tosspayments.com").build()`
- 시크릿 키는 `@Value("${toss.secret-key}")` 주입

---

### 4. 기존 코드 변경

#### 4-1. `ThemeSlot.addReservation()` 변경 ★ 핵심
- **현재**: 자리 있으면 `reservation.confirm()` 자동 호출
- **변경**: 항상 `Pending` 상태로만 추가. `confirm()`은 결제 완료 후 `PaymentService`에서 호출
- `isReserved` 플래그도 결제 완료 시점에 업데이트하도록 변경

> **영향**: `cancelReservation`에서 대기자를 승격할 때 `Reservation::confirm()` 호출은 그대로 유지 (대기자 승격은 재결제 불필요)

#### 4-2. `ReservationService.saveReservation()` 변경
- 예약 저장 시 `orderId` 생성 (`UUID` 기반, 6~64자 영숫자/-/_)
- 생성한 `orderId`와 `amount`를 예약에 함께 저장
- 반환 DTO에 `orderId`와 `amount` 포함

#### 4-3. `ReservationController.createReservation()` 변경
- 응답 DTO에 `orderId`, `amount` 추가 (클라이언트 결제창 초기화에 사용)

---

### 5. `PaymentService` 신규 추가
- **위치**: `roomescape/service/PaymentService.java`
- **의존**: `PaymentGateway`(포트), `ReservationRepository`, `PaymentRepository`
- **Toss를 모른다**

#### `confirmPayment(PaymentConfirmation confirmation)` 메서드
```
1. orderId로 예약 조회
2. 금액 검증: reservation.amount != confirmation.amount → PaymentAmountMismatch 예외 (승인 호출 전 차단)
3. paymentGateway.confirm(confirmation) 호출 → PaymentResult 반환
4. Payment 저장 (paymentKey, orderId, amount)
5. reservation.confirm() → 상태를 CONFIRMED로 변경
6. ThemeSlot.isReserved = true 업데이트
7. reservationRepository.updateStatus(reservation)
```

---

### 6. `PaymentController` 신규 추가
- **위치**: `roomescape/controller/PaymentController.java`

#### `POST /payment/confirm` — successUrl 콜백
- **Request Params**: `paymentKey`, `orderId`, `amount`
- `PaymentConfirmation` 생성 → `PaymentService.confirmPayment()` 호출
- 성공 시 예약 완료 페이지로 리다이렉트 또는 응답 반환

#### `GET /payment/fail` — failUrl 콜백
- **Request Params**: `code`, `message`, `orderId` (nullable)
- `orderId` null 가드 필수 (사용자 취소 `PAY_PROCESS_CANCELED` 시 orderId 없음)
- `orderId` 있으면 해당 예약 삭제 또는 Cancelled 처리
- 실패 사유를 사용자에게 응답

---

### 7. `PaymentRepository` 신규 추가
- **포트**: `roomescape/repository/PaymentRepository.java`
  - `save(Payment payment)`
  - `findByOrderId(String orderId)`
- **구현체**: `roomescape/infra/JdbcPaymentRepository.java`

---

### 8. 에러 코드 추가 (`ErrorCode.java`)

| ErrorCode | HttpStatus | message |
|---|---|---|
| `PAYMENT_AMOUNT_MISMATCH` | 422 | 결제 금액이 주문 금액과 일치하지 않습니다. |
| `PAYMENT_ALREADY_PROCESSED` | 400 | 이미 처리된 결제입니다. |
| `PAYMENT_CARD_REJECTED` | 403 | 카드 결제가 거절되었습니다. |
| `PAYMENT_UNAUTHORIZED_KEY` | 401 | 결제 인증 키가 유효하지 않습니다. (운영 알람 필요) |
| `PAYMENT_NOT_FOUND` | 404 | 결제 정보를 찾을 수 없습니다. |
| `PAYMENT_TOSS_INTERNAL_ERROR` | 502 | 결제 서버 내부 오류입니다. 잠시 후 재시도해 주세요. |
| `PAYMENT_UNKNOWN_ERROR` | 500 | 결제 처리 중 알 수 없는 오류가 발생했습니다. |

Toss `code` → `ErrorCode` 매핑은 `TossPaymentGateway` 내부에서 처리한다.

---

### 9. 설정 파일 변경 (`application.properties`)

```properties
toss.secret-key=test_sk_YOUR_KEY_HERE
toss.client-key=test_ck_YOUR_KEY_HERE
```

> `.gitignore`에 시크릿 키가 담긴 설정 파일 추가 또는 환경 변수로 분리

---

### 10. 테스트

#### 10-1. `TossPaymentGateway` 슬라이스 테스트
- **도구**: `@RestClientTest` + `MockRestServiceServer`
- 검증 항목:
  - 정상 승인 응답 → `PaymentResult` 반환
  - 4xx 에러 → Toss `code`별 도메인 예외 변환 확인
  - 5xx 에러 → `PAYMENT_TOSS_INTERNAL_ERROR` 예외

#### 10-2. `PaymentService` 단위 테스트
- `PaymentGateway` mock 처리
- 금액 불일치 시 `PaymentGateway.confirm()`이 호출되지 않는지 검증

---

## 변경 영향 요약

| 파일 | 변경 유형 |
|---|---|
| `schema.sql` | reservation 컬럼 추가, payment 테이블 생성 |
| `ThemeSlot.addReservation()` | 자동 confirm 제거 |
| `Reservation.java` | `orderId`, `amount` 필드 추가 |
| `ReservationService.saveReservation()` | orderId 생성 로직 추가 |
| `ReservationController` / `ReservationRequest` / `ReservationResponse` | orderId, amount 포함 |
| `ErrorCode.java` | 결제 관련 에러 추가 |
| `PaymentGateway.java` | 신규 (포트) |
| `PaymentConfirmation.java` | 신규 (도메인 모델) |
| `PaymentResult.java` | 신규 (도메인 모델) |
| `Payment.java` | 신규 (도메인 모델) |
| `TossPaymentGateway.java` | 신규 (어댑터/ACL) |
| `TossPaymentConfig.java` | 신규 (RestClient 빈 설정) |
| `TossPaymentRequest/Response/ErrorResponse.java` | 신규 (Toss DTO) |
| `PaymentService.java` | 신규 |
| `PaymentController.java` | 신규 |
| `PaymentRepository.java` | 신규 (포트 + 구현체) |

---

## 구현 순서 (권장)

1. **스키마 & 도메인 모델** — schema.sql, Reservation 필드 추가, Payment/PaymentConfirmation/PaymentResult
2. **포트 정의** — PaymentGateway, PaymentRepository 인터페이스
3. **ThemeSlot 변경** — 자동 confirm 제거 (기존 테스트 수정 동반)
4. **ReservationService 변경** — orderId 생성 + 저장
5. **TossPaymentGateway 어댑터** — RestClient, DTO, 에러 매핑
6. **PaymentService** — 금액 검증 + confirm 흐름
7. **PaymentController** — successUrl / failUrl 콜백
8. **테스트** — TossPaymentGateway 슬라이스, PaymentService 단위
