package roomescape.domain.exception;

import roomescape.exception.RoomescapeException;

public class RoleNotFoundException extends RoomescapeException {

    public RoleNotFoundException(final String message) {
        super(message);
    }
}
