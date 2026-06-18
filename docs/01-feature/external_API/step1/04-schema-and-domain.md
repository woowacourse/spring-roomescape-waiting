# 결제 연동 1단계 — 스키마 & 도메인 모델

## 구현 목표

Toss Payments 연동을 위한 기반 작업.  
DB 구조 변경과 도메인 모델 추가로 이후 단계의 비즈니스 로직이 의존할 계층을 마련한다.

---

## 변경 파일

### `src/main/resources/schema.sql`

**`reservation` 테이블 컬럼 추가**

```sql
order_id  VARCHAR(64)  NULL,
amount    BIGINT       NULL,
```

- `order_id`: 예약 생성 시 서버가 발급하는 주문 ID. 결제 승인 전까지 금액 위변조 검증 기준값.
- `amount`: 결제 요청 금액. 승인 직전 클라이언트가 넘긴 금액과 비교해 불일치 시 승인을 차단한다.
- 두 컬럼 모두 `NULL` 허용 — 결제가 필요 없는 대기 승격 경로의 기존 예약과 호환 유지.

**`payment` 테이블 신규 생성**

```sql
CREATE TABLE IF NOT EXISTS payment (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT       NOT NULL,
    payment_key    VARCHAR(200) NOT NULL,
    order_id       VARCHAR(64)  NOT NULL,
    amount         BIGINT       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (reservation_id) REFERENCES reservation (id)
);
```

- Toss로부터 받은 `paymentKey`를 저장해 결제 취소·조회 시 사용.
- `reservation_id` FK로 예약과 1:1 연결.

---

### `src/main/java/roomescape/domain/Reservation.java`

**`orderId`, `amount` 필드 추가**

```java
private final String orderId;
private final Long amount;
```

- 기존 생성자 2종: `orderId = null`, `amount = null`로 초기화 (하위 호환)
- `orderId`·`amount`를 받는 생성자 2종 추가
- `Reservation.of()` 팩토리 메서드도 두 필드를 전달하도록 수정
- `getOrderId()`, `getAmount()` getter 추가

---

### `src/main/java/roomescape/domain/payment/Payment.java` (신규)

결제 저장 엔티티. `reservationId`, `paymentKey`, `orderId`, `amount`를 보유한다.

```java
public class Payment {
    private final Long id;
    private final Long reservationId;
    private final String paymentKey;
    private final String orderId;
    private final Long amount;
    // ...
}
```

---

### `src/main/java/roomescape/domain/payment/PaymentConfirmation.java` (신규)

결제 승인 요청 데이터. 브라우저가 넘긴 값 그대로를 담는 불변 record.

```java
public record PaymentConfirmation(String paymentKey, String orderId, Long amount) {}
```

---

### `src/main/java/roomescape/domain/payment/PaymentResult.java` (신규)

Toss 승인 완료 후 서비스 계층이 돌려받는 결과 record. Toss DTO와 분리되어 어댑터 내부에서만 변환된다.

```java
public record PaymentResult(String paymentKey, String orderId, Long amount) {}
```

---

## 설계 결정 메모

| 결정 | 이유 |
|---|---|
| `order_id`에 `UNIQUE` 제약 미적용 | NULL 허용 컬럼에 UNIQUE를 걸면 DB마다 동작 차이가 있어 생략. 애플리케이션 레벨에서 중복 방지. |
| `Payment`를 `domain/payment/` 패키지에 배치 | 결제 도메인 모델은 인프라(Toss)를 모른다. 어댑터와 분리하기 위해 domain 하위에 패키지 분리. |
| `PaymentConfirmation`·`PaymentResult`를 record로 | 불변 데이터 전달 객체이므로 record가 적합. |
