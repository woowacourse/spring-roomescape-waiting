package roomescape.exception;

public class PastReservationModificationException extends RoomescapeBaseException {
    public PastReservationModificationException() {
        super("이미 지난 예약은 수정할 수 없습니다.");
    }
}