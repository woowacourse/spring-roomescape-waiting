package roomescape.reservation.controller.exception;

import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.reservation.domain.exception.PastReservationException;
import roomescape.reservation.service.exception.MemberAlreadyHasThisReservationException;
import roomescape.reservation.service.exception.WaitingDuplicateException;

@RestControllerAdvice
@Order(value = 1)
public class ReservationExceptionHandler {

    @ExceptionHandler(PastReservationException.class)
    public ResponseEntity<Void> handlePastReservationException(PastReservationException e) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(WaitingDuplicateException.class)
    public ResponseEntity<Void> handleWaitingDuplicateException(WaitingDuplicateException e) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(MemberAlreadyHasThisReservationException.class)
    public ResponseEntity<Void> handleMemberAlreadyHasThisReservationException(
            MemberAlreadyHasThisReservationException e) {
        return ResponseEntity.badRequest().build();
    }
}
