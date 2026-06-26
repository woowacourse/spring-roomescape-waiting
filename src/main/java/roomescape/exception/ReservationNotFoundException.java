package roomescape.exception;

public class ReservationNotFoundException extends RoomescapeException {

    public ReservationNotFoundException(long id) {
        super("RESERVATION_NOT_FOUND", "해당 식별자의 예약을 찾을 수 없습니다. id: " + id);
    }
}
