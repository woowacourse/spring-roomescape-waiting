package roomescape.exception.custom;

public class ReservationNotExistsException extends CustomException {

    public ReservationNotExistsException() {
        super("해당 예약이 존재하지 않습니다.");
    }
}
