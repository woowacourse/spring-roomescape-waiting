package roomescape.reservation.domain;

public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    PAYMENT_FAILED,
    PAYMENT_CHECK_REQUIRED,
    WAITING
}
