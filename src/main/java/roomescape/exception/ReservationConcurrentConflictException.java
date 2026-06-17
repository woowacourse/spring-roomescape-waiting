package roomescape.exception;

public class ReservationConcurrentConflictException extends RoomescapeBaseException {

    public ReservationConcurrentConflictException() {
        super("예약 정보가 변경되어 요청을 처리할 수 없습니다. 다시 시도해주세요.");
    }
}
