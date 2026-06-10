package roomescape.exception.custom;

public class ReservationTimeAlreadyExistsException extends CustomException {

    public ReservationTimeAlreadyExistsException() {
        super("이미 중복된 예약 시간이 존재합니다.");
    }
}
