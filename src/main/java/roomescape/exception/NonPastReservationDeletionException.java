package roomescape.exception;

public class NonPastReservationDeletionException extends RoomescapeBaseException {
    public NonPastReservationDeletionException() {
        super("아직 지나지 않은 예약은 삭제할 수 없습니다.");
    }
}
