package roomescape.exception.custom;

public class CannotDeleteReservationTimeInUseException extends CustomException {

    public CannotDeleteReservationTimeInUseException() {
        super("예약에서 사용 중인 예약 시간은 삭제할 수 없습니다.");
    }
}
