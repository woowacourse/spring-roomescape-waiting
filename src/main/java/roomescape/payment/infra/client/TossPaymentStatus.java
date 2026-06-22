package roomescape.payment.infra.client;

import static roomescape.payment.domain.PaymentStatus.COMPLETED;
import static roomescape.payment.domain.PaymentStatus.FAILED;
import static roomescape.payment.domain.PaymentStatus.PENDING;

import roomescape.payment.domain.PaymentStatus;

public enum TossPaymentStatus {
    READY,
    IN_PROGRESS,
    WAITING_FOR_DEPOSIT,
    DONE,
    CANCELED,
    PARTIAL_CANCELED,
    ABORTED,
    EXPIRED,
    UNKNOWN;

    public static TossPaymentStatus from(String raw) {
        if (raw == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(raw);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    public PaymentStatus toDomainStatus() {
        return switch (this) {
            case DONE -> COMPLETED;
            case CANCELED, PARTIAL_CANCELED -> PaymentStatus.CANCELED;
            case ABORTED, EXPIRED, UNKNOWN -> FAILED;
            case READY, IN_PROGRESS, WAITING_FOR_DEPOSIT -> PENDING;
        };
    }
}
