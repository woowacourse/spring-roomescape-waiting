package roomescape.payment.domain;

public enum PaymentStatus {

    PENDING,
    DONE,
    UNKNOWN,
    FAILED;

    public boolean isDone() {
        return this == DONE;
    }

    public boolean canConfirm() {
        return this == PENDING || this == UNKNOWN;
    }
}
