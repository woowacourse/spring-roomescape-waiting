package roomescape.exception;

public class InvalidReservationException extends RuntimeException {
    public InvalidReservationException() {
        super("이미 지난 날짜이거나 시간입니다.");
    }
}
