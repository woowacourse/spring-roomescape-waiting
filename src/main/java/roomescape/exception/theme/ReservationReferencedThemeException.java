package roomescape.exception.theme;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class ReservationReferencedThemeException extends RoomescapeException {
    public ReservationReferencedThemeException() {
        super("예약이 존재하는 테마입니다.", HttpStatus.BAD_REQUEST);
    }
}
