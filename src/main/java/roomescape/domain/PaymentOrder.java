package roomescape.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class PaymentOrder {
    private final Long id;
    private final String orderId;
    private final Long reservationId;
    private final Long amount;
    private final PaymentOrderStatus status;
    private final LocalDateTime createdAt;
    private final String idempotencyKey;

    public PaymentOrder(Long id, String orderId, Long reservationId, Long amount, String idempotencyKey, PaymentOrderStatus status, LocalDateTime createdAt) {
        Objects.requireNonNull(orderId, "주문 ID는 필수값 입니다.");
        Objects.requireNonNull(reservationId, "예약 ID는 필수값 입니다.");
        Objects.requireNonNull(amount, "결제 금액은 필수값 입니다.");
        Objects.requireNonNull(status, "결제 주문 상태는 필수값 입니다.");
        Objects.requireNonNull(idempotencyKey, "멱등키는 필수값 입니다.");
        Objects.requireNonNull(createdAt, "결제 주문 생성일시는 필수값 입니다.");

        this.id = id;
        this.orderId = orderId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
    }

    public static PaymentOrder createPendingWithoutId(
            String orderId,
            Long reservationId,
            Long amount,
            String idempotencyKey,
            LocalDateTime createdAt
    ) {
        return new PaymentOrder(null, orderId, reservationId, amount, idempotencyKey, PaymentOrderStatus.PENDING, createdAt);
    }

    public PaymentOrder confirm() {
        if (status != PaymentOrderStatus.PENDING) {
            throw new IllegalStateException("결제 대기 상태의 주문만 확정할 수 있습니다.");
        }

        return new PaymentOrder(
                id,
                orderId,
                reservationId,
                amount,
                idempotencyKey,
                PaymentOrderStatus.CONFIRMED,
                createdAt
        );
    }

    public PaymentOrder unknown() {
        if (status != PaymentOrderStatus.PENDING) {
            throw new IllegalStateException("결제 대기 상태의 주문만 확인 필요 상태로 변경할 수 있습니다.");
        }

        return new PaymentOrder(
                id,
                orderId,
                reservationId,
                amount,
                idempotencyKey,
                PaymentOrderStatus.UNKNOWN,
                createdAt
        );
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getAmount() {
        return amount;
    }

    public PaymentOrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
