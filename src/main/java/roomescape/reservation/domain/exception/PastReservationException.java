package roomescape.reservation.domain.exception;

public class PastReservationException extends RuntimeException {

    public PastReservationException() {
        super("지난 날짜에는 예약할 수 없습니다.");
    }
}
