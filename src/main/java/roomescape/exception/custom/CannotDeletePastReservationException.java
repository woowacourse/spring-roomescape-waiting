package roomescape.exception.custom;

public class CannotDeletePastReservationException extends CustomException {

    public CannotDeletePastReservationException() {
        super("지나간 시간의 예약은 삭제할 수 없습니다.");
    }
}
