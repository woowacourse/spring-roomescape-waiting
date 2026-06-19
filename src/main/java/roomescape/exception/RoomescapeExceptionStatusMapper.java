package roomescape.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import roomescape.infrastructure.payment.OutboundRateLimitException;
import roomescape.infrastructure.payment.TossPaymentException;

public class RoomescapeExceptionStatusMapper {

    private static final HttpStatus DEFAULT_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    private static final Map<Class<? extends RoomescapeBaseException>, HttpStatus> STATUS_BY_EXCEPTION = Map.ofEntries(
            Map.entry(InvalidDomainException.class, HttpStatus.INTERNAL_SERVER_ERROR),
            Map.entry(ResourceNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(ReservationOwnerMismatchException.class, HttpStatus.FORBIDDEN),
            Map.entry(StoreManagementForbiddenException.class, HttpStatus.FORBIDDEN),
            Map.entry(DuplicateReservationException.class, HttpStatus.CONFLICT),
            Map.entry(ReservationTimeInUseException.class, HttpStatus.CONFLICT),
            Map.entry(PastDateTimeReservationException.class, HttpStatus.UNPROCESSABLE_ENTITY),
            Map.entry(PastReservationModificationException.class, HttpStatus.UNPROCESSABLE_ENTITY),
            Map.entry(DuplicateUsernameException.class, HttpStatus.CONFLICT),
            Map.entry(InvalidLoginException.class, HttpStatus.UNAUTHORIZED),
            Map.entry(UnauthenticatedException.class, HttpStatus.UNAUTHORIZED),
            Map.entry(UnauthorizedException.class, HttpStatus.FORBIDDEN),
            Map.entry(ReservationConcurrentConflictException.class, HttpStatus.CONFLICT),
            Map.entry(ReservationNotFoundForWaitingException.class, HttpStatus.CONFLICT),
            Map.entry(ReservationNotReservedException.class, HttpStatus.CONFLICT),
            Map.entry(ReservationNotWaitingException.class, HttpStatus.CONFLICT),
            Map.entry(DuplicateWaitingReservationException.class, HttpStatus.CONFLICT),
            Map.entry(PaymentAmountMismatchException.class, HttpStatus.CONFLICT),
            Map.entry(PaymentOrderNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(OutboundRateLimitException.class, HttpStatus.TOO_MANY_REQUESTS)
    );

    public HttpStatus statusOf(RoomescapeBaseException exception) {
        if (exception instanceof TossPaymentException tossPaymentException) {
            return HttpStatus.valueOf(tossPaymentException.getStatus().value());
        }
        HttpStatus status = STATUS_BY_EXCEPTION.get(exception.getClass());
        if (status == null) {
            return DEFAULT_STATUS;
        }
        return status;
    }
}
