package roomescape.service.exception;

import roomescape.domain.exception.NotFoundException;

public class ReservationTimeNotFoundException extends NotFoundException {
    public ReservationTimeNotFoundException(String message) {
        super(message);
    }
}
