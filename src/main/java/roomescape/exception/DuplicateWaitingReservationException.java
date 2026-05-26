package roomescape.exception;

public class DuplicateWaitingReservationException extends RoomescapeBaseException {
    public DuplicateWaitingReservationException() {
        super("이미 해당 슬롯에 예약 대기 중입니다.");
    }
}
