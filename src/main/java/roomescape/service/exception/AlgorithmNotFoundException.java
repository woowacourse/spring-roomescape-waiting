package roomescape.service.exception;

import roomescape.exception.RoomescapeException;

public class AlgorithmNotFoundException extends RoomescapeException {

    public AlgorithmNotFoundException(final String message) {
        super(message);
    }
}
