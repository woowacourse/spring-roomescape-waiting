package roomescape.global.exception.customException;

import roomescape.global.exception.ErrorCode;

public class EntityNotFoundException extends RoomEscapeException {

    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EntityNotFoundException(ErrorCode errorCode, Long id) {
        super(errorCode, "%s id: %d".formatted(errorCode.getMessage(), id));
    }
}
