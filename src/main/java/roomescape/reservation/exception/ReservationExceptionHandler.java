package roomescape.reservation.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.exception.AuthorizationException;
import roomescape.global.exception.BusinessRuleViolationException;
import roomescape.reservation.model.exception.ReservationAuthException;
import roomescape.reservation.model.exception.ReservationException;

@RestControllerAdvice
public class ReservationExceptionHandler {

    @ExceptionHandler(ReservationException.class)
    public void handleReservationException(ReservationException e) {
        throw new BusinessRuleViolationException(e.getMessage(), e);
    }

    @ExceptionHandler(ReservationAuthException.class)
    public void handleReservationAuthException(ReservationAuthException e) {
        throw new AuthorizationException(e.getMessage(), e);
    }
}
