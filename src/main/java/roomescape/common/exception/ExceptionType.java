package roomescape.common.exception;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;

public enum ExceptionType {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, BadRequestException.class),
    NOT_FOUND(HttpStatus.NOT_FOUND, NotFoundException.class),
    CONFLICT(HttpStatus.CONFLICT, ConflictException.class),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, UnauthorizedException.class),
    UNPROCESSABLE(HttpStatus.UNPROCESSABLE_ENTITY, UnprocessableException.class);

    private static final Map<Class<? extends RoomEscapeException>, HttpStatus> STATUS_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(ExceptionType::getExceptionClass, ExceptionType::getStatus));

    private final HttpStatus status;
    private final Class<? extends RoomEscapeException> exception;

    ExceptionType(HttpStatus status, Class<? extends RoomEscapeException> exception) {
        this.status = status;
        this.exception = exception;
    }

    public static HttpStatus resolveStatus(RoomEscapeException e) {
        return STATUS_MAP.getOrDefault(e.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Class<? extends RoomEscapeException> getExceptionClass() {
        return exception;
    }

}
