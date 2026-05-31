package roomescape.exception;

public class PastReservationControlException extends RoomescapeException {

    public PastReservationControlException() {
        super("PAST_RESERVATION_CONTRL", "이미 지난 예약은 수정/삭제할 수 없습니다.");
    }
}
