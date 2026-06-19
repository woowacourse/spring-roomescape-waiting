package roomescape.payment;

import java.time.LocalDateTime;

public class PaymentOrder {

    private final Long id;
    private final String orderId;
    private final Long memberId;
    private final Long scheduleId;
    private final int amount;
    private final String idempotencyKey;
    private final PaymentOrderStatus status;
    private final String paymentKey;
    private final Long reservationId;
    private final String failureCode;
    private final String failureMessage;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PaymentOrder(
            Long id,
            String orderId,
            Long memberId,
            Long scheduleId,
            int amount,
            String idempotencyKey,
            PaymentOrderStatus status,
            String paymentKey,
            Long reservationId,
            String failureCode,
            String failureMessage,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.orderId = orderId;
        this.memberId = memberId;
        this.scheduleId = scheduleId;
        this.amount = amount;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.paymentKey = paymentKey;
        this.reservationId = reservationId;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PaymentOrder pending(
            String orderId,
            Long memberId,
            Long scheduleId,
            int amount,
            String idempotencyKey,
            LocalDateTime now
    ) {
        return new PaymentOrder(
                null,
                orderId,
                memberId,
                scheduleId,
                amount,
                idempotencyKey,
                PaymentOrderStatus.PENDING,
                null,
                null,
                null,
                null,
                now,
                now
        );
    }

    public boolean isConfirmedWith(String paymentKey) {
        return status == PaymentOrderStatus.CONFIRMED && this.paymentKey != null && this.paymentKey.equals(paymentKey);
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public int getAmount() {
        return amount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public PaymentOrderStatus getStatus() {
        return status;
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getFailureCode() {
        return failureCode;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
