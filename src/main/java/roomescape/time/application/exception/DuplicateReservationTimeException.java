package roomescape.time.application.exception;

import roomescape.common.exception.DuplicateException;

public class DuplicateReservationTimeException extends DuplicateException {
    public DuplicateReservationTimeException(String message) {
        super(message);
    }
}
