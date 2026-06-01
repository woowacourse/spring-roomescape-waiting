package roomescape.exception;

public class ReservationNotFoundException extends RoomescapeException {

    public ReservationNotFoundException() {
        super("RESERVATION_NOT_FOUND", "해당 예약을 찾을 수 없습니다.");
    }
}
