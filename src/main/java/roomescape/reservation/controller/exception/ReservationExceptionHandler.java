package roomescape.reservation.controller.exception;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.reservation.domain.exception.PastReservationException;
import roomescape.reservation.domain.exception.ReservationDateNullException;
import roomescape.reservation.domain.exception.ReserverNameEmptyException;

@RestControllerAdvice
@Order(value = 1)
public class ReservationExceptionHandler {

    @ExceptionHandler(ReserverNameEmptyException.class)
    public ResponseEntity<Void> handleReserverNameEmptyException(ReserverNameEmptyException e) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(PastReservationException.class)
    public ResponseEntity<Void> handlePastReservationException(PastReservationException e) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(ReservationDateNullException.class)
    public ResponseEntity<String> handelReservationDateNullException(ReservationDateNullException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
