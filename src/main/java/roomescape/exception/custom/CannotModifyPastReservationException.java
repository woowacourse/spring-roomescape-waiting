package roomescape.exception.custom;

public class CannotModifyPastReservationException extends CustomException {

    public CannotModifyPastReservationException() {
        super("지나간 시간의 예약은 수정, 삭제할 수 없습니다.");
    }
}
