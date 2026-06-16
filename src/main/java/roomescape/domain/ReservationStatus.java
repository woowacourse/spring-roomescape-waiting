package roomescape.domain;

public enum ReservationStatus {
    PENDING,    // 결제 대기
    CONFIRMED;  // 결제 완료(확정)

    public boolean isConfirmed() {
        return this == CONFIRMED;
    }
}
