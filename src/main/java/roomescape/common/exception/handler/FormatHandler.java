package roomescape.common.exception.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

public interface FormatHandler {
    boolean isSupport(InvalidFormatException e);

    String handle(InvalidFormatException formatException);

    default boolean canHandle(Throwable throwable) {
        return throwable instanceof InvalidFormatException;
    }
}
