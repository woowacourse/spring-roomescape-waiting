package roomescape.reservation.controller.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static roomescape.reservation.controller.response.ReservationErrorCode.PAST_RESERVATION;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.response.ApiResponse;
import roomescape.reservation.exception.AlreadyWaitingException;
import roomescape.reservation.exception.PastReservationException;

@RestControllerAdvice
public class ReservationExceptionHandler {

    @ExceptionHandler(PastReservationException.class)
    public ResponseEntity<ApiResponse<Void>> handlePastReservationException(PastReservationException e) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.fail(PAST_RESERVATION));
    }

    @ExceptionHandler(AlreadyWaitingException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyWaitingException(AlreadyWaitingException e) {
        return ResponseEntity
                .status(CONFLICT)
                .body(ApiResponse.fail(PAST_RESERVATION));
    }
}
