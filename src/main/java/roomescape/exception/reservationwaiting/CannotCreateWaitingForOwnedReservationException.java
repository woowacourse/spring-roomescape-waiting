package roomescape.exception.reservationwaiting;

import org.springframework.http.HttpStatus;
import roomescape.exception.common.RoomescapeException;

public class CannotCreateWaitingForOwnedReservationException extends RoomescapeException {
    public CannotCreateWaitingForOwnedReservationException() {
        super("본인이 예약한 건에 대해 예약 대기를 걸 수 없습니다.", HttpStatus.BAD_REQUEST);
    }
}
