package roomescape.reservation.controller.exception;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.reservation.domain.exception.PastReservationException;

@RestControllerAdvice
@Order(value = 1)
public class ReservationExceptionHandler {

    @ExceptionHandler(PastReservationException.class)
    public ResponseEntity<Void> handlePastReservationException(PastReservationException e) {
        return ResponseEntity.badRequest().build();
    }
}
