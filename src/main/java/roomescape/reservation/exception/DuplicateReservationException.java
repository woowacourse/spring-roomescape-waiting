package roomescape.reservation.exception;

public class DuplicateReservationException extends RuntimeException {

    public DuplicateReservationException() {
        super("해당 시간에는 이미 예약이 존재한다.");
    }
}
