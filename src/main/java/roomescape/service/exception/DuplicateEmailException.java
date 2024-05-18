package roomescape.service.exception;

import roomescape.exception.RoomescapeException;

public class DuplicateEmailException extends RoomescapeException {

    public DuplicateEmailException(final String message) {
        super(message);
    }
}
