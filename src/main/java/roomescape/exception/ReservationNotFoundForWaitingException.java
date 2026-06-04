package roomescape.exception;

public class ReservationNotFoundForWaitingException extends RoomescapeBaseException {
    public ReservationNotFoundForWaitingException() {
        super("확정 예약이 없으므로 대기 예약 생성이 불가능합니다.");
    }
}
