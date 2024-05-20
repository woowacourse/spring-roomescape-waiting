package roomescape.exception;

public class PastTimeReservationException extends IllegalArgumentException {

    public PastTimeReservationException(final String message) {
        super(String.format("%s는 지난 시간입니다.", message));
    }
}
