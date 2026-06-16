package roomescape.common;

import java.util.Map;
import org.springframework.http.HttpStatus;
import roomescape.common.exception.BusinessRuleViolationException;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.common.exception.HiddenResourceException;
import roomescape.common.exception.InvalidInputException;
import roomescape.common.exception.PaymentAmountMismatchException;
import roomescape.common.exception.UnauthenticatedException;
import roomescape.common.exception.UnauthorizedException;

public class ExceptionHttpStatusMapper {
    private static final Map<Class<? extends DomainException>, HttpStatus> STATUS_MAP = Map.ofEntries(
            Map.entry(InvalidInputException.class, HttpStatus.BAD_REQUEST),
            Map.entry(BusinessRuleViolationException.class, HttpStatus.BAD_REQUEST),
            Map.entry(EntityNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(HiddenResourceException.class, HttpStatus.NOT_FOUND),
            Map.entry(DuplicateEntityException.class, HttpStatus.CONFLICT),
            Map.entry(UnauthenticatedException.class, HttpStatus.UNAUTHORIZED),
            Map.entry(UnauthorizedException.class, HttpStatus.FORBIDDEN),
            Map.entry(PaymentAmountMismatchException.class, HttpStatus.BAD_REQUEST)
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
