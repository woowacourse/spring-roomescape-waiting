package roomescape.exception;

public class DuplicateReservationException extends RoomescapeBaseException {
    public DuplicateReservationException() {
        super("해당 날짜·시간·테마에 이미 예약이 존재합니다. 다른 날짜·시간·테마를 선택해주세요.");
    }
}
