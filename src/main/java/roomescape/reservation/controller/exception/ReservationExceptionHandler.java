package roomescape.reservation.controller.exception;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static roomescape.reservation.controller.response.ReservationErrorCode.ALREADY_RESERVATION;
import static roomescape.reservation.controller.response.ReservationErrorCode.PAST_RESERVATION;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import roomescape.global.response.ApiResponse;
import roomescape.reservation.exception.InAlreadyReservationException;
import roomescape.reservation.exception.InvalidStatusTransitionException;
import roomescape.reservation.exception.PastReservationException;

@RestControllerAdvice
public class ReservationExceptionHandler {

    @ExceptionHandler(PastReservationException.class)
    public ResponseEntity<ApiResponse<Void>> handlePastReservationException() {
        return ResponseEntity
                .status(UNPROCESSABLE_ENTITY)
                .body(ApiResponse.fail(PAST_RESERVATION));
    }

    @ExceptionHandler(InAlreadyReservationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyReservationException() {
        return ResponseEntity
                .status(CONFLICT)
                .body(ApiResponse.fail(ALREADY_RESERVATION));
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidStatusTransitionException() {
        return ResponseEntity
                .status(UNPROCESSABLE_ENTITY)
                .body(ApiResponse.fail(ALREADY_RESERVATION));
    }
}
