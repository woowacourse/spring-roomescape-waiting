package roomescape.common.exception;

import lombok.Getter;

@Getter
public class RoomEscapeException extends RuntimeException {

    private final ErrorInformation errorInformation;

    protected RoomEscapeException(ErrorInformation errorInformation) {
        super(errorInformation.getMessage());
        this.errorInformation = errorInformation;
    }

}
