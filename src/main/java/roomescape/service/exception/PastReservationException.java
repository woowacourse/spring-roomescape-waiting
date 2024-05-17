package roomescape.service.exception;

public class PastReservationException extends RuntimeException {

    public PastReservationException() {
        super("과거의 시간대에는 예약 할 수 없습니다.");
    }
}
