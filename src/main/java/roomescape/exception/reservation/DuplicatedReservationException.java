package roomescape.exception.reservation;

import org.springframework.http.HttpStatus;
import roomescape.exception.RoomescapeException;

public class DuplicatedReservationException extends RoomescapeException {
    public DuplicatedReservationException() {
        super("해당 테마의 해당 시간대에 이미 예약이 존재합니다.", HttpStatus.CONFLICT);
    }
}
