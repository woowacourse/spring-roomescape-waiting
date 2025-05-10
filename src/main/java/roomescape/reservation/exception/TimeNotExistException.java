package roomescape.reservation.exception;

public class TimeNotExistException extends RuntimeException {

    public TimeNotExistException() {
        super("예약 시간을 찾을 수 없다.");
    }
}
