package roomescape.exception;

public class InvalidReservationException extends RuntimeException {
    public InvalidReservationException() {
        super("지나간 날짜에는 예약할 수 없습니다.");
    }
}
