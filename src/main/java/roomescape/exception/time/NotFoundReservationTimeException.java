package roomescape.exception.time;

import org.springframework.http.HttpStatus;
import roomescape.exception.CustomException;

public class NotFoundReservationTimeException extends CustomException {
    public NotFoundReservationTimeException() {
        super("존재하지 않는 시간입니다.", HttpStatus.BAD_REQUEST);
    }
}
