package roomescape.reservation.domain.exception;

import roomescape.common.exception.NotFoundException;

public class NotFoundTimeSlotException extends NotFoundException {
    public NotFoundTimeSlotException(String message) {
        super(message);
    }
}
