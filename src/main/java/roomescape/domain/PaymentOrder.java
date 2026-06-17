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

    public PaymentOrder(Long id, String orderId, Long reservationId, Long amount, PaymentOrderStatus status, LocalDateTime createdAt) {
        Objects.requireNonNull(orderId, "주문 ID는 필수값 입니다.");
        Objects.requireNonNull(reservationId, "예약 ID는 필수값 입니다.");
        Objects.requireNonNull(amount, "결제 금액은 필수값 입니다.");
        Objects.requireNonNull(status, "결제 주문 상태는 필수값 입니다.");
        Objects.requireNonNull(createdAt, "결제 주문 생성일시는 필수값 입니다.");

        this.id = id;
        this.orderId = orderId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static PaymentOrder createPendingWithoutId(String orderId, Long reservationId, Long amount, LocalDateTime createdAt) {
        return new PaymentOrder(null, orderId, reservationId, amount, PaymentOrderStatus.PENDING, createdAt);
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
}
