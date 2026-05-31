package roomescape.exception;

public class DuplicateReservationException extends RoomescapeException {

    public DuplicateReservationException(String date, Long timeId, Long themeId) {
        super("DUPLICATE_RESERVATION_INFO", String.format("해당 날짜(%s)의 시간(%d)과 테마(%d)는 이미 예약되어 있습니다.", date, timeId, themeId));
    }
}
