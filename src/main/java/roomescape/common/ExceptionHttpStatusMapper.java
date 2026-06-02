package roomescape.common;

import java.util.Map;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.common.exception.InvalidInputException;
import roomescape.common.exception.UnauthenticatedException;
import roomescape.common.exception.UnauthorizedException;

public class ExceptionHttpStatusMapper {
    private static final Map<Class<? extends DomainException>, HttpStatus> STATUS_MAP = Map.of(
            InvalidInputException.class, HttpStatus.BAD_REQUEST,
            BusinessRuleViolationException.class, HttpStatus.BAD_REQUEST,
            EntityNotFoundException.class, HttpStatus.NOT_FOUND,
            DuplicateEntityException.class, HttpStatus.CONFLICT,
            UnauthenticatedException.class, HttpStatus.UNAUTHORIZED,
            UnauthorizedException.class, HttpStatus.FORBIDDEN
    );

    public static HttpStatus resolve(DomainException e) {
        return STATUS_MAP.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(e.getClass()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ExceptionHttpStatusMapper() {
    }
}
