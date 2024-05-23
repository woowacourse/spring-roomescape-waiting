package roomescape.exception.reservation;

public class WaitingListExceededException extends ReservationException {
    private final long reservationId;

    public WaitingListExceededException(long reservationId) {
        super("대기 인원이 초과되었습니다.");
        this.reservationId = reservationId;
    }

    public long getReservationId() {
        return reservationId;
    }
}
