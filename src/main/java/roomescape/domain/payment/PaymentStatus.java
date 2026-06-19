package roomescape.domain.payment;

import roomescape.domain.ReservationStatus;

public enum PaymentStatus {
    PAYMENT_PENDING,
    CONFIRMED,
    FAILED,
    CHECK_REQUIRED,
    NONE;

    public static PaymentStatus of(ReservationStatus reservationStatus, PaymentOrder paymentOrder) {
        if (paymentOrder == null) {
            return NONE;
        }
        if (reservationStatus == ReservationStatus.RESERVED) {
            return CONFIRMED;
        }
        if (reservationStatus == ReservationStatus.PAYMENT_FAILED) {
            return FAILED;
        }
        if (paymentOrder.getPaymentKey() != null) {
            return CHECK_REQUIRED;
        }
        return PAYMENT_PENDING;
    }
}
