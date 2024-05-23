package roomescape.exception.reservation;

public class DuplicatedReservationException extends ReservationException {
    private final long reservationId;

    public DuplicatedReservationException(long reservationId) {
        super("이미 예약했거나 대기한 항목입니다.");
        this.reservationId = reservationId;
    }

    public long getReservationId() {
        return reservationId;
    }
}
