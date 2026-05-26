package roomescape.exception;

public class ReservationOwnerMismatchException extends RoomescapeBaseException {
    public ReservationOwnerMismatchException() {
        super("본인의 예약만 취소 혹은 변경 가능합니다.");
    }
}