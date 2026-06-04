package roomescape.common.exception;

import lombok.Getter;
import roomescape.common.exception.errors.Errors;

@Getter
public abstract class RoomescapeException extends RuntimeException {

    private final Errors errors;

    public RoomescapeException(Errors errors, Object... args) {
        super(String.format(errors.getMessage(), args));
        this.errors = errors;
    }
}
