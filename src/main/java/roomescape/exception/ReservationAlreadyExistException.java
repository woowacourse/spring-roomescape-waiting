package roomescape.exception;

public class ReservationAlreadyExistException extends RuntimeException {

    public ReservationAlreadyExistException() {
        super("이미 예약된 시간입니다.");
    }
}
