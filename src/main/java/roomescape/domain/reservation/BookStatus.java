package roomescape.domain.reservation;

public enum BookStatus {
    WAITING,
    BOOKED,
    WAITING_CANCELLED,
    BOOKING_CANCELLED,
    ;

    public BookStatus book() {
        if (this == WAITING) {
            return BOOKED;
        }
        throw new IllegalStateException("대기 중인 예약이 아닙니다.");
    }

    public BookStatus cancelBooking() {
        if (this == BOOKED) {
            return BOOKING_CANCELLED;
        }
        throw new IllegalStateException("확정된 예약이 아닙니다.");
    }

    public BookStatus cancelWaiting() {
        if (this == WAITING) {
            return WAITING_CANCELLED;
        }
        throw new IllegalStateException("대기 중인 예약이 아닙니다.");
    }
}
