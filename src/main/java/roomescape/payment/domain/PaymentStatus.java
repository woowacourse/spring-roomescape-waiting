package roomescape.payment.domain;

public enum PaymentStatus {

    PENDING,
    DONE,
    FAILED;

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isDone() {
        return this == DONE;
    }
}
