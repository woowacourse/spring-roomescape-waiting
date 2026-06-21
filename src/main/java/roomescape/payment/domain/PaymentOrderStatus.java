package roomescape.payment.domain;

public enum PaymentOrderStatus {
    READY,
    PENDING_CONFIRMATION,
    CONFIRMED,
    FAILED;
}
