package roomescape.exception;

public class ReservationNotReservedException extends RoomescapeBaseException {
    public ReservationNotReservedException(String currentStatus) {
        super("해당 예약은 예약 확정 상태가 아닙니다. 현재 예약 상태 값: " + currentStatus);
    }
}