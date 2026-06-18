# 결제 연동 2단계 — 포트 정의

## 구현 목표

애플리케이션 계층이 Toss를 직접 알지 않도록 포트(인터페이스)를 먼저 정의한다.  
이후 단계에서 Toss 어댑터(`TossPaymentGateway`)와 JDBC 구현체(`JdbcPaymentRepository`)가 이 포트를 구현한다.

---

## 추가 파일

### `src/main/java/roomescape/domain/payment/PaymentGateway.java` (신규)

```java
public interface PaymentGateway {
    PaymentResult confirm(PaymentConfirmation confirmation);
}
```

- **위치**: `domain/payment/` — 애플리케이션 계층이 의존하는 포트이므로 domain 패키지에 배치.
- `PaymentService`가 이 인터페이스만 의존하고, Toss 구체 클래스는 모른다.
- 테스트 시 `PaymentGateway` mock으로 `TossPaymentGateway` 없이 `PaymentService` 단위 테스트 가능.

---

### `src/main/java/roomescape/repository/PaymentRepository.java` (신규)

```java
public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByOrderId(String orderId);
}
```

- **위치**: `repository/` — 기존 `ReservationRepository`, `ThemeRepository` 등과 같은 패키지 관례 따름.
- `save`: 결제 승인 완료 후 결제 정보를 영속화.
- `findByOrderId`: 결제 중복 처리 방지 및 조회용.

---

## 설계 결정 메모

| 결정 | 이유 |
|---|---|
| `PaymentGateway`를 `domain/payment/`에 배치 | 외부 결제 시스템 호출은 인프라지만, 포트(인터페이스) 자체는 도메인·서비스 계층의 것이다. 어댑터(`infra/toss/`)는 이 포트를 구현하기만 한다. |
| `PaymentRepository`를 `repository/`에 배치 | 기존 레포지터리 포트와 동일한 패키지. 일관성 유지. |
| `findByOrderId` 반환 타입 `Optional<Payment>` | orderId로 결제가 없을 수 있는 경우를 null 없이 처리하기 위함. |
