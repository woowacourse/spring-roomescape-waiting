package roomescape.exception.custom;

public class CannotCreatePastReservationException extends CustomException {

    public CannotCreatePastReservationException() {
        super("지나간 시간의 예약은 생성할 수 없습니다.");
    }
}
