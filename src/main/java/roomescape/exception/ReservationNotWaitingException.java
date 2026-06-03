package roomescape.exception;

public class ReservationNotWaitingException extends RoomescapeBaseException {

    public ReservationNotWaitingException(String currentStatus) {
        super("해당 예약은 예약 대기 상태가 아닙니다. 현재 예약 상태 값: " + currentStatus);
    }
}
