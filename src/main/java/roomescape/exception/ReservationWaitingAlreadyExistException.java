package roomescape.exception;

public class ReservationWaitingAlreadyExistException extends RuntimeException {

    public ReservationWaitingAlreadyExistException() {
        super("이미 해당 예약에 대한 대기가 존재합니다.");
    }
}