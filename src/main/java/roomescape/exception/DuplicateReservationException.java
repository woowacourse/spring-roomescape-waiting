package roomescape.exception;

public class DuplicateReservationException extends RoomescapeException {

    public DuplicateReservationException() {
        super("DUPLICATE_RESERVATION_INFO", "이미 예약된 시간입니다. 다른 날짜 혹은 테마를 선택해주세요.");
    }
}
