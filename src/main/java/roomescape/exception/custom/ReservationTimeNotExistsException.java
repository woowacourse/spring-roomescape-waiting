package roomescape.exception.custom;

public class ReservationTimeNotExistsException extends CustomException {

    public ReservationTimeNotExistsException() {
        super("해당 예약 시간이 존재하지 않습니다.");
    }
}
